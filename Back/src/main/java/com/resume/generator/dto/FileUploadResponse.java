package com.resume.generator.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileUploadResponse {
    private boolean success;
    private String message;
    private String sessionId;
    private List<String> fileNames;
    private String extractedContent;
}