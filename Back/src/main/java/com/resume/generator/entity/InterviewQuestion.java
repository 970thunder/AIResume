package com.resume.generator.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "interview_questions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    @Builder.Default
    private String type = "OPEN_ENDED"; // OPEN_ENDED, MULTIPLE_CHOICE

    @Column(columnDefinition = "json")
    private String options; // JSON string for options

    @Column(name = "reference_answer", columnDefinition = "TEXT")
    private String referenceAnswer;

    @Builder.Default
    private String difficulty = "MEDIUM"; // EASY, MEDIUM, HARD

    private String category;

    private String tags;

    @Builder.Default
    private Long totalAttempts = 0L;

    @Builder.Default
    private Long correctCount = 0L;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;
}
