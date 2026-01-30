package com.resume.generator.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resume.generator.dto.AdminStatsDTO;
import com.resume.generator.dto.RegisterRequest;
import com.resume.generator.entity.*;
import com.resume.generator.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final TemplateRepository templateRepository;
    private final InterviewQuestionRepository questionRepository;
    private final InterviewSessionRepository sessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final AIAnalysisService aiAnalysisService;
    
    public boolean isAdminInitialized() {
        return userRepository.existsByRoles_Name("ROLE_ADMIN");
    }

    @Transactional
    public void initAdmin(RegisterRequest request) {
        if (isAdminInitialized()) {
            throw new RuntimeException("Admin already initialized");
        }

        // Check if username already exists
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }
        
        // Check if email already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_ADMIN")));

        User admin = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .roles(new HashSet<>(Collections.singletonList(adminRole)))
                .build();

        userRepository.save(admin);
    }

    public AdminStatsDTO getStats() {
        return AdminStatsDTO.builder()
                .totalUsers(userRepository.count())
                .totalTemplates(templateRepository.count())
                .pendingTemplates(templateRepository.countByStatus(TemplateStatus.PENDING))
                .totalQuestions(questionRepository.count())
                .activeSessionsToday(sessionRepository.countByCreatedAtAfter(LocalDate.now().atStartOfDay()))
                .build();
    }

    public List<Template> getPendingTemplates() {
        return templateRepository.findByStatus(TemplateStatus.PENDING);
    }

    @Transactional
    public void auditTemplate(Long templateId, boolean approved, String reason) {
        Template template = templateRepository.findById(templateId)
                .orElseThrow(() -> new RuntimeException("Template not found"));
        
        if (approved) {
            template.setStatus(TemplateStatus.APPROVED);
            template.setAuditComment(null); // Clear previous rejection reason if approved
        } else {
            template.setStatus(TemplateStatus.REJECTED);
            template.setAuditComment(reason);
        }
        templateRepository.save(template);
    }

    // Question Management
    public List<InterviewQuestion> getAllQuestions() {
        return questionRepository.findAll();
    }

    @Transactional
    public InterviewQuestion createQuestion(InterviewQuestion question) {
        return questionRepository.save(question);
    }

    @Transactional
    public InterviewQuestion updateQuestion(Long id, InterviewQuestion updated) {
        InterviewQuestion q = questionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Question not found"));
        q.setContent(updated.getContent());
        q.setType(updated.getType());
        q.setOptions(updated.getOptions());
        q.setReferenceAnswer(updated.getReferenceAnswer());
        q.setDifficulty(updated.getDifficulty());
        q.setCategory(updated.getCategory());
        q.setTags(updated.getTags());
        return questionRepository.save(q);
    }

    @Transactional
    public void deleteQuestion(Long id) {
        questionRepository.deleteById(id);
    }

    @Transactional
    public List<InterviewQuestion> generateQuestions(int count, String category) {
        List<Map<String, Object>> generated = aiAnalysisService.generateBatchInterviewQuestions(count, category);
        List<InterviewQuestion> saved = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();

        for (Map<String, Object> map : generated) {
            try {
                String optionsJson = map.get("options") != null ? mapper.writeValueAsString(map.get("options")) : null;
                InterviewQuestion q = InterviewQuestion.builder()
                        .content((String) map.get("content"))
                        .type((String) map.get("type"))
                        .options(optionsJson)
                        .referenceAnswer((String) map.get("referenceAnswer"))
                        .difficulty((String) map.get("difficulty"))
                        .category((String) map.get("category"))
                        .tags((String) map.get("tags"))
                        .build();
                saved.add(questionRepository.save(q));
            } catch (Exception e) {
                // Ignore malformed questions
            }
        }
        return saved;
    }

    // User Management
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}
