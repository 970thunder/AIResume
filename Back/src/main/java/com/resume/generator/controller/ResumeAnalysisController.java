package com.resume.generator.controller;

import com.resume.generator.service.AIAnalysisService;
import com.resume.generator.service.FileProcessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/analysis")
@CrossOrigin(origins = "*")
public class ResumeAnalysisController {

    @Autowired
    private FileProcessService fileProcessService;

    @Autowired
    private AIAnalysisService aiAnalysisService;

    @PostMapping("/career")
    public ResponseEntity<Map<String, Object>> analyzeCareer(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "jobDescription", required = false) String jobDescription) {
        try {
            // 1. Extract text from file
            String content = fileProcessService.extractContent(file);

            // 2. AI Analysis
            Map<String, Object> result = aiAnalysisService.analyzeCareer(content, jobDescription);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}
