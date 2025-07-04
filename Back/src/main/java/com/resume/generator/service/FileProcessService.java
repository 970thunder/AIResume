package com.resume.generator.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resume.generator.entity.UserProfile;
import com.resume.generator.repository.UserProfileRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Service
public class FileProcessService {

    @Autowired
    private UserProfileRepository userProfileRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String extractContent(MultipartFile file) throws IOException {
        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new IllegalArgumentException("File must have a name.");
        }
        String extension = filename.substring(filename.lastIndexOf(".")).toLowerCase();

        switch (extension) {
            case ".pdf":
                return extractPDFContent(file);
            case ".doc":
            case ".docx":
                return extractWordContent(file);
            case ".txt":
                return new String(file.getBytes(), StandardCharsets.UTF_8);
            default:
                throw new IllegalArgumentException("Unsupported file type: " + extension);
        }
    }

    private String extractPDFContent(MultipartFile file) throws IOException {
        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    private String extractWordContent(MultipartFile file) throws IOException {
        try (XWPFDocument document = new XWPFDocument(file.getInputStream())) {
            XWPFWordExtractor extractor = new XWPFWordExtractor(document);
            return extractor.getText();
        }
    }

    public void saveUserProfile(String sessionId, List<String> fileNames, String extractedContent) {
        UserProfile profile = new UserProfile();
        profile.setSessionId(sessionId);
        profile.setOriginalFiles(String.join(",", fileNames));
        profile.setExtractedContent(extractedContent);
        userProfileRepository.save(profile);
    }

    public void saveAiAnalysis(String sessionId, Map<String, Object> aiAnalysis) {
        UserProfile profile = userProfileRepository.findBySessionId(sessionId);
        if (profile != null) {
            try {
                String aiAnalysisString = objectMapper.writeValueAsString(aiAnalysis);
                profile.setAiAnalysis(aiAnalysisString);
                userProfileRepository.save(profile);
            } catch (JsonProcessingException e) {
                // In a real application, you'd likely use a logger
                System.err.println("Could not serialize AI analysis to save to DB for session ID: " + sessionId);
                throw new RuntimeException("Failed to serialize AI analysis", e);
            }
        } else {
            // In a real application, you might want more robust error handling
            System.err.println("Could not find profile for session ID to save analysis: " + sessionId);
        }
    }
}