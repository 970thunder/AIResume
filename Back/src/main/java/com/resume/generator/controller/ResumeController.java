package com.resume.generator.controller;

import com.resume.generator.dto.AIAnalysisRequest;
import com.resume.generator.dto.ResumeGenerateRequest;
import com.resume.generator.dto.ResumeUpdateRequest;
import com.resume.generator.entity.Resume;
import com.resume.generator.entity.User;
import com.resume.generator.repository.UserRepository;
import com.resume.generator.service.AIAnalysisService;
import com.resume.generator.service.FileProcessService;
import com.resume.generator.service.ResumeGenerateService;
import com.resume.generator.service.ResumeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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

    @Autowired
    private ResumeService resumeService;

    private User getUserByPrincipal(Principal principal) {
        return userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found: " + principal.getName()));
    }

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
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to generate resume: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @GetMapping("/history")
    public ResponseEntity<List<Resume>> getResumesHistory(Principal principal) {
        User user = getUserByPrincipal(principal);
        return ResponseEntity.ok(resumeService.findResumesByUser(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Resume> getResumeDetails(@PathVariable Long id, Principal principal) {
        User user = getUserByPrincipal(principal);
        return ResponseEntity.ok(resumeService.findResumeByIdAndUser(id, user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Resume> updateResume(
            @PathVariable Long id,
            @RequestBody ResumeUpdateRequest request,
            Principal principal) {
        User user = getUserByPrincipal(principal);
        Resume updatedResume = resumeService.updateResume(
                id,
                user,
                request.getAiAnalysisData(),
                request.getTemplateId());
        return ResponseEntity.ok(updatedResume);
    }

    @PutMapping("/{id}/title")
    public ResponseEntity<Resume> updateResumeTitle(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload,
            Principal principal) {
        User user = getUserByPrincipal(principal);
        String newTitle = payload.get("title");
        if (newTitle == null || newTitle.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(resumeService.updateResumeTitle(id, newTitle, user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteResume(@PathVariable Long id, Principal principal) {
        User user = getUserByPrincipal(principal);
        resumeService.deleteResume(id, user);
        return ResponseEntity.noContent().build();
    }
}