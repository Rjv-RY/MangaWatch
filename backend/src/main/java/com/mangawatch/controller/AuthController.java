package com.mangawatch.controller;

import com.mangawatch.dto.LoginRequest;
import com.mangawatch.dto.LoginResponse;

//Files for Login/Register

import com.mangawatch.dto.RegisterRequest;
import com.mangawatch.model.User;
import com.mangawatch.repository.UserRepository;
import com.mangawatch.security.JwtUtil;
import com.mangawatch.service.AuthService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173") // for dev; will move to global config later
public class AuthController {
    private final AuthService authService;
    private final AuthenticationManager authManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthenticationManager authManager, JwtUtil jwtUtil, UserRepository userRepo, PasswordEncoder passwordEncoder, AuthService authService) {
        this.authManager = authManager;
        this.jwtUtil = jwtUtil;
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        try {
            User created = authService.register(req);
            // ensure I don't return passwordHash to client
            return ResponseEntity.status(201).body(
                java.util.Map.of("id", created.getId(), "username", created.getUsername(), "email", created.getEmail())
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(java.util.Map.of("error", "server error"));
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        try {
            Authentication auth = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            User user = userRepo.findByUsername(username).orElseThrow();
            String token = jwtUtil.generateToken(user.getUsername(), user.getId());
            return ResponseEntity.ok(Map.of(
                "token", token,
                "username", user.getUsername(),
                "displayName", Optional.ofNullable(user.getDisplayName()).orElse("")
            ));
        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
        }
    }
    
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error", "Missing or invalid token"));
        }

        try {
            String token = authHeader.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);
            User user = userRepo.findByUsername(username).orElseThrow();
            return ResponseEntity.ok(Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "email", user.getEmail(),
                "displayName", user.getDisplayName()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid token"));
        }
    }
}
