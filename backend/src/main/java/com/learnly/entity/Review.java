package com.learnly.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "reviews",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id","course_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Review {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id")
    private Course course;

    @Column(nullable = false)
    private Short rating; // 1..5

    @Column(columnDefinition = "TEXT")
    private String body;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist void prePersist() {
        if (createdAt == null) createdAt = OffsetDateTime.now();
    }
}
