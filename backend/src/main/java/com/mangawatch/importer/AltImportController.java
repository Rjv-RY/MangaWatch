package com.mangawatch.importer;

import com.fasterxml.jackson.databind.JsonNode;
import com.mangawatch.importer.MangadexImporter.ImportResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;


///**
// * Alternate approach to Admin endpoints for importing manga data from MangaDex
// * Written following the Multipass approach of Importer
// */
////@RestController
////@RequestMapping("/admin/import")
//public class AltImportController {
//
//    private static final Logger log = LoggerFactory.getLogger(ImportController.class);
//    
//    private final MangadexImporter importer;
//    
//    // Track if an import is currently running
//    private volatile boolean importInProgress = false;
//    private volatile ImportResult lastImportResult = null;
//    
//    public AltImportController(MangadexImporter importer) {
//        this.importer = importer;
//    }
//    
//    /**
//     * GET /admin/import/info
//     * Get information about available manga count
//     */
//    @GetMapping("/info")
//    public ResponseEntity<Map<String, Object>> getImportInfo() {
//        Map<String, Object> info = new HashMap<>();
//        info.put("totalAvailableOnMangadex", importer.getTotalAvailableManga());
//        info.put("importInProgress", importInProgress);
//        info.put("lastImportResult", lastImportResult);
//        
//        return ResponseEntity.ok(info);
//    }
//    
//    /**
//     * POST /admin/import/start?maxManga=5
//     * Start import process for specified number of manga
//     * 
//     * Examples:
//     * - Import 5 manga for testing: POST /admin/import/start?maxManga=5
//     * - Import 1000 manga: POST /admin/import/start?maxManga=1000
//     * - Import ALL manga: POST /admin/import/start (defaults to Integer.MAX_VALUE)
//     */
//    @PostMapping("/start")
//    public ResponseEntity<Map<String, String>> startImport(
//            @RequestParam(defaultValue = "2147483647") int maxManga) {
//        
//        if (importInProgress) {
//            return ResponseEntity.status(409) // 409 Conflict
//                .body(Map.of(
//                    "status", "error",
//                    "message", "Import already in progress. Please wait for it to complete."
//                ));
//        }
//        
//        log.info("Starting import for {} manga", maxManga);
//        importInProgress = true;
//        
//        // Run import in background thread so HTTP request doesn't time out
//        CompletableFuture.runAsync(() -> {
//            try {
//                ImportResult result = importer.importManga(maxManga);
//                lastImportResult = result;
//                log.info("Import completed: {}", result);
//            } catch (Exception e) {
//                log.error("Import failed with exception", e);
//            } finally {
//                importInProgress = false;
//            }
//        }, Executors.newSingleThreadExecutor());
//        
//        return ResponseEntity.accepted()
//            .body(Map.of(
//                "status", "accepted",
//                "message", String.format("Import started for %d manga. Check /admin/import/status for progress.", maxManga)
//            ));
//    }
//    
//    @PostMapping("/start-multipass")
//    public ResponseEntity<Map<String, String>> startMultiPassImport() {
//        if (importInProgress) {
//            return ResponseEntity.status(409)
//                .body(Map.of("status", "error", "message", "Import already in progress"));
//        }
//        
//        importInProgress = true;
//        
//        CompletableFuture.runAsync(() -> {
//            try {
//                ImportResult result = importer.importAllInMultiplePasses();
//                lastImportResult = result;
//            } catch (Exception e) {
//                log.error("Multi-pass import failed", e);
//            } finally {
//                importInProgress = false;
//            }
//        });
//        
//        return ResponseEntity.accepted()
//            .body(Map.of("status", "accepted", "message", "Multi-pass import started"));
//    }
//    
//    /**
//     * GET /admin/import/status
//     * Check the status of the current/last import
//     */
//    @GetMapping("/status")
//    public ResponseEntity<Map<String, Object>> getImportStatus() {
//        Map<String, Object> status = new HashMap<>();
//        status.put("inProgress", importInProgress);
//        
//        if (lastImportResult != null) {
//            status.put("lastResult", Map.of(
//                "totalFetched", lastImportResult.getTotalFetched(),
//                "newInserted", lastImportResult.getNewInserted(),
//                "updated", lastImportResult.getUpdated(),
//                "skipped", lastImportResult.getSkipped(),
//                "errors", lastImportResult.getErrors()
//            ));
//        } else {
//            status.put("lastResult", "No import has been run yet");
//        }
//        
//        return ResponseEntity.ok(status);
//    }
//    
//    /**
//     * POST /admin/import/stop
//     * Request to stop the current import (graceful)
//     * Note: Current batch will complete before stopping
//     */
//    @PostMapping("/stop")
//    public ResponseEntity<Map<String, String>> stopImport() {
//        if (!importInProgress) {
//            return ResponseEntity.badRequest()
//                .body(Map.of(
//                    "status", "error",
//                    "message", "No import is currently running"
//                ));
//        }
//        
//        // TODO: Implement interrupt mechanism if needed
//        return ResponseEntity.ok(Map.of(
//            "status", "acknowledged",
//            "message", "Stop request received. Current batch will complete before stopping."
//        ));
//    }
//}
