package com.resume.generator.controller;

import com.resume.generator.entity.Template;
import com.resume.generator.service.TemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final TemplateService templateService;

    @GetMapping("/templates")
    public ResponseEntity<List<Template>> getAllTemplatesForAdmin() {
        return ResponseEntity.ok(templateService.getAllTemplatesForAdmin());
    }

    @PutMapping("/templates/{id}/status")
    public ResponseEntity<Template> updateTemplateStatus(@PathVariable Long id,
            @RequestBody Map<String, String> statusUpdate) {
        String status = statusUpdate.get("status");
        if (status == null || (!status.equalsIgnoreCase("APPROVED") && !status.equalsIgnoreCase("REJECTED")
                && !status.equalsIgnoreCase("PENDING"))) {
            return ResponseEntity.badRequest().build();
        }
        Template updatedTemplate = templateService.updateTemplateStatus(id, status.toUpperCase());
        return ResponseEntity.ok(updatedTemplate);
    }
}