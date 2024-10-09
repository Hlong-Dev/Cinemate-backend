package com.mycompany.mavenproject1.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    private final String jwtSecret = "your-256-bit-secret-your-256-bit-secret"; // Use a secure key
    private final int jwtExpirationMs = 86400000; // Token validity in milliseconds (e.g., 24 hours)

    private Key getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

   public String generateJwtToken(String username, String avtUrl) {
    return Jwts.builder()
        .setSubject(username)
        .claim("avt_url", avtUrl) // Thêm avatar URL vào payload của JWT
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
        .signWith(getSigningKey(), SignatureAlgorithm.HS256)
        .compact();
}



public String getAvatarUrlFromJwtToken(String token) {
    return Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token)
            .getBody()
            .get("avt_url", String.class);
}

    public String getUserNameFromJwtToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder()
                 .setSigningKey(getSigningKey())
                 .build()
                 .parseClaimsJws(authToken);
            return true;
        } catch (JwtException e) {
            // Log the exception
            return false;
        }
    }
}
