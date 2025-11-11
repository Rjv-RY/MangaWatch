package com.mangawatch.service;

import com.mangawatch.dto.LoginRequest;
import com.mangawatch.dto.LoginResponse;

//Files for Login/Register

import com.mangawatch.dto.RegisterRequest;
import com.mangawatch.model.Library;
import com.mangawatch.model.User;
import com.mangawatch.repository.LibraryRepository;
import com.mangawatch.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.util.Optional;

@Service
public class AuthService {
	
    private final UserRepository userRepository;
    private final LibraryRepository libraryRepository;
    // cost factor 11 is fine for dev; increase in prod if needed
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(11);
    
    public AuthService(UserRepository userRepository, LibraryRepository libraryRepository) {
        this.userRepository = userRepository;
        this.libraryRepository = libraryRepository;
    }
    
//    //get username
//    String username = SecurityContextHolder.getContext().getAuthentication().getName();
//    //Get user
//    User user = userRepository.findByUsername(username)
//    	    .orElseThrow(() -> new RuntimeException("User not found"));
	
    public User register(RegisterRequest req) {
        // basic validations for now, will expand later
        if (req.getUsername() == null || req.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("username is required");
        }
        if (req.getPassword() == null || req.getPassword().length() < 6) {
            throw new IllegalArgumentException("password must be at least 6 characters");
        }
        if (req.getEmail() == null || req.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("email is required");
        }

        if (userRepository.existsByUsername(req.getUsername())) {
            throw new IllegalArgumentException("username already taken");
        }
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("email already used");
        }

        User user = new User();
        user.setUsername(req.getUsername().trim());
        user.setEmail(req.getEmail().trim().toLowerCase());
        user.setDisplayName(req.getDisplayName());
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        user.setRole("USER");
        user.setEnabled(true);
        
        User savedUser = userRepository.save(user);
        
        Library library = new Library();
        library.setUser(savedUser);
        libraryRepository.save(library);

        return savedUser;
    }
    
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new RuntimeException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid username or password");
        }

        return new LoginResponse("Login successful");
    }
}
