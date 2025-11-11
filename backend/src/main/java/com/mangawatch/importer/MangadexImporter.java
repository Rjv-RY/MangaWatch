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
import com.mangawatch.model.Manga;
import com.mangawatch.repository.MangaRepository;
import com.mangawatch.service.MangadexTransformer;

import java.time.Duration;
import java.util.*;

/**
 * Service to import manga data from MangaDex API into our database.
 * Uses cursor-based pagination with createdAt timestamps to bypass 10k offset limit.
 */
@Service
public class MangadexImporter {
    
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
    
    public MangadexImporter(
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
        private String lastCreatedAt;
        
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
        public String getLastCreatedAt() { return lastCreatedAt; }
        public void setLastCreatedAt(String lastCreatedAt) { this.lastCreatedAt = lastCreatedAt; }
        
        @Override
        public String toString() {
            return String.format(
                "Import Complete - Fetched: %d, Inserted: %d, Updated: %d, Skipped: %d, Errors: %d (Last cursor: %s)",
                totalFetched, newInserted, updated, skipped, errors, lastCreatedAt
            );
        }
    }
    
    /**
     * Main import method - fetches ALL manga using cursor-based pagination.
     * This bypasses the 10k offset limit by using createdAtSince parameter.
     * 
     * @return ImportResult with statistics
     */
    public ImportResult importAllManga() {
        return importMangaWithCursor(null, Integer.MAX_VALUE);
    }
    
    /**
     * Import manga starting from a specific createdAt cursor.
     * Use this to resume an interrupted import or to manually paginate.
     * 
     * @param startCursor createdAt timestamp to start from (null = from beginning)
     * @param maxManga Maximum number of manga to import
     * @return ImportResult with statistics including last cursor position
     */
    public ImportResult importMangaWithCursor(String startCursor, int maxManga) {
        log.info("Starting MangaDex import - cursor: {}, max: {}, batch size: {}", 
            startCursor != null ? startCursor : "START", maxManga, batchSize);
        
        ImportResult result = new ImportResult();
        result.setLastCreatedAt(startCursor);
        
        int fetchedCount = 0;
        String currentCursor = startCursor;
        int offsetWithinBatch = 0; // Track offset within current 10k window
        
        while (fetchedCount < maxManga) {
            try {
                int limit = Math.min(batchSize, maxManga - fetchedCount);
                
                log.info("Fetching page - cursor: {}, offset: {}, limit: {}", 
                    currentCursor != null ? currentCursor : "START", offsetWithinBatch, limit);
                
                // Fetch page using cursor + offset
                MangadexResponse response = fetchPageWithCursor(currentCursor, offsetWithinBatch, limit);
                
                if (response == null || response.getData() == null || response.getData().isEmpty()) {
                    log.info("No more data available. Import complete.");
                    break;
                }
                
                // Process this batch
                processBatch(response.getData(), result);
                
                int returnedCount = response.getData().size();
                fetchedCount += returnedCount;
                offsetWithinBatch += returnedCount;
                
                // Check if we need to move cursor (approaching 10k limit)
                if (offsetWithinBatch >= 9900) {
                    // Extract the last manga's createdAt timestamp for next cursor
                    var lastManga = response.getData().get(response.getData().size() - 1);
                    if (lastManga.getAttributes() != null && lastManga.getAttributes().getCreatedAt() != null) {
                        currentCursor = lastManga.getAttributes().getCreatedAt();
                        result.setLastCreatedAt(currentCursor);
                        offsetWithinBatch = 0; // Reset offset for new cursor window
                        log.info("🔄 Moving to new cursor window: {}", currentCursor);
                    }
                }
                
                log.info("Progress: {}/{} manga processed (cursor: {}, offset: {})", 
                    fetchedCount, maxManga, currentCursor != null ? currentCursor : "START", offsetWithinBatch);
                
                // If we got fewer results than requested, we've reached the end
                if (returnedCount < limit) {
                    log.info("Received fewer results than requested. Reached end of catalog.");
                    break;
                }
                
                // Rate limiting
                Thread.sleep(rateLimitMs);
                
            } catch (InterruptedException e) {
                log.error("Import interrupted", e);
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("Error during import at cursor {} offset {}", currentCursor, offsetWithinBatch, e);
                result.addError("cursor-" + currentCursor + "-offset-" + offsetWithinBatch, e.getMessage());
                // Try to continue
                offsetWithinBatch += batchSize;
            }
        }
        
        log.info(result.toString());
        return result;
    }
    
    /**
     * Fetch a page from MangaDex API using cursor-based pagination.
     * 
     * @param createdAtCursor The createdAt timestamp to start from (null for beginning)
     * @param offset Offset within this cursor window (0-9999)
     * @param limit Number of results to fetch
     * @return MangadexResponse with manga data
     */
    private MangadexResponse fetchPageWithCursor(String createdAtCursor, int offset, int limit) {
        int attempt = 0;
        Exception lastException = null;

        while (attempt < maxRetries) {
            try {
                return apiClient.get()
                    .uri(uriBuilder -> {
                        var builder = uriBuilder
                            .path("/manga")
                            .queryParam("limit", limit)
                            .queryParam("offset", offset)
                            .queryParam("includes[]", "cover_art")
                            .queryParam("contentRating[]", "safe")
                            .queryParam("contentRating[]", "suggestive")
                            .queryParam("contentRating[]", "erotica")
                            .queryParam("order[createdAt]", "asc");
                        
                        // Add cursor if provided - CRITICAL: Must be properly encoded!
                        if (createdAtCursor != null) {
                            // UriBuilder handles encoding automatically
                            builder.queryParam("createdAtSince", createdAtCursor);
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
            }
        }
        
        log.debug("Collected {} unique author IDs from batch of {} manga", authorIds.size(), dexMangas.size());
        
        // Step 2: Fetch all authors in one batch request
        Map<String, String> authorIdToName = fetchAuthorsBatch(authorIds);
        
        // Step 3: Transform each manga with the pre-fetched author name and cover
        for (MangadexResponse.MangadexManga dexManga : dexMangas) {
            try {
                result.incrementFetched();
                
                // Get author name from our pre-fetched map
                String authorId = extractAuthorId(dexManga);
                String authorName = authorId != null ? authorIdToName.get(authorId) : null;
                
                // Extract cover filename from relationships
                String coverFileName = extractCoverFileName(dexManga);
                
                // Transform MangaDex object to our entity
                Manga manga = transformer.transform(dexManga, authorName, coverFileName);
                
                // Check if this manga already exists in our database
                Optional<Manga> existing = mangaRepository.findByDexId(manga.getDexId());
                
                if (existing.isPresent()) {
                    // Update existing manga
                    Manga existingManga = existing.get();
                    updateExistingManga(existingManga, manga);
                    toSave.add(existingManga);
                    result.incrementUpdated();
                } else {
                    // New manga
                    toSave.add(manga);
                    result.incrementInserted();
                }
                
            } catch (Exception e) {
                log.error("Error processing manga: {}", dexManga.getId(), e);
                result.incrementErrors();
            }
        }
        
        // Batch save to database
        if (!toSave.isEmpty()) {
            mangaRepository.saveAll(toSave);
            log.info("💾 Saved batch of {} manga (new: {}, updated: {})", 
                toSave.size(), result.getNewInserted(), result.getUpdated());
        }
    }
    
    /**
     * Extract author ID from manga relationships
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
     */
    private String extractCoverFileName(MangadexResponse.MangadexManga dexManga) {
        if (dexManga.getRelationships() == null) {
            return null;
        }
        
        for (MangadexResponse.MangadexRelationship rel : dexManga.getRelationships()) {
            if ("cover_art".equals(rel.getType())) {
                if (rel.getAttributes() != null && rel.getAttributes().getFileName() != null) {
                    return rel.getAttributes().getFileName();
                }
                return null;
            }
        }
        
        return null;
    }
    
    /**
     * Fetch multiple authors in a single batch request
     */
    private Map<String, String> fetchAuthorsBatch(Set<String> authorIds) {
        Map<String, String> result = new HashMap<>();
        
        if (authorIds.isEmpty()) {
            return result;
        }
        
        try {
            List<String> idList = new ArrayList<>(authorIds);
            
            log.debug("🔍 Fetching batch of {} authors...", idList.size());
            
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
                log.error("❌ Author batch response was null");
                return result;
            }
            
            for (AuthorResponse.AuthorData author : response.getData()) {
                if (author != null && author.getAttributes() != null && author.getAttributes().getName() != null) {
                    result.put(author.getId(), author.getAttributes().getName());
                }
            }
            
            log.debug("✅ Mapped {} authors", result.size());
            
        } catch (Exception e) {
            log.error("❌ Failed to batch fetch authors", e);
        }
        
        return result;
    }
    
    /**
     * Simple wrapper for batch author response
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class AuthorBatchWrapper {
        @JsonProperty("data")
        private List<AuthorResponse.AuthorData> data;
        
        public List<AuthorResponse.AuthorData> getData() { return data; }
        public void setData(List<AuthorResponse.AuthorData> data) { this.data = data; }
    }
    
    /**
     * Update fields of an existing manga with new data
     */
    private void updateExistingManga(Manga existing, Manga updated) {
        existing.setTitle(updated.getTitle());
        existing.setAuthor(updated.getAuthor());
        existing.setYear(updated.getYear());
        existing.setStatus(updated.getStatus());
        existing.setDescription(updated.getDescription());
        existing.setAltTitles(updated.getAltTitles());
        existing.setGenres(updated.getGenres());
    }
    
    /**
     * Get total count of manga available on MangaDex
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
