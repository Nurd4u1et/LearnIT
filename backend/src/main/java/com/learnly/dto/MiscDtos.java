package com.learnly.dto;

import com.learnly.entity.*;
import jakarta.validation.constraints.*;

import java.time.OffsetDateTime;
import java.util.List;

public class MiscDtos {

    public record SubmissionRequest(@NotBlank String content) {}
    public record SubmissionResponse(Long id, String status, String content,
                                     String feedback, OffsetDateTime createdAt) {
        public static SubmissionResponse from(Submission s) {
            return new SubmissionResponse(s.getId(), s.getStatus().name(),
                    s.getContent(), s.getFeedback(), s.getCreatedAt());
        }
    }

    public record CommentRequest(@NotBlank @Size(max = 4000) String body, Long parentId) {}
    public record CommentResponse(Long id, Long userId, String userName,
                                  Long parentId, String body, OffsetDateTime createdAt) {
        public static CommentResponse from(Comment c) {
            return new CommentResponse(c.getId(), c.getUser().getId(),
                    c.getUser().getName(),
                    c.getParent() == null ? null : c.getParent().getId(),
                    c.getBody(), c.getCreatedAt());
        }
    }

    public record ReviewRequest(@Min(1) @Max(5) int rating, @Size(max = 4000) String body) {}
    public record ReviewResponse(Long id, Long userId, String userName,
                                 int rating, String body, OffsetDateTime createdAt) {
        public static ReviewResponse from(Review r) {
            return new ReviewResponse(r.getId(), r.getUser().getId(),
                    r.getUser().getName(), r.getRating(), r.getBody(), r.getCreatedAt());
        }
    }

    public record AiAskRequest(@NotBlank String message, Long lessonId) {}
    public record AiMessageDto(Long id, String role, String content, OffsetDateTime createdAt) {
        public static AiMessageDto from(AiMessage m) {
            return new AiMessageDto(m.getId(), m.getRole().name(), m.getContent(), m.getCreatedAt());
        }
    }

    // Interview
    public record InterviewQuestionDto(Long id, String title, String prompt,
                                       String language, String difficulty) {
        public static InterviewQuestionDto from(InterviewQuestion q, String lang) {
            boolean ru = "ru".equals(lang);
            return new InterviewQuestionDto(q.getId(),
                    ru && q.getTitleRu()  != null ? q.getTitleRu()  : q.getTitle(),
                    ru && q.getPromptRu() != null ? q.getPromptRu() : q.getPrompt(),
                    q.getLanguage(), q.getDifficulty().name());
        }
    }

    public record InterviewStartResponse(Long interviewId, List<InterviewQuestionDto> questions,
                                         int durationMin) {}
    public record InterviewAnswerRequest(@NotNull Long questionId, String answer) {}
    public record InterviewWarningRequest(@NotBlank String reason) {}
    public record InterviewFinishResponse(Long interviewId, int score, int total,
                                          int warnings, String status) {}

    public record NotificationDto(Long id, String type, String title, String body,
                                  String link, boolean read, OffsetDateTime createdAt) {
        public static NotificationDto from(Notification n) {
            return new NotificationDto(n.getId(), n.getType(), n.getTitle(),
                    n.getBody(), n.getLink(), n.isRead(), n.getCreatedAt());
        }
    }
}
