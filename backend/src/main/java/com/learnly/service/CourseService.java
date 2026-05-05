package com.learnly.service;

import com.learnly.dto.CourseDtos.*;
import com.learnly.entity.*;
import com.learnly.exception.ApiException;
import com.learnly.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class CourseService {

    private final CourseRepository           courses;
    private final LessonRepository           lessons;
    private final EnrollmentRepository       enrollments;
    private final LessonProgressRepository   progress;
    private final ReviewRepository           reviews;
    private final UserRepository             users;
    private final NotificationService        notifications;

    public CourseService(CourseRepository courses, LessonRepository lessons,
                         EnrollmentRepository enrollments,
                         LessonProgressRepository progress,
                         ReviewRepository reviews, UserRepository users,
                         NotificationService notifications) {
        this.courses = courses;
        this.lessons = lessons;
        this.enrollments = enrollments;
        this.progress = progress;
        this.reviews = reviews;
        this.users = users;
        this.notifications = notifications;
    }

    public List<CourseSummary> listAll(String lang) {
        return courses.findAll().stream()
                .sorted(Comparator.comparing(Course::getId))
                .map(c -> CourseSummary.from(c, lang,
                        avg(c.getId()),
                        lessons.countByCourseId(c.getId())))
                .toList();
    }

    public CourseDetail detail(String slug, String lang, Long userIdOrNull) {
        Course c = courses.findBySlug(slug)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Course not found"));

        boolean enrolled = userIdOrNull != null
                && enrollments.existsByUserIdAndCourseId(userIdOrNull, c.getId());

        Set<Long> completedLessonIds = userIdOrNull == null
                ? Set.of()
                : progress.findByUserId(userIdOrNull).stream()
                          .filter(LessonProgress::isCompleted)
                          .map(p -> p.getLesson().getId())
                          .collect(java.util.stream.Collectors.toSet());

        List<Lesson> ls = lessons.findByCourseIdOrderByPositionAsc(c.getId());
        var lessonDtos = ls.stream()
                .map(l -> LessonSummary.from(l, lang, completedLessonIds.contains(l.getId())))
                .toList();

        int percent = ls.isEmpty() ? 0
                : (int) Math.round(100.0 *
                  ls.stream().filter(l -> completedLessonIds.contains(l.getId())).count()
                  / ls.size());

        var summary = CourseSummary.from(c, lang, avg(c.getId()), ls.size());
        return new CourseDetail(summary, lessonDtos, percent, enrolled);
    }

    public void enroll(Long userId, Long courseId) {
        if (enrollments.existsByUserIdAndCourseId(userId, courseId)) return;
        Course c = courses.findById(courseId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Course not found"));
        User u = users.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        if (c.isProOnly() && u.getPlan() != User.Plan.PRO)
            throw new ApiException(HttpStatus.FORBIDDEN, "Pro plan required");
        enrollments.save(Enrollment.builder().user(u).course(c).build());
    }

    public List<EnrolledCourse> myCourses(Long userId, String lang) {
        return enrollments.findByUserId(userId).stream()
                .map(e -> {
                    Course c = e.getCourse();
                    long total = lessons.countByCourseId(c.getId());
                    long done  = progress.countCompletedInCourse(userId, c.getId());
                    int pct = total == 0 ? 0 : (int) Math.round(100.0 * done / total);
                    var s = CourseSummary.from(c, lang, avg(c.getId()), total);
                    return new EnrolledCourse(s, pct, (int) done, (int) total);
                })
                .toList();
    }

    private double avg(Long courseId) {
        Double v = reviews.averageForCourse(courseId);
        return v == null ? 0.0 : Math.round(v * 10.0) / 10.0;
    }

    public record EnrolledCourse(CourseSummary course, int progressPercent,
                                 int lessonsCompleted, int lessonsTotal) {}
}
