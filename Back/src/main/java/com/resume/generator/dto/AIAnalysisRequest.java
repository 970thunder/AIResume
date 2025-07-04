package com.resume.generator.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AIAnalysisRequest {
    private String sessionId;
    private String extractedContent;
}