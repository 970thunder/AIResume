package com.resume.generator.dto;

import lombok.Data;

@Data
public class SubmitAnswerRequest {
    private Long questionId;
    private String answer;
}
