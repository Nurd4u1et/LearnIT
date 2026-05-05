package com.learnly.service;

import com.learnly.dto.CourseDtos.LessonDetail;
import com.learnly.entity.*;
import com.learnly.exception.ApiException;
import com.learnly.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@Transactional
public class LessonService {

    private final LessonRepository           lessons;
    private final LessonProgressRepository   progress;
    private final UserRepository             users;
    private final NotificationService        notifications;

    public LessonService(LessonRepository lessons,
                         LessonProgressRepository progress,
                         UserRepository users,
                         NotificationService notifications) {
        this.lessons = lessons;
        this.progress = progress;
        this.users = users;
        this.notifications = notifications;
    }

    public LessonDetail get(Long lessonId, String lang, Long userIdOrNull) {
        Lesson l = lessons.findById(lessonId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Lesson not found"));
        boolean done = userIdOrNull != null
                && progress.findByUserIdAndLessonId(userIdOrNull, lessonId)
                           .map(LessonProgress::isCompleted).orElse(false);
        return LessonDetail.from(l, lang, done);
    }

    public void markComplete(Long userId, Long lessonId) {
        Lesson l = lessons.findById(lessonId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Lesson not found"));
        var existing = progress.findByUserIdAndLessonId(userId, lessonId);
        boolean wasNew = existing.isEmpty();
        var p = existing.orElseGet(() -> LessonProgress.builder()
                .user(users.getReferenceById(userId))
                .lesson(l)
                .build());
        if (!p.isCompleted()) {
            p.setCompleted(true);
            p.setCompletedAt(OffsetDateTime.now());
        }
        progress.save(p);
        if (wasNew) {
            notifications.create(userId, "LESSON_COMPLETE",
                    "Lesson completed", l.getTitle(),
                    "/lesson/" + l.getId());
        }
    }
}
