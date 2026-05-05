package com.learnly.service;

import com.learnly.dto.AuthDtos.*;
import com.learnly.dto.UserDtos;
import com.learnly.entity.*;
import com.learnly.exception.ApiException;
import com.learnly.repository.*;
import com.learnly.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@Transactional
public class AuthService {

    private final UserRepository                users;
    private final RefreshTokenRepository        refreshTokens;
    private final PasswordResetTokenRepository  resets;
    private final PasswordEncoder               encoder;
    private final JwtService                    jwt;

    public AuthService(UserRepository users,
                       RefreshTokenRepository refreshTokens,
                       PasswordResetTokenRepository resets,
                       PasswordEncoder encoder,
                       JwtService jwt) {
        this.users = users;
        this.refreshTokens = refreshTokens;
        this.resets = resets;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    public TokenResponse register(RegisterRequest req) {
        if (users.existsByEmail(req.email()))
            throw new ApiException(HttpStatus.CONFLICT, "Email already registered");
        if (users.existsByPhone(req.phone()))
            throw new ApiException(HttpStatus.CONFLICT, "Phone already registered");

        User u = User.builder()
                .name(req.name().trim())
                .email(req.email().toLowerCase())
                .phone(req.phone())
                .passwordHash(encoder.encode(req.password()))
                .build();
        users.save(u);
        return issueTokens(u);
    }

    public TokenResponse login(LoginRequest req) {
        String id = req.identifier().trim();
        User u = (id.contains("@")
                ? users.findByEmail(id.toLowerCase())
                : users.findByPhone(id))
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!encoder.matches(req.password(), u.getPasswordHash()))
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid credentials");

        return issueTokens(u);
    }

    public TokenResponse refresh(RefreshRequest req) {
        RefreshToken token = refreshTokens.findByToken(req.refreshToken())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));
        if (token.isRevoked() || token.getExpiresAt().isBefore(OffsetDateTime.now()))
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Refresh token expired");

        // Rotate: revoke old, issue new
        token.setRevoked(true);
        return issueTokens(token.getUser());
    }

    public void logout(String refreshToken) {
        refreshTokens.findByToken(refreshToken).ifPresent(t -> t.setRevoked(true));
    }

    // ----- password reset -----
    public String requestPasswordReset(ForgotPasswordRequest req) {
        User u = users.findByEmail(req.email().toLowerCase()).orElse(null);
        if (u == null) return null; // don't leak existence
        var token = PasswordResetToken.builder()
                .user(u)
                .token(UUID.randomUUID().toString())
                .expiresAt(OffsetDateTime.now().plusHours(1))
                .build();
        resets.save(token);
        // In production: email this. For dev we return so the FE can show it.
        return token.getToken();
    }

    public void resetPassword(ResetPasswordRequest req) {
        var t = resets.findByToken(req.token())
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Invalid token"));
        if (t.isUsed() || t.getExpiresAt().isBefore(OffsetDateTime.now()))
            throw new ApiException(HttpStatus.BAD_REQUEST, "Token expired");
        t.setUsed(true);
        t.getUser().setPasswordHash(encoder.encode(req.newPassword()));
    }

    // ----- helpers -----
    private TokenResponse issueTokens(User u) {
        String access = jwt.issueAccessToken(u.getId(), u.getRole().name());
        String refresh = jwt.generateRefreshTokenString();

        var rt = RefreshToken.builder()
                .user(u)
                .token(refresh)
                .expiresAt(OffsetDateTime.now().plusSeconds(jwt.refreshTtlMs() / 1000))
                .build();
        refreshTokens.save(rt);

        return new TokenResponse(access, refresh, UserDtos.Me.from(u));
    }
}
