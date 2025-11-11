package com.mangawatch.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mangawatch.model.Library;
import com.mangawatch.model.LibraryEntry;
import com.mangawatch.model.Manga;
import com.mangawatch.model.User;
import com.mangawatch.repository.LibraryEntryRepository;
import com.mangawatch.repository.LibraryRepository;
import com.mangawatch.repository.MangaRepository;
import com.mangawatch.repository.UserRepository;
import com.mangawatch.service.LibraryService;

// Files for Library

@RestController
@RequestMapping("/api/library")
public class LibraryController {
	private final UserRepository userRepository;
	private final LibraryRepository libraryRepository;
	private final LibraryEntryRepository libraryEntryRepository;
	private final MangaRepository mangaRepository;
	private final LibraryService libraryService;
	
	public LibraryController(UserRepository userRepository,
							LibraryRepository libraryRepository,
							LibraryEntryRepository libraryEntryRepository,
							MangaRepository mangaRepository,
							LibraryService libraryService) {
	this.userRepository = userRepository;
	this.libraryRepository = libraryRepository;
	this.libraryEntryRepository = libraryEntryRepository;
	this.mangaRepository = mangaRepository;
	this.libraryService = libraryService;
	}

	@GetMapping
	public ResponseEntity<?> getUserLibrary(){
		//username from jwt context
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
//		System.out.println("JWT username: " + username);
		//user
		User user = userRepository.findByUsername(username)
	    	    .orElseThrow(() -> new RuntimeException("User not found"));
		//library
		Library library = libraryRepository.findByUser(user)
				.orElseThrow(() -> new RuntimeException("Library Not Found"));
		//fetch entries
		List<LibraryEntry> entries = libraryEntryRepository.findByLibrary(library);
		//return them
		return ResponseEntity.ok(entries);
	}
	
	@PostMapping("/add/{mangaId}")
	public ResponseEntity<?> addMangaToLibrary(@PathVariable Long mangaId) {
	    String username = SecurityContextHolder.getContext().getAuthentication().getName();
	    User user = userRepository.findByUsername(username)
	        .orElseThrow(() -> new RuntimeException("User not found"));

	    Library library = libraryRepository.findByUser(user)
	        .orElseThrow(() -> new RuntimeException("Library not found"));

	    Manga manga = mangaRepository.findById(mangaId)
	        .orElseThrow(() -> new RuntimeException("Manga not found"));

	    // Check if entry already exists
	    if (libraryEntryRepository.existsByLibraryAndManga(library, manga)) {
	        return ResponseEntity.badRequest().body(Map.of("message", "Manga already in library"));
	    }

	    LibraryEntry entry = new LibraryEntry();
	    entry.setLibrary(library);
	    entry.setManga(manga);
	    entry.setCreatedAt(LocalDateTime.now());
	    libraryEntryRepository.save(entry);

	    return ResponseEntity.ok(Map.of("message", "Manga added to library"));
	}
	
	@DeleteMapping("/remove/{mangaId}")
	public ResponseEntity<?> removeMangaFromLibrary(@PathVariable Long mangaId) {
	    String username = SecurityContextHolder.getContext().getAuthentication().getName();
	    User user = userRepository.findByUsername(username)
	        .orElseThrow(() -> new RuntimeException("User not found"));

	    Library library = libraryRepository.findByUser(user)
	        .orElseThrow(() -> new RuntimeException("Library not found"));
	    Manga manga = mangaRepository.findById(mangaId)
	        .orElseThrow(() -> new RuntimeException("Manga not found"));
	    libraryEntryRepository.deleteByLibraryAndManga(library, manga);
	    return ResponseEntity.ok(Map.of("message", "Manga removed from library"));
	}
	
	@PatchMapping("/update-status/{mangaId}")
	public ResponseEntity<?> updateReadingStatus(
	        @PathVariable Long mangaId,
	        @RequestBody Map<String, String> body,
	        @AuthenticationPrincipal UserDetails userDetails) {

	    String newStatus = body.get("readingStatus");

	    User user = userRepository.findByUsername(userDetails.getUsername())
	            .orElseThrow(() -> new RuntimeException("User not found"));

	    LibraryEntry updatedEntry = libraryService.updateReadingStatus(user, mangaId, newStatus);
	    return ResponseEntity.ok(updatedEntry);
	}
}
