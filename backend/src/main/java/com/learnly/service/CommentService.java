package com.learnly.service;

import com.learnly.dto.MiscDtos.CommentRequest;
import com.learnly.entity.*;
import com.learnly.exception.ApiException;
import com.learnly.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CommentService {

    private final CommentRepository      comments;
    private final LessonRepository       lessons;
    private final UserRepository         users;
    private final NotificationService    notifications;

    public CommentService(CommentRepository comments, LessonRepository lessons,
                          UserRepository users, NotificationService notifications) {
        this.comments = comments;
        this.lessons = lessons;
        this.users = users;
        this.notifications = notifications;
    }

    public List<Comment> list(Long lessonId) {
        return comments.findByLessonIdAndHiddenFalseOrderByCreatedAtAsc(lessonId);
    }

    public Comment add(Long userId, Long lessonId, CommentRequest req) {
        Lesson l = lessons.findById(lessonId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Lesson not found"));
        Comment parent = req.parentId() == null ? null
                : comments.findById(req.parentId()).orElse(null);

        Comment c = Comment.builder()
                .user(users.getReferenceById(userId))
                .lesson(l)
                .parent(parent)
                .body(req.body().trim())
                .build();
        comments.save(c);

        if (parent != null && !parent.getUser().getId().equals(userId)) {
            notifications.create(parent.getUser().getId(), "COMMENT_REPLY",
                    "Someone replied to your comment",
                    req.body().length() > 120 ? req.body().substring(0, 120) + "..." : req.body(),
                    "/lesson/" + l.getId());
        }
        return c;
    }

    public void hide(Long commentId) {
        var c = comments.findById(commentId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Comment not found"));
        c.setHidden(true);
    }
}
