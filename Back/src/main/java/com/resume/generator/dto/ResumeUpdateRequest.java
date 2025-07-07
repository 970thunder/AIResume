package com.resume.generator.dto;

import lombok.Data;

@Data
public class ResumeUpdateRequest {
    private String aiAnalysisData;
    private Long templateId;
}