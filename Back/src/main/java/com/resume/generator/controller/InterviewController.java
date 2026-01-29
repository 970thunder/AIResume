package com.resume.generator.controller;

import com.resume.generator.dto.InterviewRecordDTO;
import com.resume.generator.dto.InterviewSessionDTO;
import com.resume.generator.dto.InterviewStatsDTO;
import com.resume.generator.entity.User;
import com.resume.generator.repository.UserRepository;
import com.resume.generator.service.InterviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/interview")
public class InterviewController {

    @Autowired
    private InterviewService interviewService;

    @Autowired
    private UserRepository userRepository;

    private User getUserByPrincipal(Principal principal) {
        return userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found: " + principal.getName()));
    }

    @PostMapping("/start")
    public ResponseEntity<InterviewSessionDTO> startSession(Principal principal) {
        User user = getUserByPrincipal(principal);
        InterviewSessionDTO session = interviewService.startNewSession(user.getId());
        return ResponseEntity.ok(session);
    }

    @PostMapping("/submit")
    public ResponseEntity<InterviewRecordDTO> submitAnswer(
            @RequestBody Map<String, Object> payload) {
        // Expected payload: sessionId, questionId, answer
        Long sessionId = Long.valueOf(payload.get("sessionId").toString());
        Long questionId = Long.valueOf(payload.get("questionId").toString());
        String answer = (String) payload.get("answer");

        InterviewRecordDTO record = interviewService.submitAnswer(sessionId, questionId, answer);
        return ResponseEntity.ok(record);
    }

    @GetMapping("/stats")
    public ResponseEntity<InterviewStatsDTO> getStats(Principal principal) {
        User user = getUserByPrincipal(principal);
        InterviewStatsDTO stats = interviewService.getUserStats(user.getId());
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/analysis/latest")
    public ResponseEntity<String> getLatestAnalysis(Principal principal) {
        User user = getUserByPrincipal(principal);
        String json = interviewService.getLatestAnalysisJson(user.getId());
        if (json == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(json);
    }
}
