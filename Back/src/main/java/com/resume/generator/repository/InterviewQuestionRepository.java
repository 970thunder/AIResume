package com.resume.generator.repository;

import com.resume.generator.entity.InterviewQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InterviewQuestionRepository extends JpaRepository<InterviewQuestion, Long> {

    @Query(value = "SELECT * FROM interview_questions q WHERE q.id NOT IN (SELECT r.question_id FROM interview_records r WHERE r.user_id = :userId) ORDER BY RAND() LIMIT :limit", nativeQuery = true)
    List<InterviewQuestion> findRandomQuestionsNotAnswered(@Param("userId") Long userId, @Param("limit") int limit);
    
    @Query(value = "SELECT * FROM interview_questions q WHERE q.id NOT IN (SELECT r.question_id FROM interview_records r WHERE r.user_id = :userId) AND (q.tags LIKE %:keyword% OR q.category LIKE %:keyword%) ORDER BY RAND() LIMIT :limit", nativeQuery = true)
    List<InterviewQuestion> findRandomQuestionsNotAnsweredWithKeyword(@Param("userId") Long userId, @Param("keyword") String keyword, @Param("limit") int limit);
}
