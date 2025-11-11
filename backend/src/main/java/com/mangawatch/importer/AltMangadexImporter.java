package com.mangawatch.importer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mangawatch.dto.AuthorResponse;
import com.mangawatch.dto.MangadexResponse;
import com.mangawatch.importer.MangadexImporter.ImportResult;
import com.mangawatch.model.Manga;
import com.mangawatch.repository.MangaRepository;
import com.mangawatch.service.MangadexTransformer;

import java.time.Duration;
import java.util.*;

/**
 * Service to import manga data from MangaDex API into our database.
 * Handles pagination, rate limiting, retries, and deduplication.
 * Written with the multipass appraoch in mind not the offset multiple manual calls.
 */
//@Service
public class AltMangadexImporter {
    
    private static final Logger log = LoggerFactory.getLogger(MangadexImporter.class);
    
    private final WebClient apiClient;
    private final MangaRepository mangaRepository;
    private final MangadexTransformer transformer;
    
    @Value("${mangadex.import.batch-size:100}")
    private int batchSize;
    
    @Value("${mangadex.import.rate-limit-ms:250}")
    private int rateLimitMs;
    
    @Value("${mangadex.import.max-retries:3}")
    private int maxRetries;
    
    public AltMangadexImporter(
            WebClient mangadexWebClient,
            MangaRepository mangaRepository,
            MangadexTransformer transformer) {
        this.apiClient = mangadexWebClient;
        this.mangaRepository = mangaRepository;
        this.transformer = transformer;
    }
    
    /**
     * Import result statistics
     */
    public static class ImportResult {
        private int totalFetched;
        private int newInserted;
        private int updated;
        private int skipped;
        private int errors;
        private List<String> errorDetails = new ArrayList<>();
        
        public ImportResult() {}
        
        public void incrementFetched() { totalFetched++; }
        public void incrementInserted() { newInserted++; }
        public void incrementUpdated() { updated++; }
        public void incrementSkipped() { skipped++; }
        public void incrementErrors() { errors++; }
        
        public void addError(String mangaId, String error) {
            errors++;
            if (errorDetails.size() < 100) {
                errorDetails.add(String.format("Manga %s: %s", mangaId, error));
            }
        }
        
        public int getTotalFetched() { return totalFetched; }
        public int getNewInserted() { return newInserted; }
        public int getUpdated() { return updated; }
        public int getSkipped() { return skipped; }
        public int getErrors() { return errors; }
        public List<String> getErrorDetails() { return errorDetails; }
        
        @Override
        public String toString() {
            return String.format(
                "Import Complete - Fetched: %d, Inserted: %d, Updated: %d, Skipped: %d, Errors: %d",
                totalFetched, newInserted, updated, skipped, errors
            );
        }
    }
    
    /**
     * Main import method - fetches manga from MangaDex and saves to database.
     * 
     * @param maxManga Maximum number of manga to import (use Integer.MAX_VALUE for all)
     * @return ImportResult with statistics
     */
    public ImportResult importManga(int maxManga) {
        log.info("Starting MangaDex import - max manga: {}", maxManga);
        
        ImportResult result = new ImportResult();
        int offset = 0;
        int fetchedCount = 0;
        String lastCreatedAt = null; // Track last manga's creation date
        
        while (fetchedCount < maxManga) {
            try {
                int limit = Math.min(batchSize, maxManga - fetchedCount);
                
                log.info("Fetching page at offset {} with limit {}", offset, limit);
                
                // Fetch page (uses createdAtSince if offset >= 10k)
                MangadexResponse response = fetchPageWithRetry(limit, offset, "createdAt", "asc");
                
                if (response == null || response.getData() == null || response.getData().isEmpty()) {
                    log.info("No more data available. Import complete.");
                    break;
                }
                
                // Track the last manga's creation date for next iteration
                var lastManga = response.getData().get(response.getData().size() - 1);
                if (lastManga.getAttributes() != null && lastManga.getAttributes().getCreatedAt() != null) {
                    lastCreatedAt = lastManga.getAttributes().getCreatedAt();
                }
                
                // Transform and save this batch
                processBatch(response.getData(), result);
                
                int returnedCount = response.getData().size();
                fetchedCount += returnedCount;
                offset += returnedCount;
                
                log.info("Progress: {}/{} manga processed (last createdAt: {})", 
                    fetchedCount, maxManga, lastCreatedAt);
                
                if (returnedCount < limit) {
                    log.info("Received fewer results than requested. Reached end of catalog.");
                    break;
                }
                
                Thread.sleep(rateLimitMs);
                
            } catch (InterruptedException e) {
                log.error("Import interrupted", e);
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("Error during import at offset {}", offset, e);
                result.addError("batch-at-offset-" + offset, e.getMessage());
                offset += batchSize;
            }
        }
        
        log.info(result.toString());
        return result;
    }
    
    public ImportResult importAllInMultiplePasses() {
        ImportResult totalResult = new ImportResult();
        
        log.info("=== PASS 1: Safe content ===");
        ImportResult pass1 = importWithFilters(List.of("safe"), null, 10000);
        mergeResults(totalResult, pass1);
        
        log.info("=== PASS 2: Suggestive content ===");
        ImportResult pass2 = importWithFilters(List.of("suggestive"), null, 10000);
        mergeResults(totalResult, pass2);
        
        log.info("=== PASS 3: Erotica content ===");
        ImportResult pass3 = importWithFilters(List.of("erotica"), null, 10000);
        mergeResults(totalResult, pass3);
        
        // Shounen demographic
        log.info("=== PASS 4: Shounen demographic ===");
        ImportResult pass4 = importWithDemographic("shounen", 10000);
        mergeResults(totalResult, pass4);
        
        log.info("=== PASS 5: Seinen demographic ===");
        ImportResult pass5 = importWithDemographic("seinen", 10000);
        mergeResults(totalResult, pass5);
        
        // Shoujo demographic
        log.info("=== PASS 6: Shoujo demographic ===");
        ImportResult pass6 = importWithDemographic("shoujo", 10000);
        mergeResults(totalResult, pass6);
        
        // Josei demographic
        log.info("=== PASS 7: Josei demographic ===");
        ImportResult pass7 = importWithDemographic("josei", 10000);
        mergeResults(totalResult, pass7);
        
        //No demographic
        log.info("=== PASS 8: No demographic ===");
        ImportResult pass8 = importWithDemographic("none", 10000);
        mergeResults(totalResult, pass8);
        
        log.info("===== MULTI-PASS IMPORT COMPLETE =====");
        log.info(totalResult.toString());
        return totalResult;
    }
    
    // Demographic based
    private ImportResult importWithDemographic(String demographic, int maxManga) {
        return importWithFilters(List.of("safe", "suggestive", "erotica"), demographic, maxManga);
    }
    
    //Filter based
    private ImportResult importWithFilters(List<String> contentRatings, String demographic, int maxManga) {
        ImportResult result = new ImportResult();
        int offset = 0;
        int fetchedCount = 0;
        
        while (fetchedCount < maxManga && offset < 10000) {
            try {
                int limit = Math.min(batchSize, maxManga - fetchedCount);
                
                MangadexResponse response = fetchPageWithFilters(limit, offset, contentRatings, demographic);
                
                if (response == null || response.getData() == null || response.getData().isEmpty()) {
                    break;
                }
                
                processBatch(response.getData(), result);
                
                int returnedCount = response.getData().size();
                fetchedCount += returnedCount;
                offset += returnedCount;
                
                log.info("Pass progress: {}/{} (new: {}, updated: {})", 
                    fetchedCount, maxManga, result.getNewInserted(), result.getUpdated());
                
                if (returnedCount < limit) break;
                Thread.sleep(rateLimitMs);
                
            } catch (Exception e) {
                log.error("Error in pass at offset {}", offset, e);
                result.addError("offset-" + offset, e.getMessage());
                break;
            }
        }
        return result;
    }
    
    private MangadexResponse fetchPageWithFilters(int limit, int offset, List<String> contentRatings, String demographic) {
        int attempt = 0;
        Exception lastException = null;

        while (attempt < maxRetries) {
            try {
                if (offset >= 10000) {
                    log.warn("Reached 10k offset limit for this pass");
                    return null;
                }
                
                return apiClient.get()
                    .uri(uriBuilder -> {
                        var builder = uriBuilder
                            .path("/manga")
                            .queryParam("limit", limit)
                            .queryParam("offset", offset)
                            .queryParam("includes[]", "cover_art")
                            .queryParam("order[createdAt]", "asc");
                        
                        // Add content ratings
                        for (String rating : contentRatings) {
                            builder.queryParam("contentRating[]", rating);
                        }
                        
                        // Add demographic filter if specified
                        if (demographic != null && !demographic.equals("none")) {
                            builder.queryParam("publicationDemographic[]", demographic);
                        }

                        return builder.build();
                    })
                    .retrieve()
                    .bodyToMono(MangadexResponse.class)
                    .timeout(Duration.ofSeconds(30))
                    .block();

            } catch (Exception e) {
                lastException = e;
                attempt++;
                log.warn("Fetch attempt {} failed: {}. Retrying...", attempt, e.getMessage());
                try {
                    Thread.sleep(1000 * attempt);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        log.error("Failed to fetch after {} attempts", maxRetries, lastException);
        return null;
    }
    
    private ImportResult importWithOrder(String orderBy, String orderDirection, int maxManga) {
        ImportResult result = new ImportResult();
        int offset = 0;
        int fetchedCount = 0;
        
        while (fetchedCount < maxManga && offset < 10000) { // Stop at 10k
            try {
                int limit = Math.min(batchSize, maxManga - fetchedCount);
                
                MangadexResponse response = fetchPageWithRetry(limit, offset, orderBy, orderDirection);
                
                if (response == null || response.getData() == null || response.getData().isEmpty()) {
                    break;
                }
                
                processBatch(response.getData(), result);
                
                int returnedCount = response.getData().size();
                fetchedCount += returnedCount;
                offset += returnedCount;
                
                log.info("Pass progress: {}/{}", fetchedCount, maxManga);
                
                if (returnedCount < limit) break;
                
                Thread.sleep(rateLimitMs);
                
            } catch (Exception e) {
                log.error("Error in pass at offset {}", offset, e);
                break;
            }
        }
        
        return result;
    }
    
    private void mergeResults(ImportResult total, ImportResult pass) {
        total.totalFetched += pass.totalFetched;
        total.newInserted += pass.newInserted;
        total.updated += pass.updated;
        total.errors += pass.errors;
    }
    
    /**
     * Fetch a single page from MangaDex API with retry logic
     * Uses createdAtSince to avoid offset limit
     */
    private MangadexResponse fetchPageWithRetry(int limit, int offset, String orderBy, String orderDirection) {
        int attempt = 0;
        Exception lastException = null;

        while (attempt < maxRetries) {
            try {
                // If we hit 10k limit, stop this pass
                if (offset >= 10000) {
                    log.warn("Reached 10k offset limit for this pass. Stopping.");
                    return null;
                }
                
                return apiClient.get()
                    .uri(uriBuilder -> {
                        var builder = uriBuilder
                            .path("/manga")
                            .queryParam("limit", limit)
                            .queryParam("offset", offset)
                            .queryParam("includes[]", "cover_art")
                            .queryParam("contentRating[]", "safe")
                            .queryParam("contentRating[]", "suggestive")
                            .queryParam("contentRating[]", "erotica");
                        
                        // Add dynamic ordering
                        if (orderBy != null) {
                            builder.queryParam("order[" + orderBy + "]", orderDirection);
                        }

                        return builder.build();
                    })
                    .retrieve()
                    .bodyToMono(MangadexResponse.class)
                    .timeout(Duration.ofSeconds(30))
                    .block();

            } catch (Exception e) {
                lastException = e;
                attempt++;
                log.warn("Fetch attempt {} failed: {}. Retrying...", attempt, e.getMessage());

                try {
                    Thread.sleep(1000 * attempt);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        log.error("Failed to fetch after {} attempts", maxRetries, lastException);
        return null;
    }
    
    /**
     * Process a batch of manga - transform and save to database
     * Uses batch author fetching for better performance
     */
    @Transactional
    protected void processBatch(List<MangadexResponse.MangadexManga> dexMangas, ImportResult result) {
        List<Manga> toSave = new ArrayList<>();
        
        // Step 1: Collect all unique author IDs from this batch
        Set<String> authorIds = new HashSet<>();
        for (MangadexResponse.MangadexManga dexManga : dexMangas) {
            String authorId = extractAuthorId(dexManga);
            if (authorId != null) {
                authorIds.add(authorId);
                log.debug("Found author ID {} for manga {}", authorId, dexManga.getId());
            } else {
                log.warn("No author found for manga {}", dexManga.getId());
            }
        }
        
        log.info("Collected {} unique author IDs from batch of {} manga", authorIds.size(), dexMangas.size());
        
        // Step 2: Fetch all authors in one batch request
        Map<String, String> authorIdToName = fetchAuthorsBatch(authorIds);
        log.info("Successfully fetched {} author names: {}", authorIdToName.size(), authorIdToName);
        
        // Step 3: Transform each manga with the pre-fetched author name and cover
        for (MangadexResponse.MangadexManga dexManga : dexMangas) {
            try {
                result.incrementFetched();
                
                // Get author name from our pre-fetched map
                String authorId = extractAuthorId(dexManga);
                String authorName = authorId != null ? authorIdToName.get(authorId) : null;
                
                // Extract cover filename from relationships
                String coverFileName = extractCoverFileName(dexManga);
                
                log.debug("Processing manga {} with author '{}', cover: {}", 
                    dexManga.getId(), authorName, coverFileName);
                
                // Transform MangaDex object to our entity with author name and cover
                Manga manga = transformer.transform(dexManga, authorName, coverFileName);
                
                // Check if this manga already exists in our database
                Optional<Manga> existing = mangaRepository.findByDexId(manga.getDexId());
                
                if (existing.isPresent()) {
                    // Update existing manga
                    Manga existingManga = existing.get();
                    updateExistingManga(existingManga, manga);
                    toSave.add(existingManga);
                    result.incrementUpdated();
                    log.info("✏️  Updated: '{}' by '{}'", manga.getTitle(), manga.getAuthor());
                } else {
                    // New manga
                    toSave.add(manga);
                    result.incrementInserted();
                    log.info("✅ New: '{}' by '{}' {}", 
                        manga.getTitle(), 
                        manga.getAuthor(),
                        manga.getCoverUrl() != null ? "[cover ✓]" : "[no cover]");
                }
                
            } catch (Exception e) {
                log.error("Error processing manga: {}", dexManga.getId(), e);
                result.incrementErrors();
            }
        }
        
        // Batch save to database (much faster than individual saves)
        if (!toSave.isEmpty()) {
            mangaRepository.saveAll(toSave);
            log.info("💾 Saved batch of {} manga to database", toSave.size());
        }
    }
    
    /**
     * Extract author ID from manga relationships
     * A manga can have multiple authors; we take the first one
     */
    private String extractAuthorId(MangadexResponse.MangadexManga dexManga) {
        if (dexManga.getRelationships() == null) {
            return null;
        }
        
        return dexManga.getRelationships().stream()
            .filter(rel -> "author".equals(rel.getType()))
            .map(MangadexResponse.MangadexRelationship::getId)
            .findFirst()
            .orElse(null);
    }
    
    /**
     * Extract cover filename from manga relationships
     * Cover art is included when we use includes[]=cover_art parameter
     */
    private String extractCoverFileName(MangadexResponse.MangadexManga dexManga) {
        if (dexManga.getRelationships() == null) {
            return null;
        }
        
        // Find cover_art relationship
        for (MangadexResponse.MangadexRelationship rel : dexManga.getRelationships()) {
            if ("cover_art".equals(rel.getType())) {
                // Extract filename from attributes
                if (rel.getAttributes() != null && rel.getAttributes().getFileName() != null) {
                    return rel.getAttributes().getFileName();
                }
                // Fallback: if no attributes, return null (no cover available)
                return null;
            }
        }
        
        return null;
    }
    
    /**
     * Fetch multiple authors in a single batch request
     * Much more efficient than individual requests
     * 
     * @param authorIds Set of author IDs to fetch
     * @return Map of authorId -> authorName
     */
    private Map<String, String> fetchAuthorsBatch(Set<String> authorIds) {
        Map<String, String> result = new HashMap<>();
        
        if (authorIds.isEmpty()) {
            log.debug("No author IDs to fetch");
            return result;
        }
        
        try {
            List<String> idList = new ArrayList<>(authorIds);
            
            log.info("🔍 Fetching batch of {} authors from MangaDex API...", idList.size());
            
            // Use the simple wrapper to deserialize
            AuthorBatchWrapper response = apiClient.get()
                .uri(uriBuilder -> {
                    var builder = uriBuilder.path("/author");
                    for (String id : idList) {
                        builder.queryParam("ids[]", id);
                    }
                    builder.queryParam("limit", 100);
                    return builder.build();
                })
                .retrieve()
                .bodyToMono(AuthorBatchWrapper.class)
                .timeout(Duration.ofSeconds(30))
                .block();
            
            if (response == null || response.getData() == null) {
                log.error("❌ Author batch response was null or empty");
                return result;
            }
            
            log.info("📦 Received {} authors from API", response.getData().size());
            
            for (AuthorResponse.AuthorData author : response.getData()) {
                if (author != null && author.getAttributes() != null && author.getAttributes().getName() != null) {
                    String name = author.getAttributes().getName();
                    result.put(author.getId(), name);
                    log.debug("  ✓ {} -> {}", author.getId(), name);
                }
            }
            
            log.info("✅ Successfully mapped {} author IDs to names", result.size());
            
        } catch (Exception e) {
            log.error("❌ Failed to batch fetch authors", e);
        }
        
        return result;
    }
    
    /**
     * Simple wrapper for batch author response (data is a list)
     * This is ONLY used internally for deserialization
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class AuthorBatchWrapper {
        @JsonProperty("data")
        private List<AuthorResponse.AuthorData> data;
        
        public List<AuthorResponse.AuthorData> getData() { return data; }
        public void setData(List<AuthorResponse.AuthorData> data) { this.data = data; }
    }
    
    /**
     * Fetch author name from MangaDex API by author ID (single request - fallback)
     * Returns null if fetch fails
     */
    private String fetchAuthorName(String authorId) {
        try {
            AuthorResponse response = apiClient.get()
                .uri("/author/{id}", authorId)
                .retrieve()
                .bodyToMono(AuthorResponse.class)
                .timeout(Duration.ofSeconds(10))
                .block();
            
            if (response != null && response.getData() != null && response.getData().getAttributes() != null) {
                String name = response.getData().getAttributes().getName();
                log.debug("Fetched author: {} (ID: {})", name, authorId);
                return name;
            }
            
        } catch (Exception e) {
            log.warn("Failed to fetch author ID {}: {}", authorId, e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Update fields of an existing manga with new data
     * Only updates fields that might change (status, description, genres, etc.)
     */
    private void updateExistingManga(Manga existing, Manga updated) {
        // Update mutable fields
        existing.setTitle(updated.getTitle());
        existing.setAuthor(updated.getAuthor());
        existing.setYear(updated.getYear());
        existing.setStatus(updated.getStatus());
        existing.setDescription(updated.getDescription());
        existing.setAltTitles(updated.getAltTitles());
        existing.setGenres(updated.getGenres());
        // Note: we keep the existing ID and dexId
    }
    
    /**
     * Get total count of manga available on MangaDex (for informational purposes)
     */
    public int getTotalAvailableManga() {
        try {
            MangadexResponse response = apiClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/manga")
                    .queryParam("limit", 1)
                    .queryParam("offset", 0)
                    .build())
                .retrieve()
                .bodyToMono(MangadexResponse.class)
                .block();
                
            return response != null && response.getTotal() != null ? response.getTotal() : 0;
        } catch (Exception e) {
            log.error("Failed to get total manga count", e);
            return 0;
        }
    }
}
