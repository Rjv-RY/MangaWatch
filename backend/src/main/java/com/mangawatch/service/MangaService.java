package com.mangawatch.service;

import com.mangawatch.dto.MangaDto;
import com.mangawatch.mapper.MangaMapper;
import com.mangawatch.model.Manga;
import com.mangawatch.repository.MangaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

// enable logs if needed idk
@Service
public class MangaService {
    private final MangaRepository repo;

    public MangaService(MangaRepository repo) {
        this.repo = repo;
    }
    
    private static final Logger log = LoggerFactory.getLogger(MangaService.class);
    
    private static final Map<String, String> ENTITY_SORT_MAP = Map.of(
    	    "year", "releaseYear",
    	    "title", "title",
    	    "author", "author"
    	);

    	private static final Map<String, String> COLUMN_SORT_MAP = Map.of(
    	    "year", "release_year",
    	    "title", "title",
    	    "author", "author"
    	);

    /**
     * search manga with filters and pagination
     */
    public Page<MangaDto> search(String query, String status, List<String> genres, String sort, Pageable pageable) {
    	
        boolean useNative =
                (genres != null && !genres.isEmpty()) ||
                (status != null && !status.isBlank()) ||
                (query != null && !query.isBlank());
    	
    	// can also log HttpServletRequest.getQueryString() for some more info?
    	log.info("Incoming /api/manga request: query='{}', status='{}', genres={}, sort='{}', page={}",
                query, status, genres, pageable.getPageNumber());
        
        String sortField = "title";
        Sort.Direction sortDir = Sort.Direction.ASC;

        if (sort != null && !sort.isBlank()) {
            String[] parts = sort.split(",");
            sortField = parts[0];
            if (parts.length > 1 && "desc".equalsIgnoreCase(parts[1])) {
                sortDir = Sort.Direction.DESC;
            }
        }
        
        sortField = useNative
                ? COLUMN_SORT_MAP.getOrDefault(sortField, "title")
                : ENTITY_SORT_MAP.getOrDefault(sortField, "title");

        // frontend -> db column mapping (NOT entity fields)
//        switch (sortField) {
//            case "year" -> sortField = "release_year";
//            case "title" -> sortField = "title";
//            case "author" -> sortField = "author";
//            default -> sortField = "title"; // safety fallback
//        }

        // Rebuild pageable with DB-safe sort
        pageable = PageRequest.of(
            pageable.getPageNumber(),
            pageable.getPageSize(),
            Sort.by(sortDir, sortField)
        );
    	
        // treat empty strings as null
        query = (query != null && query.trim().isEmpty()) ? null : query;
        status = (status != null && status.trim().isEmpty()) ? null : status;
        genres = (genres != null && genres.isEmpty()) ? null : genres;
        
        Page<Manga> results;

        if (useNative && genres != null && !genres.isEmpty()) {
            results = repo.searchWithFilters(query, status, genres, pageable);
        } else if (useNative && status != null) {
            results = repo.searchByQueryAndStatus(query, status, pageable);
        } else if (useNative && query != null) {
            results = repo.searchByQuery(query, pageable);
        } else {
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
    @Cacheable("genres")
    public List<String> getAllGenres() {
        return repo.findAllDistinctGenres();
    }
}
