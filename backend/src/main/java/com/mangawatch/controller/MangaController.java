package com.mangawatch.controller;

import com.mangawatch.dto.MangaDto;
import com.mangawatch.mapper.MangaMapper;
import com.mangawatch.model.Manga;
import com.mangawatch.service.MangaService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/manga")
@CrossOrigin(origins = "http://localhost:5173")
public class MangaController {
    private final MangaService service;
    
    private static final Logger log = LoggerFactory.getLogger(MangaService.class);
    
    public MangaController(MangaService service) {
        this.service = service;
    }

    /**
     * GET /api/manga/{id}
     * Get a single manga by database ID
     */
    @GetMapping("/{id}")
    public MangaDto getMangaById(@PathVariable Long id) {
        return service.getById(id)
            .map(MangaMapper::toDto)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, 
                "Manga not found with id: " + id
            ));
    }
    
    /**
     * GET /api/manga/dex/{dexId}
     * Get a single manga by MangaDex ID
     */
    @GetMapping("/dex/{dexId}")
    public MangaDto getMangaByDexId(@PathVariable String dexId) {
        return service.getByDexId(dexId)
            .map(MangaMapper::toDto)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, 
                "Manga not found with dexId: " + dexId
            ));
    }

    /**
     * GET /api/manga
     * List/search manga with filters and pagination
     * 
     * Example: /api/manga?query=naruto&status=Completed&genres=Action&page=0&size=20
     */
    @GetMapping
    public Page<MangaDto> list(
        @RequestParam(required = false) String query,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) List<String> genres,
        @RequestParam(required = false, defaultValue = "title,asc") String sort,
        Pageable pageable
    ) {
    	
    	log.info("📥 Incoming /api/manga request: query='{}', status='{}', genres={}, sort='{}', page={}",
                query, status, genres, sort, pageable.getPageNumber());
    	// Parse and fix sort parameter
        String[] sortParts = sort.split(",");
        String sortField = sortParts[0];
        String sortDir = sortParts.length > 1 ? sortParts[1] : "asc";
        
        // Map "year" to actual column name for native queries
        if ("year".equals(sortField)) {
            sortField = "releaseYear";
        }
        
        log.debug("🧭 Normalized sort field='{}', direction='{}'", sortField, sortDir);
        
        // Rebuild pageable with corrected sort
        Sort sortObj = Sort.by(
            "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC,
            sortField
        );
        pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sortObj);
        log.info("✅ /api/manga completed: returned {} items", service.search(query, status, genres, pageable));
        return service.search(query, status, genres, pageable);
    }
    
    /**
     * GET /api/manga/stats
     * Get database statistics
     */
    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        return Map.of(
            "totalManga", service.getTotalCount(),
            "message", "Manga database statistics"
        );
    }
    
    /**
     * GET /api/manga/genres
     * Get all unique genres available in the database
     */
    @GetMapping("/genres")
    public List<String> getAllGenres() {
        return service.getAllGenres();
    }
}
