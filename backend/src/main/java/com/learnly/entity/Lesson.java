package com.learnly.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "lessons")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Lesson {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id")
    private Course course;

    @Column(nullable = false)
    private Integer position;

    @Column(nullable = false, length = 200)
    private String title;
    @Column(name = "title_ru", length = 200)
    private String titleRu;

    @Column(name = "video_url", columnDefinition = "TEXT")
    private String videoUrl;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;
    @Column(name = "description_ru", columnDefinition = "TEXT")
    private String descriptionRu;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String task;
    @Column(name = "task_ru", columnDefinition = "TEXT")
    private String taskRu;

    @Column(name = "expected_solution", columnDefinition = "TEXT")
    private String expectedSolution;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist void prePersist() {
        var now = OffsetDateTime.now();
        if (createdAt == null) createdAt = now;
        updatedAt = now;
    }
    @PreUpdate void preUpdate() { updatedAt = OffsetDateTime.now(); }
}
