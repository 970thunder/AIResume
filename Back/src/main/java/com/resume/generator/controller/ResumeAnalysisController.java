package com.resume.generator.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resume.generator.entity.ResumeAnalysisResult;
import com.resume.generator.entity.User;
import com.resume.generator.repository.ResumeAnalysisResultRepository;
import com.resume.generator.repository.UserRepository;
import com.resume.generator.service.AIAnalysisService;
import com.resume.generator.service.FileProcessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/analysis")
@CrossOrigin(origins = "*")
public class ResumeAnalysisController {

    @Autowired
    private FileProcessService fileProcessService;

    @Autowired
    private AIAnalysisService aiAnalysisService;

    @Autowired
    private ResumeAnalysisResultRepository analysisResultRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping("/career")
    public ResponseEntity<Map<String, Object>> analyzeCareer(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "jobDescription", required = false) String jobDescription,
            Principal principal) {
        try {
            // 1. Extract text from file
            String content = fileProcessService.extractContent(file);

            // 2. AI Analysis
            Map<String, Object> result = aiAnalysisService.analyzeCareer(content, jobDescription);

            // 3. Save analysis result to database
            if (principal != null) {
                String username = principal.getName();
                User user = userRepository.findByUsername(username)
                        .orElseThrow(() -> new RuntimeException("User not found: " + username));

                ResumeAnalysisResult analysisResult = ResumeAnalysisResult.builder()
                        .user(user)
                        .contentJson(objectMapper.writeValueAsString(result))
                        .build();

                analysisResultRepository.save(analysisResult);
            }

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}
