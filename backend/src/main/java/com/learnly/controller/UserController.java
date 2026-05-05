package com.learnly.controller;

import com.learnly.dto.UserDtos;
import com.learnly.dto.UserDtos.*;
import com.learnly.security.CurrentUser;
import com.learnly.service.CourseService;
import com.learnly.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/me")
public class UserController {

    private final CurrentUser   me;
    private final UserService   userService;
    private final CourseService courseService;

    public UserController(CurrentUser me, UserService userService, CourseService courseService) {
        this.me = me;
        this.userService = userService;
        this.courseService = courseService;
    }

    @GetMapping
    public Me me() { return Me.from(me.get()); }

    @PatchMapping("/profile")
    public Me updateProfile(@Valid @RequestBody UpdateProfileRequest req) {
        return Me.from(userService.updateProfile(me.id(), req));
    }

    @PatchMapping("/language")
    public Me updateLanguage(@Valid @RequestBody UpdateLanguageRequest req) {
        return Me.from(userService.updateLanguage(me.id(), req));
    }

    @PatchMapping("/plan")
    public Me upgradePlan(@Valid @RequestBody UpgradePlanRequest req) {
        return Me.from(userService.upgradePlan(me.id(), req));
    }

    @GetMapping("/courses")
    public List<CourseService.EnrolledCourse> myCourses() {
        return courseService.myCourses(me.id(), me.get().getLanguage());
    }
}
