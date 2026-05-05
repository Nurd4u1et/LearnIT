package com.learnly.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "notifications")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Notification {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 40)
    private String type;          // COURSE_UPDATE | LESSON_COMPLETE | COMMENT_REPLY

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String body;

    @Column(columnDefinition = "TEXT")
    private String link;

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private boolean read = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist void prePersist() {
        if (createdAt == null) createdAt = OffsetDateTime.now();
    }
}
