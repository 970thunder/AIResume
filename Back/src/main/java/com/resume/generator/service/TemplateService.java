package com.resume.generator.service;

import com.resume.generator.entity.Template;
import com.resume.generator.repository.TemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TemplateService {

    @Autowired
    private TemplateRepository templateRepository;

    public List<Template> getAllTemplates() {
        return templateRepository.findAll();
    }

    public Template getTemplateById(Long id) {
        return templateRepository.findById(id).orElse(null);
    }

    public List<Template> getFreeTemplates() {
        return templateRepository.findByType("free");
    }

    public List<Template> getPremiumTemplates() {
        return templateRepository.findByType("premium");
    }
}