package com.resume.generator.service;

import com.resume.generator.entity.Resume;
import com.resume.generator.entity.User;
import com.resume.generator.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ResumeService {

    private final ResumeRepository resumeRepository;

    @Transactional(readOnly = true)
    public List<Resume> findResumesByUser(User user) {
        return resumeRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
    }

    @Transactional(readOnly = true)
    public Resume findResumeByIdAndUser(Long resumeId, User user) {
        return resumeRepository.findByIdAndUserId(resumeId, user.getId())
                .orElseThrow(() -> new RuntimeException("Resume not found or access denied"));
    }

    @Transactional
    public Resume updateResume(Long resumeId, User user, String aiAnalysisData, Long templateId) {
        Resume resume = findResumeByIdAndUser(resumeId, user);
        resume.setAiAnalysisData(aiAnalysisData);
        resume.setTemplateId(templateId);
        return resumeRepository.save(resume);
    }

    @Transactional
    public Resume updateResumeTitle(Long resumeId, String title, User user) {
        Resume resume = findResumeByIdAndUser(resumeId, user);
        resume.setTitle(title);
        return resumeRepository.save(resume);
    }

    @Transactional
    public void deleteResume(Long resumeId, User user) {
        Resume resume = findResumeByIdAndUser(resumeId, user);
        resumeRepository.delete(resume);
    }
}