package com.learnly.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "courses")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Course {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 80)
    private String slug;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(name = "title_ru", length = 200)
    private String titleRu;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "description_ru", columnDefinition = "TEXT")
    private String descriptionRu;

    @Column(nullable = false, length = 40)
    private String language;          // Python | Java | Go

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Level level = Level.BEGINNER;

    @Column(name = "cover_url", columnDefinition = "TEXT")
    private String coverUrl;

    @Column(name = "is_pro_only", nullable = false)
    @Builder.Default
    private boolean proOnly = false;

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

    public enum Level { BEGINNER, INTERMEDIATE, ADVANCED }
}
