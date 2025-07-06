package com.resume.generator.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resume.generator.entity.Resume;
import com.resume.generator.entity.Template;
import com.resume.generator.repository.ResumeRepository;
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
    private ObjectMapper objectMapper;

    public Resume generateResume(Long userId, String aiAnalysisJson, Long templateId) throws Exception {

        Template template = templateService.getTemplateById(templateId);
        if (template == null) {
            throw new RuntimeException("Template not found for id: " + templateId);
        }

        Resume resume = new Resume();
        resume.setUserId(userId);
        resume.setTemplateId(templateId);
        resume.setFilePath(null);
        resume.setAiAnalysisData(aiAnalysisJson);

        Resume savedResume = resumeRepository.save(resume);

        Map<String, Object> resumeDataMap = objectMapper.readValue(aiAnalysisJson, new TypeReference<>() {
        });
        savedResume.setResumeData(resumeDataMap);

        return savedResume;
    }
}