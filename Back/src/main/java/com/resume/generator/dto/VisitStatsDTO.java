package com.resume.generator.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VisitStatsDTO {
    private long sitePv; // Page Views
    private long siteUv; // Unique Visitors
}