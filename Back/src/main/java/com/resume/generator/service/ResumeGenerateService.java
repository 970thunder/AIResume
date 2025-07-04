package com.resume.generator.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.resume.generator.entity.Resume;
import com.resume.generator.entity.Template;
import com.resume.generator.entity.UserProfile;
import com.resume.generator.repository.ResumeRepository;
import com.resume.generator.repository.UserProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

@Service
public class ResumeGenerateService {

    @Autowired
    private ResumeRepository resumeRepository;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private UserProfileRepository userProfileRepository;

    public String generateResume(String sessionId, Long templateId, String aiAnalysis) {
        try {
            // Get user profile
            UserProfile userProfile = userProfileRepository.findBySessionId(sessionId);
            if (userProfile == null) {
                throw new RuntimeException("User profile not found for session: " + sessionId);
            }

            // Get template
            Template template = templateService.getTemplateById(templateId);
            if (template == null) {
                throw new RuntimeException("Template not found for id: " + templateId);
            }

            // Parse AI analysis result
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> analysisData = objectMapper.readValue(aiAnalysis, Map.class);

            // Apply template to generate resume content
            String resumeContent = applyTemplate(template, analysisData);

            // Save resume
            Resume resume = new Resume();
            resume.setUserProfileId(userProfile.getId());
            resume.setTemplateId(templateId);
            resume.setResumeContent(resumeContent);
            resumeRepository.save(resume);

            return resumeContent;

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate resume: " + e.getMessage(), e);
        }
    }

    private String applyTemplate(Template template, Map<String, Object> data) {
        // This is a simplified example. In a real application, you would use a
        // templating engine
        // like Thymeleaf or FreeMarker for more complex and robust template processing.
        StringBuilder content = new StringBuilder();

        // Personal Info
        Map<String, String> personalInfo = (Map<String, String>) data.get("personalInfo");
        content.append("Name: ").append(personalInfo.get("name")).append("\n");
        content.append("Email: ").append(personalInfo.get("email")).append("\n");
        content.append("Phone: ").append(personalInfo.get("phone")).append("\n");
        content.append("Address: ").append(personalInfo.get("address")).append("\n\n");

        // Summary
        content.append("Summary:\n");
        content.append(data.get("summary")).append("\n\n");

        // Experience
        content.append("Experience:\n");
        List<Map<String, String>> experience = (List<Map<String, String>>) data.get("experience");
        for (Map<String, String> exp : experience) {
            content.append("- ").append(exp.get("position")).append(" at ").append(exp.get("company"));
            content.append(" (").append(exp.get("duration")).append(")\n");
            content.append("  ").append(exp.get("description")).append("\n");
        }

        return content.toString();
    }

    public Resource generatePDF(Long resumeId) throws Exception {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new RuntimeException("Resume not found with id: " + resumeId));

        // Generate PDF using iText
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, outputStream);

        document.open();
        document.add(new Paragraph(resume.getResumeContent()));
        document.close();

        return new ByteArrayResource(outputStream.toByteArray());
    }
}