package com.mangawatch.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.mangawatch.model.Library;
import com.mangawatch.model.Manga;
import com.mangawatch.model.User;
import com.mangawatch.repository.LibraryEntryRepository;
import com.mangawatch.repository.LibraryRepository;
import com.mangawatch.repository.MangaRepository;
import com.mangawatch.repository.UserRepository;
import com.mangawatch.security.JwtUtil;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class LibraryControllerTest {
	@Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private LibraryRepository libraryRepository;
    @Autowired private LibraryEntryRepository libraryEntryRepository;
    @Autowired private MangaRepository mangaRepository;
    @Autowired private JwtUtil jwtUtil;
    
    private User userA;
    private User userB;
    private Manga manga1;
    private Manga manga2;
    
    @BeforeEach
    void setUp() {
        userA = createUserWithLibrary("alice");
        userB = createUserWithLibrary("bob");
        manga1 = createManga("One Bleach", "Kubo Oda");
        manga2 = createManga("666 Naruto", "Seishi Kishimoto");
    }
    
    // helpers time
    private User createUserWithLibrary(String username) {
    	User user = new User();
    	user.setUsername(username);
    	user.setPasswordHash("eminent-domain");
    	user.setEmail(username + "@hotmail.com");
    	user.setDisplayName(username);
    	user.setRole("USER");
    	user.setEnabled(true);
    	user = userRepository.save(user);
    	
    	Library library = new Library();
    	library.setUser(user);
    	libraryRepository.save(library);
    	
    	return user;
    }
    
    private Manga createManga(String title, String author) {
    	Manga manga = new Manga();
    	manga.setTitle(title);
    	manga.setAuthor(author);
    	manga.setStatus("Ongoing");
    	return mangaRepository.save(manga);
    }
    
    private String tokenFor(User user) {
        return "Bearer " + jwtUtil.generateToken(user.getUsername(), user.getId());
    }
    // helpers time over
    
    //actual tests
    @Test
    void getLibrary_withoutToken_isRejected() throws Exception {
        mockMvc.perform(get("/api/library"))
            .andExpect(status().is4xxClientError());
    }
    
    @Test
    void getLibrary_withValidToken_returnsOnlyOwnEntries() throws Exception {
        //manga1 to alice's library, manga2 to bob's
        mockMvc.perform(post("/api/library/add/{id}", manga1.getId())
                .header("Authorization", tokenFor(userA)))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/library/add/{id}", manga2.getId())
                .header("Authorization", tokenFor(userB)))
            .andExpect(status().isOk());

        // each user see only their own entry
        mockMvc.perform(get("/api/library")
                .header("Authorization", tokenFor(userA)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].manga.title").value("One Bleach"));
        mockMvc.perform(get("/api/library")
                .header("Authorization", tokenFor(userB)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].manga.title").value("666 Naruto"));
    }
    
    @Test
    void addMangaToLibrary_andthenGet_returnsEntry() throws Exception {
        mockMvc.perform(post("/api/library/add/{id}", manga1.getId())
                .header("Authorization", tokenFor(userA)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Manga added to library"));

        List<?> entries = libraryEntryRepository.findByLibrary(
            libraryRepository.findByUser(userA).orElseThrow()
        );
        assertThat(entries).hasSize(1);
    }
    
    @Test
    void addMangaToLibrary_duplicate_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/library/add/{id}", manga1.getId())
                .header("Authorization", tokenFor(userA)))
            .andExpect(status().isOk());

        // same manga added again should be rejected
        mockMvc.perform(post("/api/library/add/{id}", manga1.getId())
                .header("Authorization", tokenFor(userA)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Manga already in library"));
    }
    
    @Test
    void removeMangaFromLibrary_wellYouKNow() throws Exception {
        mockMvc.perform(post("/api/library/add/{id}", manga1.getId())
                .header("Authorization", tokenFor(userA)))
            .andExpect(status().isOk());

        mockMvc.perform(delete("/api/library/remove/{id}", manga1.getId())
                .header("Authorization", tokenFor(userA)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Manga removed from library"));

        mockMvc.perform(get("/api/library")
                .header("Authorization", tokenFor(userA)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(0)));
    }
    
    @Test
    void updateReadingStatus_updatesTheEntry() throws Exception {
        mockMvc.perform(post("/api/library/add/{id}", manga1.getId())
                .header("Authorization", tokenFor(userA)))
            .andExpect(status().isOk());

        mockMvc.perform(patch("/api/library/update-status/{id}", manga1.getId())
                .header("Authorization", tokenFor(userA))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"readingStatus\": \"Completed\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.readingStatus").value("Completed"));
    }
    
    @Test
    void bobCannotUpdateAlicesEntry() throws Exception {
        // userA adds manga1
        mockMvc.perform(post("/api/library/add/{id}", manga1.getId())
                .header("Authorization", tokenFor(userA)))
            .andExpect(status().isOk());

        //Bob never added One Bleach to his Library, so updating it should fail
        mockMvc.perform(patch("/api/library/update-status/{id}", manga1.getId())
                .header("Authorization", tokenFor(userB))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"readingStatus\": \"Completed\"}"))
            .andExpect(status().is4xxClientError());
    }
}


