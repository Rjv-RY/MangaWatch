package com.mangawatch.security;

import com.mangawatch.model.User;
import com.mangawatch.repository.UserRepository;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository repo;

    public CustomUserDetailsService(UserRepository repo) {
        this.repo = repo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = repo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("No user: " + username));
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPasswordHash())     // use actual password field name in User entity
                .authorities(user.getRole() == null ? Collections.emptyList()
                        : Collections.singleton(new SimpleGrantedAuthority("ROLE_" + user.getRole())))
                .disabled(!user.isEnabled())
                .build();
    }
}