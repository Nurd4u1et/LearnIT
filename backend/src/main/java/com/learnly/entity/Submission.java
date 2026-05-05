package com.learnly.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "submissions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Submission {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lesson_id")
    private Lesson lesson;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Status status = Status.SUBMITTED;

    @Column(columnDefinition = "TEXT")
    private String feedback;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist void prePersist() {
        if (createdAt == null) createdAt = OffsetDateTime.now();
    }

    public enum Status { SUBMITTED, PASSED, FAILED }
}
