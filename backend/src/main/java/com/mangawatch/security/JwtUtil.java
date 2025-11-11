package com.mangawatch.security;

//Files for Login/Register

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {
    private final Key key;
    private final long expirationMs;
    private final String issuer;
    
    public JwtUtil(@Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-ms}") long expirationMs,
            @Value("${jwt.issuer}") String issuer) {
		 this.key = Keys.hmacShaKeyFor(secret.getBytes());
		 this.expirationMs = expirationMs;
		 this.issuer = issuer;
    }
    
    public String generateToken(String username, Long userId) {
    	Date now = new Date();
    	Date exp = new Date(now.getTime() + expirationMs);
    	
    	return Jwts.builder()
    			.setSubject(username)
    			.setIssuer(issuer)
    			.setIssuedAt(now)
    			.setExpiration(exp)
    			.claim("uid", userId)
    			.signWith(key, SignatureAlgorithm.HS256)
    			.compact();
    }
    
    public Jws<Claims> validateToken(String token){
        return Jwts.parser()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
    }
    
    public String getUsernameFromToken(String token) {
        return validateToken(token).getBody().getSubject();
    }
    
    public Long getUserIdFromToken(String token) {
        Object v = validateToken(token).getBody().get("uid");
        if (v instanceof Integer) return ((Integer) v).longValue();
        if (v instanceof Long) return (Long) v;
        if (v instanceof String) return Long.parseLong((String) v);
        return null;
    }
}
