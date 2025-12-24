package com.mangawatch.media;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;

import com.mangawatch.model.Manga;
import com.mangawatch.repository.MangaRepository;

import java.net.URI;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CoverService {
	
	private final WebClient webClient;
	
	private final MangaRepository mangaRepository;

    private final Map<String, CachedCover> cache = new ConcurrentHashMap<>();
	
    public CoverService(WebClient.Builder builder, MangaRepository mangaRepository) {
        this.webClient = builder
                .baseUrl("https://uploads.mangadex.org")
                .build();
        
        this.mangaRepository = mangaRepository;
    }
    
    private ResponseEntity<byte[]> getCover(String dexId, String fileName) {

        // 1. Validation
        if (!isValid(dexId, fileName)) {
            return ResponseEntity.badRequest().build();
        }

        String cacheKey = dexId + "/" + fileName;

        // 2. Cache hit
        CachedCover cached = cache.get(cacheKey);
        if (cached != null) {
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(cached.contentType))
                    .header(HttpHeaders.CACHE_CONTROL, "public, max-age=86400")
                    .body(cached.data);
        }

        // 3. Cache miss → fetch from MangaDex
        return fetchAndCache(dexId, fileName, cacheKey);
    }
    
    private ParsedCover parseCoverUrl(String url) {
        URI uri = URI.create(url);
        String[] parts = uri.getPath().split("/");

        String dexId = parts[2];
        String fileName = parts[3];

        return new ParsedCover(dexId, fileName);
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

            String contentType = response.getHeaders()
                    .getContentType()
                    .toString();

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
            return ResponseEntity.notFound().build();
        }
    }

    private boolean isValid(String dexId, String fileName) {
        if (dexId == null || dexId.isBlank()) return false;
        if (fileName == null || fileName.isBlank()) return false;
        if (fileName.contains("..")) return false;
        if (fileName.contains("/")) return false;

        return fileName.endsWith(".jpg")
                || fileName.endsWith(".jpeg")
                || fileName.endsWith(".png");
    }
    
}
