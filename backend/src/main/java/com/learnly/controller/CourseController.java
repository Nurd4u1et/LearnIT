package com.learnly.controller;

import com.learnly.dto.CourseDtos.*;
import com.learnly.dto.MiscDtos.*;
import com.learnly.entity.User;
import com.learnly.security.CurrentUser;
import com.learnly.service.CourseService;
import com.learnly.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService courses;
    private final ReviewService reviews;
    private final CurrentUser   me;

    public CourseController(CourseService courses, ReviewService reviews, CurrentUser me) {
        this.courses = courses;
        this.reviews = reviews;
        this.me = me;
    }

    @GetMapping
    public List<CourseSummary> list(@RequestHeader(value = "Accept-Language", defaultValue = "en") String lang) {
        return courses.listAll(normalize(lang));
    }

    @GetMapping("/{slug}")
    public CourseDetail detail(@PathVariable String slug,
                               @RequestHeader(value = "Accept-Language", defaultValue = "en") String lang) {
        Long uid = currentUserIdOrNull();
        return courses.detail(slug, normalize(lang), uid);
    }

    @PostMapping("/{id}/enroll")
    public void enroll(@PathVariable Long id) {
        courses.enroll(me.id(), id);
    }

    @GetMapping("/{id}/reviews")
    public List<ReviewResponse> listReviews(@PathVariable Long id) {
        return reviews.list(id).stream().map(ReviewResponse::from).toList();
    }

    @PostMapping("/{id}/reviews")
    public ReviewResponse upsertReview(@PathVariable Long id,
                                       @Valid @RequestBody ReviewRequest r) {
        return ReviewResponse.from(reviews.upsert(me.id(), id, r));
    }

    private Long currentUserIdOrNull() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User u) return u.getId();
        return null;
    }

    private String normalize(String header) {
        if (header == null) return "en";
        String h = header.toLowerCase();
        return h.startsWith("ru") ? "ru" : "en";
    }
}
