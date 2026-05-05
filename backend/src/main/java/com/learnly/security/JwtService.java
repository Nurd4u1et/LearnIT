package com.learnly.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtService {

    private final SecretKey key;
    private final long accessTtlMs;
    private final long refreshTtlMs;

    public JwtService(@Value("${learnly.jwt.secret}") String base64Secret,
                      @Value("${learnly.jwt.access-token-ttl-min}") long accessTtlMin,
                      @Value("${learnly.jwt.refresh-token-ttl-days}") long refreshTtlDays) {
        byte[] decoded = Base64.getDecoder().decode(base64Secret);
        this.key = Keys.hmacShaKeyFor(decoded);
        this.accessTtlMs  = accessTtlMin  * 60_000L;
        this.refreshTtlMs = refreshTtlDays * 24L * 3_600_000L;
    }

    public String issueAccessToken(Long userId, String role) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("role", role)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + accessTtlMs))
                .signWith(key)
                .compact();
    }

    /** Random-looking string used as the refresh-token identifier (stored in DB, not a JWT). */
    public String generateRefreshTokenString() {
        // Reuse JWT signing for a strong opaque string
        Date now = new Date();
        return Jwts.builder()
                .id(java.util.UUID.randomUUID().toString())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + refreshTtlMs))
                .signWith(key)
                .compact();
    }

    public Jws<Claims> parse(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
    }

    public long refreshTtlMs() { return refreshTtlMs; }
}
