package com.learnly.service;

import com.learnly.dto.CourseDtos.*;
import com.learnly.entity.*;
import com.learnly.exception.ApiException;
import com.learnly.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@Transactional
public class AdminService {

    private final UserRepository       users;
    private final CourseRepository     courses;
    private final LessonRepository     lessons;
    private final EnrollmentRepository enrollments;
    private final SubmissionRepository submissions;
    private final CommentRepository    comments;

    public AdminService(UserRepository users, CourseRepository courses,
                        LessonRepository lessons, EnrollmentRepository enrollments,
                        SubmissionRepository submissions, CommentRepository comments) {
        this.users = users;
        this.courses = courses;
        this.lessons = lessons;
        this.enrollments = enrollments;
        this.submissions = submissions;
        this.comments = comments;
    }

    public Course createCourse(CreateCourseRequest r) {
        Course c = Course.builder()
                .slug(r.slug())
                .title(r.title()).titleRu(r.titleRu())
                .description(r.description()).descriptionRu(r.descriptionRu())
                .language(r.language())
                .level(Course.Level.valueOf(r.level() == null ? "BEGINNER" : r.level()))
                .coverUrl(r.coverUrl())
                .proOnly(Boolean.TRUE.equals(r.proOnly()))
                .build();
        return courses.save(c);
    }

    public Lesson addLesson(Long courseId, CreateLessonRequest r) {
        Course c = courses.findById(courseId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Course not found"));
        Lesson l = Lesson.builder()
                .course(c)
                .position(r.position())
                .title(r.title()).titleRu(r.titleRu())
                .videoUrl(r.videoUrl())
                .description(r.description()).descriptionRu(r.descriptionRu())
                .task(r.task()).taskRu(r.taskRu())
                .expectedSolution(r.expectedSolution())
                .build();
        return lessons.save(l);
    }

    public void hideComment(Long commentId) {
        var c = comments.findById(commentId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Comment not found"));
        c.setHidden(true);
    }

    public Map<String, Object> analytics() {
        return Map.of(
                "users",        users.count(),
                "courses",      courses.count(),
                "lessons",      lessons.count(),
                "enrollments",  enrollments.count(),
                "submissions",  submissions.count(),
                "comments",     comments.count()
        );
    }
}
