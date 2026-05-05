package com.learnly.service;

import com.learnly.dto.MiscDtos.SubmissionRequest;
import com.learnly.entity.*;
import com.learnly.exception.ApiException;
import com.learnly.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class SubmissionService {

    private final SubmissionRepository submissions;
    private final LessonRepository     lessons;
    private final UserRepository       users;
    private final LessonService        lessonService;

    public SubmissionService(SubmissionRepository submissions, LessonRepository lessons,
                             UserRepository users, LessonService lessonService) {
        this.submissions = submissions;
        this.lessons = lessons;
        this.users = users;
        this.lessonService = lessonService;
    }

    public Submission submit(Long userId, Long lessonId, SubmissionRequest req) {
        Lesson l = lessons.findById(lessonId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Lesson not found"));

        // Lightweight auto-grading: compare key tokens vs expected solution.
        Submission.Status status = Submission.Status.SUBMITTED;
        String feedback = null;
        if (l.getExpectedSolution() != null && !l.getExpectedSolution().isBlank()) {
            if (looksCorrect(req.content(), l.getExpectedSolution())) {
                status = Submission.Status.PASSED;
                feedback = "Looks good — your output matches what we expected.";
            } else {
                status = Submission.Status.FAILED;
                feedback = "Your solution doesn't appear to satisfy the task. Try again.";
            }
        }

        Submission s = Submission.builder()
                .user(users.getReferenceById(userId))
                .lesson(l)
                .content(req.content())
                .status(status)
                .feedback(feedback)
                .build();
        submissions.save(s);

        if (status == Submission.Status.PASSED) {
            lessonService.markComplete(userId, lessonId);
        }
        return s;
    }

    public List<Submission> mySubmissions(Long userId, Long lessonId) {
        return submissions.findByUserIdAndLessonIdOrderByCreatedAtDesc(userId, lessonId);
    }

    /** Naive grader: extracts the printed string from `print("...")` / println / fmt.Println
     *  in both the expected solution and the user's submission and compares. Falls back to
     *  comparing normalized whitespace. */
    private boolean looksCorrect(String submitted, String expected) {
        String e = extractOutput(expected);
        String s = extractOutput(submitted);
        if (!e.isEmpty() && !s.isEmpty()) return e.equals(s);
        return normalize(submitted).contains(normalize(expected));
    }

    private String extractOutput(String code) {
        var p = java.util.regex.Pattern.compile(
                "(?:print|println|Println)\\s*\\(\\s*[\"`]([^\"`]+)[\"`]");
        var m = p.matcher(code);
        return m.find() ? m.group(1).trim() : "";
    }

    private String normalize(String s) {
        return s.replaceAll("\\s+", " ").trim();
    }
}
