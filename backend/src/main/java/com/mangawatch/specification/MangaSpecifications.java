package com.mangawatch.specification;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.mangawatch.model.Manga;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

public class MangaSpecifications {
	public static Specification<Manga> hasStatus(String status){
		return (root, query, cb) ->
			status == null || status.isBlank()
				? cb.conjunction()
				: cb.equal(root.get("status"), status);
	}
	
	public static Specification<Manga> titleOrAuthorContains(String queryStr){
		return (root, query, cb) ->
			(queryStr == null || queryStr.isBlank())
				? cb.conjunction()
				: cb.or(
					cb.like(cb.lower(root.get("title")), "%" + queryStr.toLowerCase() + "%"),	
					cb.like(cb.lower(root.get("author")), "%" + queryStr.toLowerCase() + "%")
				);
	}
	
	public static Specification<Manga> hasAllGenres(List<String> genres){
		return (root, query, cb) -> {
			if(genres == null || genres.isEmpty()) {
				return cb.conjunction();
			}
			
	        // subquery: select count(distinct g) from manga_genres where manga_id = this.id and genre in :genres
	        Subquery<Long> subquery = query.subquery(Long.class);
	        Root<Manga> subRoot = subquery.from(Manga.class);
	        Join<Manga, String> subGenres = subRoot.join("genres");

	        subquery.select(cb.countDistinct(subGenres))
	                .where(
	                    cb.equal(subRoot.get("id"), root.get("id")),
	                    subGenres.in(genres)
	                );

	        return cb.equal(subquery, (long) genres.size());
	    };
	}
}