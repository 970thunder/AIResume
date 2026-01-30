package com.resume.generator.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminStatsDTO {
    private long totalUsers;
    private long totalTemplates;
    private long pendingTemplates;
    private long totalQuestions;
    private long activeSessionsToday;
}
