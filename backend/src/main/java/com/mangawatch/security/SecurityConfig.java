package com.mangawatch.security;

//Significant changes for Login/Register

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.annotation.PostConstruct;

@Configuration
public class SecurityConfig {
	
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;
    private final JwtFilter jwtFilter;
    
    public SecurityConfig(JwtUtil jwtUtil, CustomUserDetailsService userDetailsService, JwtFilter jwtFilter) {
    	this.jwtUtil = jwtUtil;
    	this.userDetailsService = userDetailsService;
    	this.jwtFilter = jwtFilter;
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(11);
    }
    

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
    

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
        	.sessionManagement(session -> 
            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        			)
            .authorizeHttpRequests(auth -> auth
            	.requestMatchers("/api/covers/**").permitAll()
            	.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers( "/api/**", "/api/manga/**", "/admin/import/**").permitAll() // allow H2
                .anyRequest().permitAll()                 // protect everything else
            )
            .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**", "/admin/import/**")) // disable CSRF for H2
            .headers(headers -> headers.frameOptions().disable())       // allow frames (H2 UI)
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()));

        return http.build();
    }
    
    @PostConstruct
    public void init() {
        System.out.println(">>> SecurityConfig loaded successfully <<<");
    }
    
    //cors config bean
    @Bean
    public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {
        var config = new org.springframework.web.cors.CorsConfiguration();
        
        //allow frontend origin
        config.setAllowedOrigins(java.util.List.of("http://localhost:5173", "https://manga-watch.vercel.app"));
        
        //allow all HTTP methods
        config.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        
        // allow all headers
        config.setAllowedHeaders(java.util.List.of("*"));
        
        // allow credentials (cookies, authorization headers)
        config.setAllowCredentials(false);
        
        //apply to all paths
        UrlBasedCorsConfigurationSource source = new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
