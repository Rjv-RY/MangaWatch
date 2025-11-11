package com.mangawatch.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Response from GET /author/{id}
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthorResponse {
	private String result;
    private String response;
    private AuthorData data;           // For single author response
    private List<AuthorData> dataList; // For batch response (we'll handle this manually)
    private Integer limit;
    private Integer offset;
    private Integer total;

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
    
    public String getResponse() { return response; }
    public void setResponse(String response) { this.response = response; }
    
    public AuthorData getData() { return data; }
    public void setData(AuthorData data) { this.data = data; }
    
    // For batch responses, Jackson will try to deserialize into this
    public List<AuthorData> getDataList() { return dataList; }
    public void setDataList(List<AuthorData> dataList) { this.dataList = dataList; }
    
    public Integer getLimit() { return limit; }
    public void setLimit(Integer limit) { this.limit = limit; }
    
    public Integer getOffset() { return offset; }
    public void setOffset(Integer offset) { this.offset = offset; }
    
    public Integer getTotal() { return total; }
    public void setTotal(Integer total) { this.total = total; }
    
    // ========== NESTED CLASSES - PUBLIC AND STATIC ==========
    
    /**
     * Individual author data
     */
    
    @JsonIgnoreProperties(ignoreUnknown = true)
	public static class AuthorData {
    	private String id;
    	private String type;
    	private AuthorAttributes attributes;
    	
    	public String getId() { return id; }
    	public void setId(String id) { this.id = id; }
    	
    	public String getType() { return type; }
    	public void setType(String type) { this.type = type; }
    	
    	public AuthorAttributes getAttributes() { return attributes; }
    	public void setAttributes(AuthorAttributes attributes) { this.attributes = attributes; }
    }
    
    /**
     * Author attributes
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AuthorAttributes {
    	private String name;
    	private String imageUrl;
    	private String createdAt;
    	private String updatedAt;
    	
    	public String getName() { return name; }
    	public void setName(String name) { this.name = name; }
    	
    	public String getImageUrl() { return imageUrl; }
    	public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    	
    	public String getCreatedAt() { return createdAt; }
    	public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    	
    	public String getUpdatedAt() { return updatedAt; }
    	public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
    }
}

