package com.resume.generator.dto;

import lombok.Data;

@Data
public class ResumeGenerateRequest {
    // It no longer needs sessionId
    // private String sessionId;

    private Long templateId;
    private Object aiAnalysis; // Receive the full analysis object
}