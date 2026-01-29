package com.resume.generator.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class InterviewSessionDTO {
    private Long id;
    private Long userId;
    private List<InterviewQuestionDTO> questions;
    private String status;
    private Integer totalQuestions;
    private Integer score;
    private LocalDateTime createdAt;
}
