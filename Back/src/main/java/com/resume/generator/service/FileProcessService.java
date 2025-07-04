package com.resume.generator.service;

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

@Service
public class FileProcessService {

    @Autowired
    private UserProfileRepository userProfileRepository;

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
}