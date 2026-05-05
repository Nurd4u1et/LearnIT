package com.learnly.controller;

import com.learnly.dto.AuthDtos.*;
import com.learnly.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService auth;
    public AuthController(AuthService auth) { this.auth = auth; }

    @PostMapping("/register")
    public TokenResponse register(@Valid @RequestBody RegisterRequest r) {
        return auth.register(r);
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest r) {
        return auth.login(r);
    }

    @PostMapping("/refresh")
    public TokenResponse refresh(@Valid @RequestBody RefreshRequest r) {
        return auth.refresh(r);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshRequest r) {
        auth.logout(r.refreshToken());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/forgot-password")
    public Map<String, String> forgot(@Valid @RequestBody ForgotPasswordRequest r) {
        String token = auth.requestPasswordReset(r);
        // Always return generic message; include dev token only when present.
        var resp = new java.util.HashMap<String, String>();
        resp.put("message", "If the email exists, a reset link has been sent.");
        if (token != null) resp.put("devToken", token);
        return resp;
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> reset(@Valid @RequestBody ResetPasswordRequest r) {
        auth.resetPassword(r);
        return ResponseEntity.noContent().build();
    }
}
