package com.resume.generator.controller;

import com.resume.generator.dto.AIAnalysisRequest;
import com.resume.generator.dto.ResumeGenerateRequest;
import com.resume.generator.entity.Resume;
import com.resume.generator.entity.User;
import com.resume.generator.repository.UserRepository;
import com.resume.generator.service.AIAnalysisService;
import com.resume.generator.service.FileProcessService;
import com.resume.generator.service.ResumeGenerateService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.security.Principal;

@RestController
@RequestMapping("/api/resume")
public class ResumeController {

    @Autowired
    private AIAnalysisService aiAnalysisService;

    @Autowired
    private ResumeGenerateService resumeGenerateService;

    @Autowired
    private FileProcessService fileProcessService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

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
    public ResponseEntity<Map<String, Object>> generateResume(
            @RequestBody ResumeGenerateRequest request,
            Principal principal) {
        try {
            // 1. Get current user from security context
            User currentUser = userRepository.findByUsername(principal.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // 2. Convert aiAnalysis object to JSON string
            String aiAnalysisJson = objectMapper.writeValueAsString(request.getAiAnalysis());

            // 3. Call service with user's ID
            Resume newResume = resumeGenerateService.generateResume(
                    currentUser.getId(),
                    aiAnalysisJson,
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