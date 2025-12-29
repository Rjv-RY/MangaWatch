package com.mangawatch.media;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.mangawatch.model.Manga;
import com.mangawatch.repository.MangaRepository;

import java.net.URI;
import java.time.Duration;

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
    
    private ResponseEntity<byte[]> getCover(String dexId, String fileName) {
        if (!isValid(dexId, fileName)) {
            return ResponseEntity.badRequest().build();
        }

        String cacheKey = dexId + "/" + fileName;

        // Check cache
        CachedCover cached = cache.getIfPresent(cacheKey);
        if (cached != null) {
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(cached.contentType))
                    .header(HttpHeaders.CACHE_CONTROL, "public, max-age=86400")
                    .body(cached.data);
        }

        // Cache miss → fetch
        return fetchAndCache(dexId, fileName, cacheKey);
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
    
    public ResponseEntity<byte[]> getCoverByMangaId(long mangaId) {

        // 1. Lookup manga
        Manga manga = mangaRepository.findById(mangaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        // 2. Extract dexId + fileName from stored coverUrl
        ParsedCover cover = parseCoverUrl(manga.getCoverUrl());

        // 3. Delegate to existing logic
        return getCover(cover.dexId(), cover.fileName());
    }
    
    private ResponseEntity<byte[]> fetchAndCache(
            String dexId,
            String fileName,
            String cacheKey
    ) {
        try {
            ResponseEntity<byte[]> response = webClient
                    .get()
                    .uri("/covers/{dexId}/{fileName}", dexId, fileName)
                    .accept(MediaType.ALL)
                    .retrieve()
                    .toEntity(byte[].class)
                    .block(Duration.ofSeconds(15));

            if (response == null || response.getBody() == null) {
                return ResponseEntity.notFound().build();
            }

            String contentType = response.getHeaders().getContentType() != null
                    ? response.getHeaders().getContentType().toString()
                    : inferContentType(fileName);
            
            System.out.println("Cover filename = " + fileName);

            CachedCover cached = new CachedCover(
                    response.getBody(),
                    contentType
            );

            cache.put(cacheKey, cached);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CACHE_CONTROL, "public, max-age=86400")
                    .body(cached.data);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
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
