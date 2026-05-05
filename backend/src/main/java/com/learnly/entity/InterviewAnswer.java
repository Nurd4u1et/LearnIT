package com.learnly.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "interview_answers",
       uniqueConstraints = @UniqueConstraint(columnNames = {"interview_id","question_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class InterviewAnswer {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "interview_id")
    private Interview interview;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id")
    private InterviewQuestion question;

    @Column(nullable = false, columnDefinition = "TEXT")
    @Builder.Default
    private String answer = "";

    @Column(name = "is_correct")
    private Boolean correct;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist void prePersist() {
        if (createdAt == null) createdAt = OffsetDateTime.now();
    }
}
