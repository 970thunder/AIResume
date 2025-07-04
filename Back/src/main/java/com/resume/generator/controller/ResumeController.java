package com.resume.generator.controller;

import com.resume.generator.dto.AIAnalysisRequest;
import com.resume.generator.dto.ResumeGenerateRequest;
import com.resume.generator.entity.Resume;
import com.resume.generator.service.AIAnalysisService;
import com.resume.generator.service.FileProcessService;
import com.resume.generator.service.ResumeGenerateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/resume")
public class ResumeController {

    @Autowired
    private AIAnalysisService aiAnalysisService;

    @Autowired
    private ResumeGenerateService resumeGenerateService;

    @Autowired
    private FileProcessService fileProcessService;

    @PostMapping("/analyze")
    public ResponseEntity<Map<String, Object>> analyzeProfile(@RequestBody AIAnalysisRequest request) {
        try {
            Map<String, Object> aiAnalysisMap = aiAnalysisService.analyzeProfile(request.getExtractedContent());

            // Save the analysis to the database
            fileProcessService.saveAiAnalysis(request.getSessionId(), aiAnalysisMap);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("analysis", aiAnalysisMap);
            response.put("sessionId", request.getSessionId());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "AI analysis failed: " + e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generateResume(@RequestBody ResumeGenerateRequest request) {
        try {
            Resume newResume = resumeGenerateService.generateResume(
                    request.getSessionId(),
                    request.getTemplateId());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("resume", newResume);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Resume generation failed: " + e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }
}