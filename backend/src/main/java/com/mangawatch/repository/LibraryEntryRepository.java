package com.mangawatch.repository;

//Files for Library

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import com.mangawatch.model.Library;
import com.mangawatch.model.LibraryEntry;
import com.mangawatch.model.Manga;

public interface LibraryEntryRepository extends JpaRepository<LibraryEntry, Long> {

	List<LibraryEntry> findByLibrary(Library library);

    Optional<LibraryEntry> findByLibraryAndManga(Library library, Manga manga);
    
    boolean existsByLibraryAndManga(Library library, Manga manga);
    
    @Transactional
    void deleteByLibraryAndManga(Library library, Manga manga);
    
    @Transactional
    void deleteByLibraryIdAndMangaId(Long libraryId, Long mangaId);
}
