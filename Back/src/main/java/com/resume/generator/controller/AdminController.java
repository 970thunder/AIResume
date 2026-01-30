package com.resume.generator.controller;

import com.resume.generator.dto.AdminInitRequest;
import com.resume.generator.dto.AdminStatsDTO;
import com.resume.generator.dto.RegisterRequest;
import com.resume.generator.entity.InterviewQuestion;
import com.resume.generator.entity.Template;
import com.resume.generator.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/check-init")
    public ResponseEntity<Boolean> checkInit() {
        return ResponseEntity.ok(adminService.isAdminInitialized());
    }

    @PostMapping("/init")
    public ResponseEntity<?> initAdmin(@RequestBody AdminInitRequest request) {
        // Convert to RegisterRequest structure (ignoring verification code)
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(request.getPassword())
                .build();
        
        adminService.initAdmin(registerRequest);
        return ResponseEntity.ok("Admin initialized successfully");
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminStatsDTO> getStats() {
        return ResponseEntity.ok(adminService.getStats());
    }

    // Template Audit
    @GetMapping("/templates/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Template>> getPendingTemplates() {
        return ResponseEntity.ok(adminService.getPendingTemplates());
    }

    @PostMapping("/templates/{id}/audit")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> auditTemplate(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        boolean approved = (Boolean) body.get("approved");
        String reason = (String) body.get("reason");
        adminService.auditTemplate(id, approved, reason);
        return ResponseEntity.ok().build();
    }

    // Question Management
    @GetMapping("/questions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<InterviewQuestion>> getAllQuestions() {
        return ResponseEntity.ok(adminService.getAllQuestions());
    }

    @PostMapping("/questions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InterviewQuestion> createQuestion(@RequestBody InterviewQuestion question) {
        return ResponseEntity.ok(adminService.createQuestion(question));
    }

    @PutMapping("/questions/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InterviewQuestion> updateQuestion(
            @PathVariable Long id,
            @RequestBody InterviewQuestion question) {
        return ResponseEntity.ok(adminService.updateQuestion(id, question));
    }

    @DeleteMapping("/questions/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteQuestion(@PathVariable Long id) {
        adminService.deleteQuestion(id);
        return ResponseEntity.ok().build();
    }
}
