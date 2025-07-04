package com.resume.generator.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resume.generator.entity.Resume;
import com.resume.generator.entity.Template;
import com.resume.generator.entity.UserProfile;
import com.resume.generator.repository.ResumeRepository;
import com.resume.generator.repository.UserProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

@Service
public class ResumeGenerateService {

    @Autowired
    private ResumeRepository resumeRepository;
    @Autowired
    private TemplateService templateService;
    @Autowired
    private UserProfileRepository userProfileRepository;

    public Resume generateResume(String sessionId, Long templateId) throws Exception {
        UserProfile userProfile = userProfileRepository.findBySessionId(sessionId);
        if (userProfile == null) {
            throw new RuntimeException("Profile not found for session: " + sessionId);
        }

        Template template = templateService.getTemplateById(templateId);
        if (template == null) {
            throw new RuntimeException("Template not found for id: " + templateId);
        }

        // Create a record in the database without generating a file
        Resume resume = new Resume();
        resume.setUserProfileId(userProfile.getId());
        resume.setTemplateId(templateId);
        resume.setFilePath(null); // No file path as no file is generated
        return resumeRepository.save(resume);
    }
}