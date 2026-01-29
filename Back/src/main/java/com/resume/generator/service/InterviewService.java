package com.resume.generator.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resume.generator.dto.InterviewQuestionDTO;
import com.resume.generator.dto.InterviewRecordDTO;
import com.resume.generator.dto.InterviewSessionDTO;
import com.resume.generator.dto.InterviewStatsDTO;
import com.resume.generator.entity.*;
import com.resume.generator.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Async;
import java.util.concurrent.CompletableFuture;

@Service
public class InterviewService {

    @Autowired
    private InterviewQuestionRepository questionRepository;
    @Autowired
    private InterviewSessionRepository sessionRepository;
    @Autowired
    private InterviewRecordRepository recordRepository;
    @Autowired
    private ResumeAnalysisResultRepository analysisResultRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AIAnalysisService aiAnalysisService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public InterviewSessionDTO startNewSession(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 1. Get tech stack from latest resume analysis
        String techStack = "Java"; // Default fallback
        String keyword = "Java";
        ResumeAnalysisResult analysis = analysisResultRepository.findTopByUserOrderByCreatedAtDesc(user);

        if (analysis != null && analysis.getContentJson() != null) {
            try {
                JsonNode root = objectMapper.readTree(analysis.getContentJson());
                JsonNode skillsNode = root.path("skills").path("technicalSkills");
                if (skillsNode.isArray() && skillsNode.size() > 0) {
                    // Create a comma-separated string for AI generation
                    List<String> skills = new ArrayList<>();
                    skillsNode.forEach(s -> skills.add(s.asText()));
                    techStack = String.join(", ", skills);

                    // Use the first skill as keyword for DB search
                    keyword = skills.get(0);
                } else if (root.has("jobTitle")) {
                    techStack = root.get("jobTitle").asText();
                    keyword = techStack;
                }
            } catch (JsonProcessingException e) {
                // Ignore parse error, use default
            }
        }

        // 2. Fetch questions
        List<InterviewQuestion> questions = new ArrayList<>();

        // Try with keyword
        questions.addAll(questionRepository.findRandomQuestionsNotAnsweredWithKeyword(userId, keyword, 10));

        // If not enough, try generic unattempted
        if (questions.size() < 10) {
            int needed = 10 - questions.size();
            List<InterviewQuestion> generic = questionRepository.findRandomQuestionsNotAnswered(userId, needed);
            // Avoid duplicates (though the query should handle it, mixing queries might
            // not)
            // The second query excludes answered, but might include ones we just picked if
            // we didn't save a record yet?
            // Actually, "NotAnswered" checks interview_records. We haven't created records
            // yet.
            // So we need to filter locally.
            Set<Long> pickedIds = questions.stream().map(InterviewQuestion::getId).collect(Collectors.toSet());
            for (InterviewQuestion q : generic) {
                if (!pickedIds.contains(q.getId())) {
                    questions.add(q);
                    pickedIds.add(q.getId());
                }
            }
        }

        // 3. If still not enough, generate with AI
        if (questions.size() < 10) {
            try {
                int needed = 10 - questions.size();
                // Generate slightly more to ensure we get valid ones, but 10 is the batch size
                // usually.
                // The AI service generates 10 fixed. We'll take what we need.
                List<Map<String, Object>> generated = aiAnalysisService.generateInterviewQuestions(techStack);

                for (Map<String, Object> map : generated) {
                    if (questions.size() >= 10)
                        break;

                    String content = (String) map.get("content");
                    // Check if similar question exists? Skipping for now.

                    InterviewQuestion q = InterviewQuestion.builder()
                            .content(content)
                            .type((String) map.getOrDefault("type", "OPEN_ENDED"))
                            .options(objectMapper.writeValueAsString(map.get("options")))
                            .referenceAnswer((String) map.get("referenceAnswer"))
                            .difficulty((String) map.getOrDefault("difficulty", "MEDIUM"))
                            .category((String) map.getOrDefault("category", "General"))
                            .tags((String) map.getOrDefault("tags", keyword))
                            .build();

                    q = questionRepository.save(q);
                    questions.add(q);
                }
            } catch (Exception e) {
                // If AI fails, and we have at least some questions, proceed.
                // If 0 questions, we might need to fail or return empty?
                if (questions.isEmpty()) {
                    throw new RuntimeException("Could not generate questions: " + e.getMessage());
                }
            }
        }

        // 4. Create Session
        InterviewSession session = InterviewSession.builder()
                .user(user)
                .status("IN_PROGRESS")
                .totalQuestions(questions.size())
                .score(0)
                .build();
        session = sessionRepository.save(session);

        // 5. Convert to DTOs
        // We hide reference answer for the frontend
        List<InterviewQuestionDTO> questionDTOs = questions.stream().map(q -> {
            try {
                return InterviewQuestionDTO.builder()
                        .id(q.getId())
                        .content(q.getContent())
                        .type(q.getType())
                        .options(q.getOptions() != null ? objectMapper.readValue(q.getOptions(), List.class) : null)
                        .difficulty(q.getDifficulty())
                        .category(q.getCategory())
                        .tags(q.getTags())
                        .build();
            } catch (JsonProcessingException e) {
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());

        return InterviewSessionDTO.builder()
                .id(session.getId())
                .userId(userId)
                .status(session.getStatus())
                .totalQuestions(session.getTotalQuestions())
                .score(session.getScore())
                .createdAt(session.getCreatedAt())
                .questions(questionDTOs)
                .build();
    }

    @Transactional
    public InterviewRecordDTO submitAnswer(Long sessionId, Long questionId, String userAnswer) {
        InterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));
        InterviewQuestion question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));

        // Save Record immediately with pending state
        InterviewRecord record = InterviewRecord.builder()
                .session(session)
                .user(session.getUser())
                .question(question)
                .userAnswer(userAnswer)
                .aiEvaluation("Pending evaluation...")
                .score(0)
                .isCorrect(false) // Default, will update later
                .build();

        record = recordRepository.save(record);

        // Trigger Async Evaluation
        processAnswerAsync(record.getId(), question.getContent(), userAnswer);

        return InterviewRecordDTO.builder()
                .id(record.getId())
                .sessionId(sessionId)
                .questionId(questionId)
                .userAnswer(userAnswer)
                .aiEvaluation("Evaluating...")
                .score(0)
                .isCorrect(false)
                .createdAt(record.getCreatedAt())
                .build();
    }

    @Async
    public void processAnswerAsync(Long recordId, String questionContent, String userAnswer) {
        try {
            // Evaluate Answer
            Map<String, Object> evaluation = aiAnalysisService.evaluateAnswer(questionContent, userAnswer);

            boolean isCorrect = (Boolean) evaluation.getOrDefault("isCorrect", false);
            Integer score = (Integer) evaluation.getOrDefault("score", 0);
            String feedback = (String) evaluation.getOrDefault("evaluation", "");

            // Update Record in new transaction
            updateRecord(recordId, isCorrect, score, feedback);
        } catch (Exception e) {
            System.err.println("Async evaluation failed for record " + recordId + ": " + e.getMessage());
        }
    }

    @Transactional
    public void updateRecord(Long recordId, boolean isCorrect, Integer score, String feedback) {
        InterviewRecord record = recordRepository.findById(recordId).orElse(null);
        if (record == null)
            return;

        record.setIsCorrect(isCorrect);
        record.setScore(score);
        record.setAiEvaluation(feedback);
        recordRepository.save(record);

        // Update Session Score
        InterviewSession session = record.getSession();
        int currentScore = session.getScore() != null ? session.getScore() : 0;
        session.setScore(currentScore + score);

        // Check if session complete
        List<InterviewRecord> sessionRecords = recordRepository.findBySessionId(session.getId());
        if (sessionRecords.size() >= session.getTotalQuestions()) {
            session.setStatus("COMPLETED");
        }
        sessionRepository.save(session);
    }

    public InterviewStatsDTO getUserStats(Long userId) {
        Long totalAnswered = recordRepository.countByUserId(userId);
        Long correctCount = recordRepository.countCorrectByUserId(userId);

        // Accuracy
        double accuracy = totalAnswered > 0 ? (double) correctCount / totalAnswered * 100 : 0.0;

        // Wrong Questions
        List<InterviewRecord> wrongRecords = recordRepository.findWrongAnswersByUserId(userId);
        List<InterviewRecordDTO> wrongQuestions = wrongRecords.stream().map(r -> {
            try {
                // Need to fetch question content?
                InterviewQuestion q = r.getQuestion();
                return InterviewRecordDTO.builder()
                        .id(r.getId())
                        .questionContent(q.getContent())
                        .userAnswer(r.getUserAnswer())
                        .aiEvaluation(r.getAiEvaluation())
                        .isCorrect(r.getIsCorrect())
                        .createdAt(r.getCreatedAt())
                        .build();
            } catch (Exception e) {
                return null;
            }
        }).collect(Collectors.toList());

        return InterviewStatsDTO.builder()
                .totalQuestionsAnswered(totalAnswered.intValue())
                .totalCorrect(correctCount.intValue())
                .accuracy(accuracy)
                .wrongRecords(wrongQuestions)
                .build();
    }

    public String getLatestAnalysisJson(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null)
            return null;
        ResumeAnalysisResult analysis = analysisResultRepository.findTopByUserOrderByCreatedAtDesc(user);
        return analysis != null ? analysis.getContentJson() : null;
    }
}
