package com.learnly.dto;

import com.learnly.entity.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class UserDtos {

    public record Me(Long id, String name, String email, String phone,
                     String role, String plan, String language, boolean emailVerified) {
        public static Me from(User u) {
            return new Me(u.getId(), u.getName(), u.getEmail(), u.getPhone(),
                    u.getRole().name(), u.getPlan().name(), u.getLanguage(), u.isEmailVerified());
        }
    }

    public record UpdateProfileRequest(String name) {}
    public record UpdateLanguageRequest(
            @NotBlank @Pattern(regexp = "en|ru") String language) {}
    public record UpgradePlanRequest(
            @NotBlank @Pattern(regexp = "FREE|PRO") String plan) {}
}
