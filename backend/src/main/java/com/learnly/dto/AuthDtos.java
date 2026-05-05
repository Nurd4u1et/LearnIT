package com.learnly.dto;

import jakarta.validation.constraints.*;

public class AuthDtos {

    public record RegisterRequest(
            @NotBlank @Size(min = 2, max = 120) String name,
            @NotBlank @Email String email,
            @NotBlank @Pattern(regexp = "^\\+?[0-9]{7,20}$",
                    message = "Phone must be 7-20 digits, optional leading +") String phone,
            @NotBlank @Size(min = 8, max = 128,
                    message = "Password must be at least 8 characters") String password
    ) {}

    public record LoginRequest(
            @NotBlank String identifier,    // email or phone
            @NotBlank String password
    ) {}

    public record RefreshRequest(@NotBlank String refreshToken) {}

    public record TokenResponse(
            String accessToken,
            String refreshToken,
            UserDtos.Me user
    ) {}

    public record ForgotPasswordRequest(@NotBlank @Email String email) {}
    public record ResetPasswordRequest(@NotBlank String token,
                                       @NotBlank @Size(min = 8) String newPassword) {}
    public record VerifyEmailRequest(@NotBlank String token) {}
}
