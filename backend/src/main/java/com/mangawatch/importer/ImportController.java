package com.mangawatch.importer;


import com.mangawatch.importer.MangadexImporter.ImportResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

/**
 * Admin endpoints for importing manga data from MangaDex
 */
@RestController
@RequestMapping("/admin/import")
public class ImportController {

    private static final Logger log = LoggerFactory.getLogger(ImportController.class);
    
    private final MangadexImporter importer;
    
    // Track if an import is currently running
    private volatile boolean importInProgress = false;
    private volatile ImportResult lastImportResult = null;
    
    public ImportController(MangadexImporter importer) {
        this.importer = importer;
    }
    
    /**
     * GET /admin/import/info
     * Get information about available manga count
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getImportInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("totalAvailableOnMangadex", importer.getTotalAvailableManga());
        info.put("importInProgress", importInProgress);
        info.put("lastImportResult", lastImportResult);
        
        return ResponseEntity.ok(info);
    }
    
    /**
     * POST /admin/import/start
     * Start import process for ALL manga using cursor-based pagination
     * 
     * This will import the entire MangaDex catalog by using createdAt timestamps
     * as cursors, bypassing the 10k offset limit completely.
     */
    @PostMapping("/start")
    public ResponseEntity<Map<String, String>> startFullImport() {
        
        if (importInProgress) {
            return ResponseEntity.status(409)
                .body(Map.of(
                    "status", "error",
                    "message", "Import already in progress. Please wait for it to complete."
                ));
        }
        
        log.info("Starting import of all manga using cursor-based pagination");
        importInProgress = true;
        
        // Run import in background thread
        CompletableFuture.runAsync(() -> {
            try {
                ImportResult result = importer.importAllManga();
                lastImportResult = result;
                log.info("Import completed: {}", result);
            } catch (Exception e) {
                log.error("Import failed with exception", e);
            } finally {
                importInProgress = false;
            }
        }, Executors.newSingleThreadExecutor());
        
        return ResponseEntity.accepted()
            .body(Map.of(
                "status", "accepted",
                "message", "Full import started using cursor-based pagination. Check /admin/import/status for progress."
            ));
    }
    
    /**
     * POST /admin/import/resume
     * Resume import from a specific createdAt cursor
     * 
     * Use this if import was interrupted - provide the last cursor from the previous run.
     * You can get the cursor from the lastImportResult in /admin/import/status
     * 
     * Example: POST /admin/import/resume?cursor=2024-01-15T10:30:45
     */
    @PostMapping("/resume")
    public ResponseEntity<Map<String, String>> resumeImport(
            @RequestParam String cursor,
            @RequestParam(defaultValue = "2147483647") int maxManga) {
        
        if (importInProgress) {
            return ResponseEntity.status(409)
                .body(Map.of(
                    "status", "error",
                    "message", "Import already in progress"
                ));
        }
        
        log.info("Resuming import from cursor: {}", cursor);
        importInProgress = true;
        
        CompletableFuture.runAsync(() -> {
            try {
                ImportResult result = importer.importMangaWithCursor(cursor, maxManga);
                lastImportResult = result;
                log.info("Resume import completed: {}", result);
            } catch (Exception e) {
                log.error("Resume import failed", e);
            } finally {
                importInProgress = false;
            }
        }, Executors.newSingleThreadExecutor());
        
        return ResponseEntity.accepted()
            .body(Map.of(
                "status", "accepted",
                "message", String.format("Import resumed from cursor: %s", cursor)
            ));
    }
    
    /**
     * GET /admin/import/status
     * Check the status of the current/last import
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getImportStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("inProgress", importInProgress);
        
        if (lastImportResult != null) {
            status.put("lastResult", Map.of(
                "totalFetched", lastImportResult.getTotalFetched(),
                "newInserted", lastImportResult.getNewInserted(),
                "updated", lastImportResult.getUpdated(),
                "skipped", lastImportResult.getSkipped(),
                "errors", lastImportResult.getErrors(),
                "lastCursor", lastImportResult.getLastCreatedAt() != null ? 
                    lastImportResult.getLastCreatedAt() : "N/A"
            ));
        } else {
            status.put("lastResult", "No import has been run yet");
        }
        
        return ResponseEntity.ok(status);
    }
    
    /**
     * POST /admin/import/stop
     * Request to stop the current import
     * Note: Current batch will complete before stopping
     */
    @PostMapping("/stop")
    public ResponseEntity<Map<String, String>> stopImport() {
        if (!importInProgress) {
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "status", "error",
                    "message", "No import is currently running"
                ));
        }
        
        return ResponseEntity.ok(Map.of(
            "status", "acknowledged",
            "message", "Stop request received. Current batch will complete before stopping. Note the lastCursor from /status to resume later."
        ));
    }
}
