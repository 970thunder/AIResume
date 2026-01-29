package com.resume.generator.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class InterviewRecordDTO {
    private Long id;
    private Long sessionId;
    private Long questionId;
    private String questionContent;
    private String userAnswer;
    private String referenceAnswer;
    private String aiEvaluation;
    private Integer score;
    private Boolean isCorrect;
    private LocalDateTime createdAt;
}
