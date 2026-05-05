package com.learnly.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "interview_questions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class InterviewQuestion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;
    @Column(name = "title_ru", length = 200)
    private String titleRu;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String prompt;
    @Column(name = "prompt_ru", columnDefinition = "TEXT")
    private String promptRu;

    @Column(nullable = false, length = 40)
    private String language;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Difficulty difficulty = Difficulty.EASY;

    public enum Difficulty { EASY, MEDIUM, HARD }
}
