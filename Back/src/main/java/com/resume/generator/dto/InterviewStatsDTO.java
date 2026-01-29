package com.resume.generator.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class InterviewStatsDTO {
    private long totalQuestionsAnswered;
    private long totalCorrect;
    private double accuracy;
    private List<InterviewRecordDTO> wrongRecords;
}
