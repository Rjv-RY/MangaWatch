package com.mangawatch.service;

import com.mangawatch.model.*;
import com.mangawatch.repository.*;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class LibraryService {

    private final LibraryRepository libraryRepository;
    private final LibraryEntryRepository libraryEntryRepository;
    private final MangaRepository mangaRepository;
    
    public LibraryService(LibraryRepository libraryRepository,
                          LibraryEntryRepository libraryEntryRepository,
                          MangaRepository mangaRepository) {
        this.libraryRepository = libraryRepository;
        this.libraryEntryRepository = libraryEntryRepository;
        this.mangaRepository = mangaRepository;
    }

    public Library getLibraryByUser(User user) {
        return libraryRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Library not found for user " + user.getUsername()));
    }

    public List<LibraryEntry> getEntries(User user) {
        return libraryEntryRepository.findByLibrary(getLibraryByUser(user));
    }

    public LibraryEntry addMangaToLibrary(User user, Long mangaId) {
        Library library = getLibraryByUser(user);
        Manga manga = mangaRepository.findById(mangaId)
                .orElseThrow(() -> new RuntimeException("Manga not found"));

        if (libraryEntryRepository.findByLibraryAndManga(library, manga).isPresent()) {
            throw new RuntimeException("Manga already in library");
        }

        LibraryEntry entry = new LibraryEntry();
        entry.setLibrary(library);
        entry.setManga(manga);
        return libraryEntryRepository.save(entry);
    }
    
    @Transactional
    public void removeMangaFromLibrary(User user, Long mangaId) {
        Library library = getLibraryByUser(user);
        Manga manga = mangaRepository.findById(mangaId)
                .orElseThrow(() -> new RuntimeException("Manga not found"));

        LibraryEntry entry = libraryEntryRepository.findByLibraryAndManga(library, manga)
                .orElseThrow(() -> new RuntimeException("Entry not found"));
        libraryEntryRepository.delete(entry);
    }
    
    @Transactional
    public LibraryEntry updateReadingStatus(User user, Long mangaId, String newStatus) {
        Library library = getLibraryByUser(user);
        Manga manga = mangaRepository.findById(mangaId)
                .orElseThrow(() -> new RuntimeException("Manga not found"));

        LibraryEntry entry = libraryEntryRepository.findByLibraryAndManga(library, manga)
                .orElseThrow(() -> new RuntimeException("Entry not found"));

        entry.setReadingStatus(newStatus);
        entry.setUpdatedAt(LocalDateTime.now());
        return libraryEntryRepository.save(entry);
    }
}
