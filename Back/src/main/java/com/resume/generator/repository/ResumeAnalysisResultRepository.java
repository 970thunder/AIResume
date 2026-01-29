package com.resume.generator.repository;

import com.resume.generator.entity.ResumeAnalysisResult;
import com.resume.generator.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResumeAnalysisResultRepository extends JpaRepository<ResumeAnalysisResult, Long> {
    List<ResumeAnalysisResult> findByUser(User user);

    ResumeAnalysisResult findTopByUserOrderByCreatedAtDesc(User user);
}
