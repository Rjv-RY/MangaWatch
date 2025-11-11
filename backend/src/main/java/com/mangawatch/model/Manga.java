package com.mangawatch.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "manga")
public class Manga {
		@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long id;
		private String dexId;
	    private String title;
	    private String author;
	    
	    @Column(name = "release_year")
	    private Integer releaseYear;
	    

		private String status; // "Ongoing", "Completed", "Hiatus", etc.
	    private Double rating;
	    
	    @Column(length = 2000)
	    private String description;
	    
	    private String coverUrl;
	    
	    @ElementCollection(fetch = FetchType.EAGER)
	    @CollectionTable(name = "manga_alt_titles", joinColumns = @JoinColumn(name = "manga_id"))
	    @Column(name = "alt_title", columnDefinition = "TEXT")  // <-- force Hibernate to match DB column
	    private List<String> altTitles;

	    @ElementCollection(fetch = FetchType.EAGER)
	    @CollectionTable(name = "manga_genres", joinColumns = @JoinColumn(name = "manga_id"))
	    @Column(name = "genre")
	    private List<String> genres;

	    // getters & setters
	    public Manga() {}

		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public String getDexId() {
			return dexId;
		}
		
		public void setDexId(String dexId) {
			this.dexId = dexId;
		}
		
		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getAuthor() {
			return author;
		}

		public void setAuthor(String author) {
			this.author = author;
		}

		public Integer getYear() {
			return releaseYear;
		}

		public void setYear(Integer year) {
			this.releaseYear = year;
		}

		public String getStatus() {
			return status;
		}

		public void setStatus(String status) {
			this.status = status;
		}

		public Double getRating() {
			return rating;
		}

		public void setRating(Double rating) {
			this.rating = rating;
		}

		public String getDescription() {
			return description;
		}

		public List<String> getAltTitles() {
			return altTitles;
		}

		public void setAltTitles(List<String> altTitles) {
			this.altTitles = altTitles;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public String getCoverUrl() {
			return coverUrl;
		}

		public void setCoverUrl(String coverUrl) {
			this.coverUrl = coverUrl;
		}

		public List<String> getGenres() {
			return genres;
		}

		public void setGenres(List<String> genres) {
			this.genres = genres;
		}
}
