package com.resume.generator.repository;

import com.resume.generator.entity.InterviewRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InterviewRecordRepository extends JpaRepository<InterviewRecord, Long> {
    List<InterviewRecord> findByUserId(Long userId);
    List<InterviewRecord> findBySessionId(Long sessionId);
    
    @Query("SELECT count(r) FROM InterviewRecord r WHERE r.user.id = :userId")
    Long countByUserId(@Param("userId") Long userId);
    
    @Query("SELECT count(r) FROM InterviewRecord r WHERE r.user.id = :userId AND r.isCorrect = true")
    Long countCorrectByUserId(@Param("userId") Long userId);
    
    @Query("SELECT r FROM InterviewRecord r WHERE r.user.id = :userId AND r.isCorrect = false ORDER BY r.createdAt DESC")
    List<InterviewRecord> findWrongAnswersByUserId(@Param("userId") Long userId);
}
