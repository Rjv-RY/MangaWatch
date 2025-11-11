package com.mangawatch.repository;

import com.mangawatch.model.Manga;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MangaRepository extends JpaRepository<Manga, Long>, JpaSpecificationExecutor<Manga>{
	
    /**
     * find manga by Mangadex ID (unique identifier from MangaDex API)
     */
    Optional<Manga> findByDexId(String dexId);
    

    /**
     * Search manga by title, author, OR alternate titles
     * Using native SQL to avoid Hibernate type confusion
     */
    @Query(value = "SELECT DISTINCT m.* FROM manga m " +
                   "LEFT JOIN manga_alt_titles alt ON m.id = alt.manga_id " +
                   "WHERE LOWER(m.title) LIKE LOWER(CONCAT('%', :query, '%')) " +
                   "OR LOWER(m.author) LIKE LOWER(CONCAT('%', :query, '%')) " +
                   "OR LOWER(CAST(alt.alt_title AS TEXT)) LIKE LOWER(CONCAT('%', :query, '%'))",
           countQuery = "SELECT COUNT(DISTINCT m.id) FROM manga m " +
                        "LEFT JOIN manga_alt_titles alt ON m.id = alt.manga_id " +
                        "WHERE LOWER(m.title) LIKE LOWER(CONCAT('%', :query, '%')) " +
                        "OR LOWER(m.author) LIKE LOWER(CONCAT('%', :query, '%')) " +
                        "OR LOWER(CAST(alt.alt_title AS TEXT)) LIKE LOWER(CONCAT('%', :query, '%'))",
           nativeQuery = true)
    Page<Manga> searchByQuery(@Param("query") String query, Pageable pageable);

    /**
     * Search with status filter
     * Using native SQL to avoid Hibernate type confusion
     */
    @Query(value = "SELECT DISTINCT m.* FROM manga m " +
                   "LEFT JOIN manga_alt_titles alt ON m.id = alt.manga_id " +
                   "WHERE (:query IS NULL OR " +
                   "LOWER(m.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
                   "LOWER(m.author) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
                   "LOWER(CAST(alt.alt_title AS TEXT)) LIKE LOWER(CONCAT('%', :query, '%'))) " +
                   "AND m.status = :status",
           countQuery = "SELECT COUNT(DISTINCT m.id) FROM manga m " +
                        "LEFT JOIN manga_alt_titles alt ON m.id = alt.manga_id " +
                        "WHERE (:query IS NULL OR " +
                        "LOWER(m.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
                        "LOWER(m.author) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
                        "LOWER(CAST(alt.alt_title AS TEXT)) LIKE LOWER(CONCAT('%', :query, '%'))) " +
                        "AND m.status = :status",
           nativeQuery = true)
    Page<Manga> searchByQueryAndStatus(
        @Param("query") String query,
        @Param("status") String status,
        Pageable pageable
    );

    /**
     * Full search with all filters
     * Using native SQL to avoid Hibernate type confusion
     */
    @Query(value = "SELECT DISTINCT m.* FROM manga m " +
                   "LEFT JOIN manga_alt_titles alt ON m.id = alt.manga_id " +
                   "JOIN manga_genres g ON m.id = g.manga_id " +
                   "WHERE (:query IS NULL OR " +
                   "LOWER(m.title) LIKE LOWER('%' || :query || '%') OR " +
                   "LOWER(m.author) LIKE LOWER('%' || :query || '%') OR " +
                   "LOWER(CAST(alt.alt_title AS TEXT)) LIKE LOWER('%' || :query || '%')) " +
                   "AND (:status IS NULL OR m.status = :status) " +
                   "AND g.genre IN (:genres)",
           countQuery = "SELECT COUNT(DISTINCT m.id) FROM manga m " +
                        "LEFT JOIN manga_alt_titles alt ON m.id = alt.manga_id " +
                        "JOIN manga_genres g ON m.id = g.manga_id " +
                        "WHERE (:query IS NULL OR " +
                        "LOWER(m.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
                        "LOWER(m.author) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
                        "LOWER(CAST(alt.alt_title AS TEXT)) LIKE LOWER(CONCAT('%', :query, '%'))) " +
                        "AND (:status IS NULL OR m.status = :status) " +
                        "AND g.genre IN (:genres)",
           nativeQuery = true)
    Page<Manga> searchWithFilters(
        @Param("query") String query,
        @Param("status") String status,
        @Param("genres") List<String> genres,
        Pageable pageable
    );
    
    /**
     * Get all unique genres from the database
     */
    @Query("SELECT DISTINCT g FROM Manga m JOIN m.genres g ORDER BY g")
    List<String> findAllDistinctGenres();
    
    /**
     * Count total manga
     */
    long count();
}
