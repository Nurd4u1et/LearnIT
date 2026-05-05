package com.learnly.controller;

import com.learnly.dto.CourseDtos.LessonDetail;
import com.learnly.dto.MiscDtos.*;
import com.learnly.entity.User;
import com.learnly.security.CurrentUser;
import com.learnly.service.CommentService;
import com.learnly.service.LessonService;
import com.learnly.service.SubmissionService;
import jakarta.validation.Valid;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lessons")
public class LessonController {

    private final LessonService     lessons;
    private final CommentService    comments;
    private final SubmissionService submissions;
    private final CurrentUser       me;

    public LessonController(LessonService lessons, CommentService comments,
                            SubmissionService submissions, CurrentUser me) {
        this.lessons = lessons;
        this.comments = comments;
        this.submissions = submissions;
        this.me = me;
    }

    @GetMapping("/{id}")
    public LessonDetail get(@PathVariable Long id,
                            @RequestHeader(value = "Accept-Language", defaultValue = "en") String lang) {
        return lessons.get(id, normalize(lang), currentUserIdOrNull());
    }

    @PostMapping("/{id}/complete")
    public void complete(@PathVariable Long id) {
        lessons.markComplete(me.id(), id);
    }

    @GetMapping("/{id}/comments")
    public List<CommentResponse> listComments(@PathVariable Long id) {
        return comments.list(id).stream().map(CommentResponse::from).toList();
    }

    @PostMapping("/{id}/comments")
    public CommentResponse addComment(@PathVariable Long id, @Valid @RequestBody CommentRequest r) {
        return CommentResponse.from(comments.add(me.id(), id, r));
    }

    @PostMapping("/{id}/submissions")
    public SubmissionResponse submit(@PathVariable Long id, @Valid @RequestBody SubmissionRequest r) {
        return SubmissionResponse.from(submissions.submit(me.id(), id, r));
    }

    @GetMapping("/{id}/submissions")
    public List<SubmissionResponse> mySubmissions(@PathVariable Long id) {
        return submissions.mySubmissions(me.id(), id).stream()
                .map(SubmissionResponse::from).toList();
    }

    private Long currentUserIdOrNull() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User u) return u.getId();
        return null;
    }

    private String normalize(String h) {
        return h != null && h.toLowerCase().startsWith("ru") ? "ru" : "en";
    }
}
