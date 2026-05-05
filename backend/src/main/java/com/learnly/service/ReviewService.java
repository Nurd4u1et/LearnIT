package com.learnly.service;

import com.learnly.dto.MiscDtos.ReviewRequest;
import com.learnly.entity.*;
import com.learnly.exception.ApiException;
import com.learnly.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ReviewService {

    private final ReviewRepository reviews;
    private final CourseRepository courses;
    private final UserRepository   users;

    public ReviewService(ReviewRepository reviews, CourseRepository courses, UserRepository users) {
        this.reviews = reviews;
        this.courses = courses;
        this.users = users;
    }

    public List<Review> list(Long courseId) {
        return reviews.findByCourseIdOrderByCreatedAtDesc(courseId);
    }

    public Review upsert(Long userId, Long courseId, ReviewRequest req) {
        Course c = courses.findById(courseId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Course not found"));
        var existing = reviews.findByUserIdAndCourseId(userId, courseId);
        Review r = existing.orElseGet(() -> Review.builder()
                .user(users.getReferenceById(userId))
                .course(c)
                .build());
        r.setRating((short) req.rating());
        r.setBody(req.body());
        return reviews.save(r);
    }
}
