package com.mangawatch.service;

import org.springframework.stereotype.Service;
import com.mangawatch.dto.MangadexResponse;
import com.mangawatch.model.Manga;
import java.util.*;
import java.util.stream.Collectors;

/**
 * transform MangaDex API response objects into our manga entities.
 * this is where we control which fields get mapped and how.
 */
@Service
public class MangadexTransformer {

    /**
     * transform a single MangaDex manga item into our Manga entity.
     * 
     * @param dexManga The MangaDex manga data
     * @param authorName The author name (fetched separately), or null if unavailable
     * @param coverFileName The cover filename from relationships
     * 
     * To add a new field:
     * 1. Add the field to Manga entity
     * 2. Add corresponding Flyway migration
     * 3. Extract the value here from the DTO
     * 4. Set it on the manga object
     */
    public Manga transform(MangadexResponse.MangadexManga dexManga, String authorName, String coverFileName) {
        Manga manga = new Manga();
        
        // ID mapping. This is our unique identifier from MangaDex
        manga.setDexId(dexManga.getId());
        
        var attrs = dexManga.getAttributes();
        if (attrs == null) {
            return manga; // Return minimal manga if attributes missing
        }
        
        // title. prefer english, fallback to first available
        manga.setTitle(extractTitle(attrs.getTitle()));
        
        // author. use provided author name, fallback to "Unknown"
        manga.setAuthor(authorName != null && !authorName.isBlank() ? authorName : "Unknown");
        
        // year or release
        manga.setYear(attrs.getYear());
        
        // status, capitalize first letter to match format
        manga.setStatus(capitalizeStatus(attrs.getStatus()));
        
        // rating, always null for now (will calculate this later from user reviews)
        manga.setRating(null);
        
        // description, prefer English
        manga.setDescription(extractDescription(attrs.getDescription()));
        
        // cover url, construct from MangaDex CDN if we have the filename
        manga.setCoverUrl(buildCoverUrl(dexManga.getId(), coverFileName));
        
        // alt titles, extract all available translations
        manga.setAltTitles(extractAltTitles(attrs.getAltTitles()));
        
        // genres, extract tag names from all tags (genres, themes, formats combined)
        manga.setGenres(extractGenres(attrs.getTags()));
        
        return manga;
    }
    
    /**
     * build MangaDex cover url from manga ID and cover filename
     * format= https://uploads.mangadex.org/covers/{mangaId}/{coverFileName}
     * 
     * returns null if coverFileName is missing
     */
    private String buildCoverUrl(String mangaId, String coverFileName) {
        if (coverFileName == null || coverFileName.isBlank()) {
            return null;
        }
        return String.format("https://uploads.mangadex.org/covers/%s/%s", mangaId, coverFileName);
    }
    
    /**
     * extrac title, preferring English, fallback to first available language
     */
    private String extractTitle(Map<String, String> titleMap) {
        if (titleMap == null || titleMap.isEmpty()) {
            return "Unknown Title";
        }
        
        // try eng first
        if (titleMap.containsKey("en")) {
            return titleMap.get("en");
        }
        
        // try romanized ver
        if (titleMap.containsKey("ja-ro")) {
            return titleMap.get("ja-ro");
        }
        
        // return first available
        return titleMap.values().iterator().next();
    }
    
    /**
     * extracst description, prefer English
     */
    private String extractDescription(Map<String, String> descMap) {
        if (descMap == null || descMap.isEmpty()) {
            return "";
        }
        
        if (descMap.containsKey("en")) {
            return descMap.get("en");
        }
        
        // return first avb or empty
        return descMap.values().stream().findFirst().orElse("");
    }
    
    /**
     *extract all alternative titles from all languages (what if someone searched?)
     */
    private List<String> extractAltTitles(List<Map<String, String>> altTitlesList) {
        if (altTitlesList == null || altTitlesList.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<String> titles = new ArrayList<>();
        
        for (Map<String, String> titleMap : altTitlesList) {
            if (titleMap != null) {
                titles.addAll(titleMap.values());
            }
        }
        
        return titles;
    }
    
    /**
     * extract genre/tag names from all tags (genres, themes, formats)
     */
    private List<String> extractGenres(List<MangadexResponse.MangadexTag> tags) {
        if (tags == null || tags.isEmpty()) {
            return new ArrayList<>();
        }
        
        return tags.stream()
            .filter(tag -> tag.getAttributes() != null)
            .filter(tag -> tag.getAttributes().getName() != null)
            .map(tag -> {
                Map<String, String> nameMap = tag.getAttributes().getName();
                // Prefer English tag name
                return nameMap.getOrDefault("en", nameMap.values().iterator().next());
            })
            .collect(Collectors.toList());
    }
    
    /**
     * eapitalize status to match your format (ongoing -> Ongoing)
     */
    private String capitalizeStatus(String status) {
        if (status == null || status.isEmpty()) {
            return "Unknown";
        }
        return status.substring(0, 1).toUpperCase() + status.substring(1).toLowerCase();
    }
}