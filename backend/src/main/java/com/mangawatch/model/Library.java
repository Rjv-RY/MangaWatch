package com.mangawatch.model;

//Files For Library

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "library")
public class Library {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
	@OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @OneToMany(mappedBy = "library", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<LibraryEntry> entries = new ArrayList<>();

    private LocalDateTime createdAt = LocalDateTime.now();

    // Getters/setters

    public Long getId() {return id;}
	public void setId(Long id) {this.id = id;}

	public User getUser() {return user;}
	public void setUser(User user) {this.user = user;}

	public List<LibraryEntry> getEntries() {return entries;}
	public void setEntries(List<LibraryEntry> entries) {this.entries = entries;}

	public LocalDateTime getCreatedAt() {return createdAt;}
	public void setCreatedAt(LocalDateTime createdAt) {this.createdAt = createdAt;}
}