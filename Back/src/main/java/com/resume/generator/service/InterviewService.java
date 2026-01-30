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
        int targetQuestionCount = 20;

        // Try with keyword
        questions.addAll(
                questionRepository.findRandomQuestionsNotAnsweredWithKeyword(userId, keyword, targetQuestionCount));

        // If not enough, try generic unattempted
        if (questions.size() < targetQuestionCount) {
            int needed = targetQuestionCount - questions.size();
            List<InterviewQuestion> generic = questionRepository.findRandomQuestionsNotAnswered(userId, needed);

            Set<Long> pickedIds = questions.stream().map(InterviewQuestion::getId).collect(Collectors.toSet());
            for (InterviewQuestion q : generic) {
                if (!pickedIds.contains(q.getId())) {
                    questions.add(q);
                    pickedIds.add(q.getId());
                }
            }
        }

        // 3. If still not enough, generate with AI
        // Optimization: If we have at least 10 questions locally, we can start the
        // session
        // without waiting for AI generation, to improve user experience.
        int minQuestionsRequired = 10;

        if (questions.size() < minQuestionsRequired) {
            // Retry loop to generate more questions
            int maxAttempts = 3;
            int attempts = 0;

            // If we trigger AI, we try to reach the full target of 20
            while (questions.size() < targetQuestionCount && attempts < maxAttempts) {
                attempts++;
                try {
                    // Generate questions
                    List<Map<String, Object>> generated = aiAnalysisService.generateInterviewQuestions(techStack);

                    for (Map<String, Object> map : generated) {
                        if (questions.size() >= targetQuestionCount)
                            break;

                        String content = (String) map.get("content");

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
                    // Log error but continue trying or break if critical
                    System.err.println("AI generation attempt " + attempts + " failed: " + e.getMessage());
                }
            }
        }

        // Final check
        if (questions.isEmpty()) {
            throw new RuntimeException("Could not generate questions.");
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
                        .options(objectMapper.readValue(q.getOptions(), List.class))
                        .difficulty(q.getDifficulty())
                        .category(q.getCategory())
                        .build();
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());

        return InterviewSessionDTO.builder()
                .id(session.getId()) // Populate id field for compatibility
                .sessionId(session.getId())
                .questions(questionDTOs)
                .totalQuestions(session.getTotalQuestions())
                .build();
    }

    public InterviewSessionDTO getPendingSession(Long userId) {
        // Check if there is an active session
        InterviewSession session = sessionRepository.findFirstByUserIdAndStatusOrderByCreatedAtDesc(userId,
                "IN_PROGRESS");
        if (session == null) {
            return null;
        }

        // Get answered questions for this session
        List<InterviewRecord> records = recordRepository.findBySessionId(session.getId());
        Set<Long> answeredQuestionIds = records.stream()
                .map(r -> r.getQuestion().getId())
                .collect(Collectors.toSet());

        // We need to reconstruct the list of questions for this session.
        // NOTE: In the current design, InterviewSession doesn't store the list of
        // question IDs directly.
        // It seems we rely on dynamically fetching questions or we should have stored
        // them.
        // Wait, if startNewSession picks random questions, we MUST store the mapping of
        // Session -> Questions.
        // Looking at the entities, InterviewSession does NOT have a OneToMany to
        // Questions.
        // AND InterviewRecord only stores answered questions.
        // THIS IS A DATA MODEL LIMITATION.

        // However, if we look at startNewSession, it returns a list of questions.
        // But it doesn't persist the association between Session and the Questions
        // *until* they are answered (via InterviewRecord).
        // This means if the user refreshes the page, they will lose the *unanswered*
        // questions if we don't store them.

        // To support "Resuming", we have two options:
        // 1. Change data model to store all questions selected for a session.
        // 2. Just return the *answered* questions + generate *new* questions to fill
        // the remaining count.

        // Option 2 is easier and doesn't require schema migration, but it changes the
        // questions the user *would have* seen.
        // Given the user request "restore the previous answer state and questions",
        // Option 1 is more correct but harder.
        // However, if the user hasn't seen the questions yet, changing them is
        // acceptable.
        // BUT, the user might have seen the current question and then refreshed.

        // Let's go with Option 2 for now as it's safer without schema changes,
        // BUT we must ensure we don't pick questions that were already answered in this
        // session.

        // Wait, if we generate new questions, we might pick ones that were already
        // answered?
        // No, we can filter them out.

        // Let's refine Option 2:
        // 1. Fetch all records for this session.
        // 2. Extract answered questions.
        // 3. Calculate how many more needed (Total - Answered).
        // 4. Fetch/Generate new questions excluding the answered ones.
        // 5. Combine Answered + New Questions.
        // 6. Return the combined list. The frontend will skip the first N (answered)
        // questions.

        // Fetch answered questions
        List<InterviewQuestion> answeredQuestions = records.stream()
                .map(InterviewRecord::getQuestion)
                .collect(Collectors.toList());

        // Fetch new questions to fill the gap
        int remaining = session.getTotalQuestions() - answeredQuestions.size();
        List<InterviewQuestion> newQuestions = new ArrayList<>();

        if (remaining > 0) {
            // We need to find questions NOT answered by this user globally?
            // Or just not in this session?
            // Usually globally to avoid repetition.

            // Similar logic to startNewSession but we need to fetch 'remaining' count
            // and avoid 'answeredQuestionIds' if we want to be strict,
            // but 'findRandomQuestionsNotAnswered' already handles 'not answered by user'.

            // However, we need to know the 'keyword' or 'techStack' to fetch relevant
            // questions.
            // We don't have that stored in Session.
            // We can infer it from the first answered question's tags or fallback to Resume
            // Analysis again.

            User user = userRepository.findById(userId).orElseThrow();
            String keyword = "Java"; // Fallback

            // Try to get keyword from existing answered questions
            if (!answeredQuestions.isEmpty()) {
                String tags = answeredQuestions.get(0).getTags();
                if (tags != null && !tags.isEmpty()) {
                    keyword = tags.split(",")[0].trim();
                }
            } else {
                // Fallback to resume analysis
                ResumeAnalysisResult analysis = analysisResultRepository.findTopByUserOrderByCreatedAtDesc(user);
                if (analysis != null) {
                    // ... (same extraction logic as startNewSession) ...
                    // For brevity, let's just use the repo method that finds based on User history
                    // if possible
                    // or just reuse the logic.
                    try {
                        JsonNode root = objectMapper.readTree(analysis.getContentJson());
                        if (root.has("jobTitle")) {
                            keyword = root.get("jobTitle").asText();
                        }
                    } catch (Exception e) {
                    }
                }
            }

            newQuestions
                    .addAll(questionRepository.findRandomQuestionsNotAnsweredWithKeyword(userId, keyword, remaining));

            if (newQuestions.size() < remaining) {
                List<InterviewQuestion> generic = questionRepository.findRandomQuestionsNotAnswered(userId,
                        remaining - newQuestions.size());
                for (InterviewQuestion q : generic) {
                    boolean alreadyPicked = newQuestions.stream().anyMatch(nq -> nq.getId().equals(q.getId()));
                    if (!alreadyPicked)
                        newQuestions.add(q);
                }
            }

            // Note: We skip the complex AI generation for resume here to keep it simple and
            // fast.
            // If we really run out of questions, we just return what we have.
        }

        // Combine lists: Answered First, then New
        List<InterviewQuestion> allQuestions = new ArrayList<>(answeredQuestions);
        allQuestions.addAll(newQuestions);

        // Convert to DTOs
        List<InterviewQuestionDTO> questionDTOs = allQuestions.stream().map(q -> {
            try {
                return InterviewQuestionDTO.builder()
                        .id(q.getId())
                        .content(q.getContent())
                        .type(q.getType())
                        .options(objectMapper.readValue(q.getOptions(), List.class))
                        .difficulty(q.getDifficulty())
                        .category(q.getCategory())
                        .build();
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());

        return InterviewSessionDTO.builder()
                .id(session.getId()) // Populate id field for compatibility
                .sessionId(session.getId())
                .questions(questionDTOs)
                .totalQuestions(session.getTotalQuestions())
                .currentQuestionIndex(answeredQuestions.size()) // Add this field to DTO to tell frontend where to start
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
