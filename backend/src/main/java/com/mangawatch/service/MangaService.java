package com.mangawatch.service;

import com.mangawatch.dto.MangaDto;
import com.mangawatch.mapper.MangaMapper;
import com.mangawatch.model.Manga;
import com.mangawatch.repository.MangaRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

// enable logs if needed idk
@Service
public class MangaService {
    private final MangaRepository repo;

    public MangaService(MangaRepository repo) {
        this.repo = repo;
    }
    
    private static final Logger log = LoggerFactory.getLogger(MangaService.class);

    /**
     * search manga with filters and pagination
     */
    public Page<MangaDto> search(String query, String status, List<String> genres, Pageable pageable) {
    	
    	// can also log HttpServletRequest.getQueryString() for some more info?
    	log.info("📥 Incoming /api/manga request: query='{}', status='{}', genres={}, sort='{}', page={}",
                query, status, genres, pageable.getPageNumber());
        
        // treat empty strings as null
        query = (query != null && query.trim().isEmpty()) ? null : query;
        status = (status != null && status.trim().isEmpty()) ? null : status;
        genres = (genres != null && genres.isEmpty()) ? null : genres;
        
        Page<Manga> results;

        if (genres != null && !genres.isEmpty()) {
//            log.info("→ Using searchWithFilters");
            results = repo.searchWithFilters(query, status, genres, pageable);
        } else if (status != null) {
//            log.info("→ Using searchByQueryAndStatus with status: {}", status);
            results = repo.searchByQueryAndStatus(query, status, pageable);
        } else if (query != null) {
//            log.info("→ Using searchByQuery");
            results = repo.searchByQuery(query, pageable);
        } else {
//            log.info("→ Using findAll");
            results = repo.findAll(pageable);
        }

        log.info(" found {} results", results.getTotalElements());
        return results.map(MangaMapper::toDto);
    }

    /**
     * get manga by internal database ID
     */
    public Optional<Manga> getById(Long id) {
        return repo.findById(id);
    }
    
    /**
     * get manga by MangaDex ID (dexId)
     */
    public Optional<Manga> getByDexId(String dexId) {
        return repo.findByDexId(dexId);
    }
    
    /**
     * get total count of manga in database
     */
    public long getTotalCount() {
        return repo.count();
    }
    
    /**
     * get all unique genres available in the manga_genres table
     * @return list of genre names, sorted alphabetically
     */
    public List<String> getAllGenres() {
        return repo.findAllDistinctGenres();
    }
}
