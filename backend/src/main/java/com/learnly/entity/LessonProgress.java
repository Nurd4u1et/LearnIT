package com.learnly.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "lesson_progress",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id","lesson_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LessonProgress {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lesson_id")
    private Lesson lesson;

    @Column(nullable = false)
    @Builder.Default
    private boolean completed = false;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;
}
