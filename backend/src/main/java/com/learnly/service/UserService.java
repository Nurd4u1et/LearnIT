package com.learnly.service;

import com.learnly.dto.UserDtos.*;
import com.learnly.entity.User;
import com.learnly.exception.ApiException;
import com.learnly.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {

    private final UserRepository users;

    public UserService(UserRepository users) { this.users = users; }

    public User updateProfile(Long userId, UpdateProfileRequest req) {
        User u = users.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        if (req.name() != null && !req.name().isBlank()) u.setName(req.name().trim());
        return u;
    }

    public User updateLanguage(Long userId, UpdateLanguageRequest req) {
        User u = users.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        u.setLanguage(req.language());
        return u;
    }

    public User upgradePlan(Long userId, UpgradePlanRequest req) {
        User u = users.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        u.setPlan(User.Plan.valueOf(req.plan()));
        return u;
    }
}
