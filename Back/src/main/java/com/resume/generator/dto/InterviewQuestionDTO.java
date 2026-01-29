package com.resume.generator.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class InterviewQuestionDTO {
    private Long id;
    private String content;
    private String type;
    private List<String> options;
    private String difficulty;
    private String category;
    private String tags;
}
