package com.mangawatch.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

/**
 * Top-level response from MangaDex /manga endpoint
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MangadexResponse {
    private String result;
    private String response;
    private List<MangadexManga> data;
    private Integer limit;
    private Integer offset;
    private Integer total;

    // Getters and setters
    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
    
    public String getResponse() { return response; }
    public void setResponse(String response) { this.response = response; }
    
    public List<MangadexManga> getData() { return data; }
    public void setData(List<MangadexManga> data) { this.data = data; }
    
    public Integer getLimit() { return limit; }
    public void setLimit(Integer limit) { this.limit = limit; }
    
    public Integer getOffset() { return offset; }
    public void setOffset(Integer offset) { this.offset = offset; }
    
    public Integer getTotal() { return total; }
    public void setTotal(Integer total) { this.total = total; }
    
    // ========== NESTED CLASSES - ALL PUBLIC STATIC ==========
    
    /**
     * Individual manga item from MangaDex
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MangadexManga {
        private String id;
        private String type;
        private MangadexAttributes attributes;
        private List<MangadexRelationship> relationships;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        
        public MangadexAttributes getAttributes() { return attributes; }
        public void setAttributes(MangadexAttributes attributes) { this.attributes = attributes; }
        
        public List<MangadexRelationship> getRelationships() { return relationships; }
        public void setRelationships(List<MangadexRelationship> relationships) { 
            this.relationships = relationships; 
        }
    }

    /**
     * Attributes nested object containing manga details
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MangadexAttributes {
        private Map<String, String> title;
        private List<Map<String, String>> altTitles;
        private Map<String, String> description;
        private String originalLanguage;
        private String status;
        private Integer year;
        private String contentRating;
        private List<MangadexTag> tags;
        private String createdAt;
        private String updatedAt;

        public Map<String, String> getTitle() { return title; }
        public void setTitle(Map<String, String> title) { this.title = title; }
        
        public List<Map<String, String>> getAltTitles() { return altTitles; }
        public void setAltTitles(List<Map<String, String>> altTitles) { this.altTitles = altTitles; }
        
        public Map<String, String> getDescription() { return description; }
        public void setDescription(Map<String, String> description) { this.description = description; }
        
        public String getOriginalLanguage() { return originalLanguage; }
        public void setOriginalLanguage(String originalLanguage) { 
            this.originalLanguage = originalLanguage; 
        }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public Integer getYear() { return year; }
        public void setYear(Integer year) { this.year = year; }
        
        public String getContentRating() { return contentRating; }
        public void setContentRating(String contentRating) { this.contentRating = contentRating; }
        
        public List<MangadexTag> getTags() { return tags; }
        public void setTags(List<MangadexTag> tags) { this.tags = tags; }
        
        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
        
        public String getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
    }

    /**
     * Tag object (genres, themes, formats)
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MangadexTag {
        private String id;
        private String type;
        private MangadexTagAttributes attributes;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        
        public MangadexTagAttributes getAttributes() { return attributes; }
        public void setAttributes(MangadexTagAttributes attributes) { this.attributes = attributes; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MangadexTagAttributes {
        private Map<String, String> name;
        private String group;

        public Map<String, String> getName() { return name; }
        public void setName(Map<String, String> name) { this.name = name; }
        
        public String getGroup() { return group; }
        public void setGroup(String group) { this.group = group; }
    }

    /**
     * Relationship object (author, artist, cover_art references)
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MangadexRelationship {
        private String id;
        private String type;
        private String related;
        private RelationshipAttributes attributes; // NEW - for cover_art filename

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        
        public String getRelated() { return related; }
        public void setRelated(String related) { this.related = related; }
        
        public RelationshipAttributes getAttributes() { return attributes; }
        public void setAttributes(RelationshipAttributes attributes) { this.attributes = attributes; }
    }
    
    /**
     * Attributes that can be included in relationships (like cover_art)
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RelationshipAttributes {
        private String fileName;
        private String description;
        private String volume;
        
        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getVolume() { return volume; }
        public void setVolume(String volume) { this.volume = volume; }
    }
}
