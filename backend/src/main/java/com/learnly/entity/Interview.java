package com.learnly.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "interviews")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Interview {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "started_at", nullable = false, updatable = false)
    private OffsetDateTime startedAt;

    @Column(name = "finished_at")
    private OffsetDateTime finishedAt;

    @Column(name = "duration_sec")
    private Integer durationSec;

    private Integer score;

    @Column(nullable = false)
    @Builder.Default
    private Integer warnings = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Status status = Status.IN_PROGRESS;

    @PrePersist void prePersist() {
        if (startedAt == null) startedAt = OffsetDateTime.now();
    }

    public enum Status { IN_PROGRESS, FINISHED, TERMINATED }
}
