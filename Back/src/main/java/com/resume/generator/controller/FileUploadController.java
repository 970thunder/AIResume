package com.resume.generator.controller;

import com.resume.generator.dto.FileUploadResponse;
import com.resume.generator.service.FileProcessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
public class FileUploadController {

    @Autowired
    private FileProcessService fileProcessService;

    @PostMapping("/upload")
    public ResponseEntity<FileUploadResponse> uploadFiles(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(value = "sessionId", required = false) String sessionId) {

        try {
            if (sessionId == null || sessionId.isEmpty()) {
                sessionId = UUID.randomUUID().toString();
            }

            List<String> fileNames = new ArrayList<>();
            StringBuilder extractedContent = new StringBuilder();

            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String content = fileProcessService.extractContent(file);
                    extractedContent.append(content).append("\n\n");
                    fileNames.add(file.getOriginalFilename());
                }
            }

            // Save to database
            String content = extractedContent.toString();
            fileProcessService.saveUserProfile(sessionId, fileNames, content);

            return ResponseEntity.ok(new FileUploadResponse(
                    true,
                    "Files uploaded successfully.",
                    sessionId,
                    fileNames,
                    content));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new FileUploadResponse(
                    false,
                    "File upload failed: " + e.getMessage(),
                    null,
                    null,
                    null));
        }
    }
}