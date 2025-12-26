package com.mangawatch.media;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.mangawatch.model.Manga;
import com.mangawatch.repository.MangaRepository;

import reactor.core.publisher.Flux;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class CoverService {
	
	private final WebClient webClient;
	
	private final MangaRepository mangaRepository;
	
	private final Cache<String, CachedCover> cache = Caffeine.newBuilder()
	        .maximumWeight(50_000_000) // 50MB total cache size
	        .weigher((String key, CachedCover cover) -> cover.data.length)
	        .expireAfterWrite(Duration.ofHours(1))
	        .build();

//    private final Map<String, CachedCover> cache = new ConcurrentHashMap<>();
	
    public CoverService(WebClient.Builder builder, MangaRepository mangaRepository) {
        this.webClient = builder
                .baseUrl("https://uploads.mangadex.org")
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(5 * 1024 * 1024)) // 5MB limit
                .build();
        
        this.mangaRepository = mangaRepository;
    }
    
    private ResponseEntity<Flux<DataBuffer>> getCover(String dexId, String fileName) {
        if (!isValid(dexId, fileName)) {
            return ResponseEntity.badRequest().build();
        }

        String cacheKey = dexId + "/" + fileName;

        // Check cache
        CachedCover cached = cache.getIfPresent(cacheKey);
        if (cached != null) {
            DataBuffer buffer =
                    new DefaultDataBufferFactory().wrap(cached.data);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(cached.contentType))
                    .header(HttpHeaders.CACHE_CONTROL, "public, max-age=86400")
                    .body(Flux.just(buffer));
        }

        // Cache miss → fetch
        return streamCover(dexId, fileName, cacheKey);
    }
    
    private ParsedCover parseCoverUrl(String url) {
        if (url == null || url.isBlank()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No cover URL");
        }

        URI uri;
        try {
            uri = URI.create(url);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid cover URL");
        }

        String[] parts = uri.getPath().split("/");

        // Expected: /covers/{dexId}/{fileName}
        if (parts.length < 4) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Malformed cover path");
        }

        return new ParsedCover(parts[2], parts[3]);
    }
    
    public ResponseEntity<Flux<DataBuffer>> getCoverByMangaId(long mangaId) {

        // 1. Lookup manga
        Manga manga = mangaRepository.findById(mangaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        // 2. Extract dexId + fileName from stored coverUrl
        ParsedCover cover = parseCoverUrl(manga.getCoverUrl());

        // 3. Delegate to existing logic
        return getCover(cover.dexId(), cover.fileName());
    }
    
    //heavy, testing changes to streamCover
    public ResponseEntity<Flux<DataBuffer>> streamCover(
            String dexId,
            String fileName,
            String cacheKey
    ) {
    	
        if (!isValid(dexId, fileName)) {
            return ResponseEntity.badRequest().build();
        }
        
        //hits cache, serves from memory as a stream
        CachedCover cached = cache.getIfPresent(cacheKey);
        if (cached != null) {
            DataBuffer buffer = new DefaultDataBufferFactory().wrap(cached.data);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(cached.contentType))
                    .header(HttpHeaders.CACHE_CONTROL, "public, max-age=86400")
                    .body(Flux.just(buffer));
        }
        
        //in case cache misses, streams from dex
        Flux<DataBuffer> upstream = webClient
                .get()
                .uri("/covers/{dexId}/{fileName}", dexId, fileName)
                .accept(MediaType.ALL)
                .retrieve()
                .bodyToFlux(DataBuffer.class);
        
        //bounded cache collectr, optional
        ByteArrayOutputStream collector = new ByteArrayOutputStream();
        AtomicInteger totalBytes = new AtomicInteger(0);
        int MAX_CACHEABLE = 2 * 1024 * 1024; //2mb
    	
        Flux<DataBuffer> tee = upstream.doOnNext(buffer -> {
            int readable = buffer.readableByteCount();
            int nextSize = totalBytes.addAndGet(readable);

            if (nextSize <= MAX_CACHEABLE) {
                byte[] chunk = new byte[readable];
                buffer.read(chunk);
                collector.writeBytes(chunk);
            }
        }).doOnComplete(() -> {
            if (totalBytes.get() <= MAX_CACHEABLE) {
                cache.put(
                        cacheKey,
                        new CachedCover(
                                collector.toByteArray(),
                                inferContentType(fileName)
                        )
                );
            }
        });
        
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(inferContentType(fileName)))
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=86400")
                .body(tee);
    }
    
    private String inferContentType(String fileName) {
        String lower = fileName.toLowerCase();

        if (lower.endsWith(".png")) return MediaType.IMAGE_PNG_VALUE;
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg"))
            return MediaType.IMAGE_JPEG_VALUE;

        return MediaType.APPLICATION_OCTET_STREAM_VALUE;
    }

    private boolean isValid(String dexId, String fileName) {
        if (dexId == null || dexId.isBlank()) return false;
        if (fileName == null || fileName.isBlank()) return false;
        if (fileName.contains("..")) return false;
        if (fileName.contains("/")) return false;

        String lower = fileName.toLowerCase();

        return lower.endsWith(".jpg")
            || lower.endsWith(".jpeg")
            || lower.endsWith(".png");
    }
    
}
