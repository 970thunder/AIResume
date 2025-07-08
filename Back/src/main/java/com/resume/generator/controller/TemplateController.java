package com.resume.generator.controller;

import com.resume.generator.dto.TemplateCreateRequest;
import com.resume.generator.entity.Template;
import com.resume.generator.entity.User;
import com.resume.generator.repository.UserRepository;
import com.resume.generator.service.TemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/templates")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TemplateController {

    private final TemplateService templateService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<Template>> getAllTemplates() {
        return ResponseEntity.ok(templateService.getAllTemplates());
    }

    @GetMapping("/free")
    public ResponseEntity<List<Template>> getFreeTemplates() {
        return ResponseEntity.ok(templateService.getFreeTemplates());
    }

    @GetMapping("/premium")
    public ResponseEntity<List<Template>> getPremiumTemplates() {
        return ResponseEntity.ok(templateService.getPremiumTemplates());
    }

    @PostMapping("/create")
    public ResponseEntity<Template> createTemplate(@RequestBody TemplateCreateRequest request,
            Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build(); // Unauthorized
        }
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found with name: " + principal.getName()));

        Template createdTemplate = templateService.createTemplate(request, user);
        return ResponseEntity.ok(createdTemplate);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Template> getTemplate(@PathVariable Long id) {
        Template template = templateService.getTemplateById(id);
        if (template != null) {
            return ResponseEntity.ok(template);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}