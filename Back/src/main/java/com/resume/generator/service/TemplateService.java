package com.resume.generator.service;

import com.resume.generator.dto.TemplateCreateRequest;
import com.resume.generator.entity.Template;
import com.resume.generator.entity.TemplateStatus;
import com.resume.generator.entity.User;
import com.resume.generator.repository.TemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TemplateService {

    private final TemplateRepository templateRepository;

    public List<Template> getAllTemplates() {
        return templateRepository.findByStatus(TemplateStatus.APPROVED);
    }

    public List<Template> getAllTemplatesForAdmin() {
        return templateRepository.findAll();
    }

    public Template getTemplateById(Long id) {
        return templateRepository.findById(id).orElse(null);
    }

    public List<Template> getFreeTemplates() {
        return templateRepository.findByStatus(TemplateStatus.APPROVED).stream()
                .filter(template -> "free".equalsIgnoreCase(template.getType()))
                .collect(Collectors.toList());
    }

    public List<Template> getPremiumTemplates() {
        return templateRepository.findByStatus(TemplateStatus.APPROVED).stream()
                .filter(template -> "premium".equalsIgnoreCase(template.getType()))
                .collect(Collectors.toList());
    }

    public Template createTemplate(TemplateCreateRequest request, User author) {
        Template template = new Template();
        template.setName(request.getName());
        template.setDescription(request.getDescription());
        template.setHtmlContent(request.getHtmlContent());
        template.setAuthor(author);

        // FIX: Set default values for required fields that were missed
        template.setType("premium"); // Default type for user-submitted templates
        template.setPrice(BigDecimal.ZERO); // Default price is 0
        template.setPreviewImage("default_preview.png"); // Placeholder for preview image

        // Status and likes have default values in the entity, no need to set here
        return templateRepository.save(template);
    }

    public Template updateTemplateStatus(Long id, String status) {
        Template template = templateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Template not found with id: " + id));
        template.setStatus(TemplateStatus.valueOf(status));
        return templateRepository.save(template);
    }
}