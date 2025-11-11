package com.mangawatch.repository;

//Files for Library

import com.mangawatch.model.Library;
import com.mangawatch.model.User;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface LibraryRepository extends JpaRepository<Library, Long> {
	Optional<Library> findByUser(User user);
    Optional<Library> findByUserId(Long userId);
}
