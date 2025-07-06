package com.resume.generator.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "resumes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Resume {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "template_id", nullable = false)
    private Long templateId;

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "ai_analysis_data", columnDefinition = "TEXT")
    private String aiAnalysisData;

    @CreationTimestamp
    private LocalDateTime createdAt;

    // This field will not be persisted in the database.
    // It's used to carry the full resume data to the frontend.
    @Transient
    private Object resumeData;
}