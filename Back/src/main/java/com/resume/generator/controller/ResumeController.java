package com.resume.generator.controller;

import com.resume.generator.dto.AIAnalysisRequest;
import com.resume.generator.dto.ResumeGenerateRequest;
import com.resume.generator.service.AIAnalysisService;
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
@CrossOrigin(origins = "*")
public class ResumeController {

    @Autowired
    private AIAnalysisService aiAnalysisService;

    @Autowired
    private ResumeGenerateService resumeGenerateService;

    @PostMapping("/analyze")
    public ResponseEntity<Map<String, Object>> analyzeProfile(@RequestBody AIAnalysisRequest request) {
        try {
            String aiAnalysis = aiAnalysisService.analyzeProfile(request.getExtractedContent());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("analysis", aiAnalysis);
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
            String resumeContent = resumeGenerateService.generateResume(
                    request.getSessionId(),
                    request.getTemplateId(),
                    request.getAiAnalysis());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("resumeContent", resumeContent);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Resume generation failed: " + e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/download/{resumeId}")
    public ResponseEntity<Resource> downloadResume(@PathVariable Long resumeId) {
        try {
            Resource resource = resumeGenerateService.generatePDF(resumeId);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"resume.pdf\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}