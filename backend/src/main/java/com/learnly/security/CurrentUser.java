package com.learnly.security;

import com.learnly.entity.User;
import com.learnly.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUser {
    public User get() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof User u)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }
        return u;
    }

    public Long id() { return get().getId(); }
}
