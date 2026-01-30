package com.resume.generator.dto;

import lombok.Data;

@Data
public class TemplateCreateRequest {

    private String name;
    private String description;
    private String htmlContent;
    private java.math.BigDecimal price;

}