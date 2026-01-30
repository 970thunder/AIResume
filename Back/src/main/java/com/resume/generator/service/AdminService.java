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
import java.util.HashMap;
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
    private final InterviewRecordRepository recordRepository;
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
        List<InterviewQuestion> questions = questionRepository.findAll();
        List<Object[]> stats = recordRepository.getQuestionStatistics();

        Map<Long, Long[]> statsMap = new HashMap<>();
        for (Object[] row : stats) {
            Long qId = (Long) row[0];
            Long total = ((Number) row[1]).longValue();
            Long correct = row[2] == null ? 0L : ((Number) row[2]).longValue();
            statsMap.put(qId, new Long[] { total, correct });
        }

        for (InterviewQuestion q : questions) {
            if (statsMap.containsKey(q.getId())) {
                Long[] s = statsMap.get(q.getId());
                q.setTotalAttempts(s[0]);
                q.setCorrectCount(s[1]);
            } else {
                q.setTotalAttempts(0L);
                q.setCorrectCount(0L);
            }
        }
        return questions;
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

    @Transactional
    public User updateUser(Long id, User updatedUser) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        // Update email
        user.setEmail(updatedUser.getEmail());

        // Update Roles if provided
        if (updatedUser.getRoles() != null && !updatedUser.getRoles().isEmpty()) {
            // In a real app, you'd fetch roles from RoleRepository to ensure they exist and
            // are persistent
            // Here assuming updatedUser.getRoles() contains valid role entities or at least
            // names to look up
            // For simplicity, let's look up roles by name or ID if passed
            java.util.Set<Role> newRoles = new HashSet<>();
            for (Role r : updatedUser.getRoles()) {
                roleRepository.findByName(r.getName())
                        .ifPresent(newRoles::add);
            }
            if (!newRoles.isEmpty()) {
                user.setRoles(newRoles);
            }
        }

        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        // Prevent deleting self or super admin if needed, but for now simple delete
        userRepository.deleteById(id);
    }
}
