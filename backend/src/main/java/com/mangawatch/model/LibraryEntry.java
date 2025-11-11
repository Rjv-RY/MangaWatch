package com.mangawatch.model;

//Files for Library

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "library_entry")
public class LibraryEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "library_id", nullable = false)
    @JsonBackReference
    private Library library;

    @ManyToOne
    @JoinColumn(name = "manga_id", nullable = false)
    private Manga manga;

    private String readingStatus = "Plan to Read";
    private Integer rating;
    private String review;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    //gettieros & settieros
    
	public Long getId() {return id;}
	public void setId(Long id) {this.id = id;}
	
	public Library getLibrary() {return library;}
	public void setLibrary(Library library) {this.library = library;}
	
	public Manga getManga() {return manga;}
	public void setManga(Manga manga) {this.manga = manga;}
	
	public String getReadingStatus() {return readingStatus;}
	public void setReadingStatus(String readingStatus) {this.readingStatus = readingStatus;}
	
	public Integer getRating() {return rating;}
	public void setRating(Integer rating) {this.rating = rating;}
	
	public String getReview() {return review;}
	public void setReview(String review) {this.review = review;}
	
	public LocalDateTime getCreatedAt() {return createdAt;}
	public void setCreatedAt(LocalDateTime createdAt) {this.createdAt = createdAt;}
	
	public LocalDateTime getUpdatedAt() {return updatedAt;}
	public void setUpdatedAt(LocalDateTime updatedAt) {this.updatedAt = updatedAt;}
}