package com.resume.generator.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resume.generator.config.DeepSeekConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AIAnalysisService {

  @Autowired
  private DeepSeekConfig deepSeekConfig;

  @Autowired
  private RestTemplate restTemplate;

  private final ObjectMapper objectMapper = new ObjectMapper();

  public Map<String, Object> analyzeProfile(String extractedContent) {
    try {
      // Build DeepSeek API request
      Map<String, Object> requestBody = new HashMap<>();
      requestBody.put("model", deepSeekConfig.getModel());

      List<Map<String, String>> messages = new ArrayList<>();
      Map<String, String> message = new HashMap<>();
      message.put("role", "user");
      message.put("content", buildAnalysisPrompt(extractedContent));
      messages.add(message);

      requestBody.put("messages", messages);
      requestBody.put("temperature", 0.7);
      requestBody.put("max_tokens", 2000);

      // Set request headers
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      headers.setBearerAuth(deepSeekConfig.getApiKey());

      HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

      // Call DeepSeek API
      ResponseEntity<Map> response = restTemplate.postForEntity(
          deepSeekConfig.getBaseUrl() + "/chat/completions",
          entity,
          Map.class);

      if (response.getStatusCode() == HttpStatus.OK) {
        Map<String, Object> responseBody = response.getBody();
        List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
        if (!choices.isEmpty()) {
          Map<String, Object> firstChoice = choices.get(0);
          Map<String, String> messageContent = (Map<String, String>) firstChoice.get("message");
          String rawJson = messageContent.get("content");

          // Clean and parse the JSON response from the AI
          String cleanedJson = rawJson.replaceAll("```json", "").replaceAll("```", "").trim();
          try {
            return objectMapper.readValue(cleanedJson, new TypeReference<>() {
            });
          } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse AI response as JSON", e);
          }
        }
      }

      throw new RuntimeException("DeepSeek API call failed with status: " + response.getStatusCode());

    } catch (Exception e) {
      throw new RuntimeException("AI analysis failed: " + e.getMessage(), e);
    }
  }

  public Map<String, Object> analyzeCareer(String extractedContent, String jobDescription) {
    try {
      Map<String, Object> requestBody = new HashMap<>();
      requestBody.put("model", deepSeekConfig.getModel());

      List<Map<String, String>> messages = new ArrayList<>();
      Map<String, String> message = new HashMap<>();
      message.put("role", "user");
      message.put("content", buildCareerAnalysisPrompt(extractedContent, jobDescription));
      messages.add(message);

      requestBody.put("messages", messages);
      requestBody.put("temperature", 0.7);
      requestBody.put("max_tokens", 2500); // Increased tokens for more detailed analysis

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      headers.setBearerAuth(deepSeekConfig.getApiKey());

      HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

      ResponseEntity<Map> response = restTemplate.postForEntity(
          deepSeekConfig.getBaseUrl() + "/chat/completions",
          entity,
          Map.class);

      if (response.getStatusCode() == HttpStatus.OK) {
        Map<String, Object> responseBody = response.getBody();
        List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
        if (!choices.isEmpty()) {
          Map<String, Object> firstChoice = choices.get(0);
          Map<String, String> messageContent = (Map<String, String>) firstChoice.get("message");
          String rawJson = messageContent.get("content");

          String cleanedJson = rawJson.replaceAll("```json", "").replaceAll("```", "").trim();
          try {
            return objectMapper.readValue(cleanedJson, new TypeReference<>() {
            });
          } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse AI response as JSON", e);
          }
        }
      }
      throw new RuntimeException("DeepSeek API call failed with status: " + response.getStatusCode());
    } catch (Exception e) {
      throw new RuntimeException("Career analysis failed: " + e.getMessage(), e);
    }
  }

  public List<Map<String, Object>> generateInterviewQuestions(String techStack) {
    try {
      Map<String, Object> requestBody = new HashMap<>();
      requestBody.put("model", deepSeekConfig.getModel());

      List<Map<String, String>> messages = new ArrayList<>();
      Map<String, String> message = new HashMap<>();
      message.put("role", "user");
      message.put("content", buildInterviewQuestionPrompt(techStack));
      messages.add(message);

      requestBody.put("messages", messages);
      requestBody.put("temperature", 0.7);
      requestBody.put("max_tokens", 3000);

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      headers.setBearerAuth(deepSeekConfig.getApiKey());

      HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

      ResponseEntity<Map> response = restTemplate.postForEntity(
          deepSeekConfig.getBaseUrl() + "/chat/completions",
          entity,
          Map.class);

      if (response.getStatusCode() == HttpStatus.OK) {
        Map<String, Object> responseBody = response.getBody();
        List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
        if (!choices.isEmpty()) {
          Map<String, Object> firstChoice = choices.get(0);
          Map<String, String> messageContent = (Map<String, String>) firstChoice.get("message");
          String rawJson = messageContent.get("content");

          String cleanedJson = rawJson.replaceAll("```json", "").replaceAll("```", "").trim();
          try {
            return objectMapper.readValue(cleanedJson, new TypeReference<>() {
            });
          } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse AI response as JSON", e);
          }
        }
      }
      throw new RuntimeException("DeepSeek API call failed with status: " + response.getStatusCode());
    } catch (Exception e) {
      throw new RuntimeException("Interview generation failed: " + e.getMessage(), e);
    }
  }

  public Map<String, Object> evaluateAnswer(String question, String answer) {
    try {
      Map<String, Object> requestBody = new HashMap<>();
      requestBody.put("model", deepSeekConfig.getModel());

      List<Map<String, String>> messages = new ArrayList<>();
      Map<String, String> message = new HashMap<>();
      message.put("role", "user");
      message.put("content", buildEvaluationPrompt(question, answer));
      messages.add(message);

      requestBody.put("messages", messages);
      requestBody.put("temperature", 0.3);
      requestBody.put("max_tokens", 1000);

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      headers.setBearerAuth(deepSeekConfig.getApiKey());

      HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

      ResponseEntity<Map> response = restTemplate.postForEntity(
          deepSeekConfig.getBaseUrl() + "/chat/completions",
          entity,
          Map.class);

      if (response.getStatusCode() == HttpStatus.OK) {
        Map<String, Object> responseBody = response.getBody();
        List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
        if (!choices.isEmpty()) {
          Map<String, Object> firstChoice = choices.get(0);
          Map<String, String> messageContent = (Map<String, String>) firstChoice.get("message");
          String rawJson = messageContent.get("content");
          String cleanedJson = rawJson.replaceAll("```json", "").replaceAll("```", "").trim();
          try {
            return objectMapper.readValue(cleanedJson, new TypeReference<>() {
            });
          } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse AI response as JSON", e);
          }
        }
      }
      throw new RuntimeException("DeepSeek API call failed");
    } catch (Exception e) {
      throw new RuntimeException("Answer evaluation failed: " + e.getMessage(), e);
    }
  }

  private String buildInterviewQuestionPrompt(String techStack) {
    return """
        Generate 10 technical interview questions based on the following technology stack or keywords.
        Tech Stack: """ + techStack + """

        Include a mix of Difficulty (EASY, MEDIUM, HARD) and Types (OPEN_ENDED, MULTIPLE_CHOICE).
        For MULTIPLE_CHOICE, provide 4 options and the correct answer.
        For OPEN_ENDED, provide a comprehensive reference answer.

        Return STRICTLY a JSON array of objects with the following structure:
        [
            {
                "content": "Question text here",
                "type": "OPEN_ENDED", // or "MULTIPLE_CHOICE"
                "options": ["Option A", "Option B", "Option C", "Option D"], // null if OPEN_ENDED
                "referenceAnswer": "Detailed reference answer or correct option (e.g. Option A)",
                "difficulty": "MEDIUM",
                "category": "Java", // The specific tech category e.g. Java, Redis, React
                "tags": "Java,Backend" // Comma separated tags
            }
        ]
        """;
  }

  private String buildEvaluationPrompt(String question, String answer) {
    return """
        Evaluate the following interview answer.
        Question: """ + question + """
        User Answer: """ + answer + """

        Return STRICTLY a JSON object:
        {
            "isCorrect": true, // or false. Be somewhat lenient for open ended, strictly check logic.
            "evaluation": "Detailed feedback on what was right, what was wrong, and how to improve.",
            "score": 85 // 0-100 score
        }
        """;
  }

  private String buildCareerAnalysisPrompt(String extractedContent, String jobDescription) {
    String jdSection = "";
    if (jobDescription != null && !jobDescription.trim().isEmpty()) {
      jdSection = """

          Target Job Description (JD):
          """ + jobDescription + """

          Please also compare the resume against the above Job Description.
          """;
    }

    return """
        Please analyze the following resume content and return a structured JSON object for career capability analysis.
        The JSON object must strictly follow the format below.
        """
        + jdSection
        + """

            Resume Content:
            """
        + extractedContent
        + """

            Please return the analysis result strictly in the following JSON format. Do not include any text before or after the JSON object.
            {
              "score": 85, // Total score 0-100 based on general quality or JD match if provided
              "radar": [ // 6 dimensions for radar chart
                { "name": "Professional Skills", "value": 80, "max": 100 },
                { "name": "Communication", "value": 90, "max": 100 },
                { "name": "Leadership", "value": 70, "max": 100 },
                { "name": "Experience", "value": 85, "max": 100 },
                { "name": "Education", "value": 80, "max": 100 },
                { "name": "Potential", "value": 75, "max": 100 }
              ],
              "strengths": ["strength 1", "strength 2", "strength 3"], // At least 3 points
              "weaknesses": ["weakness 1", "weakness 2", "weakness 3"], // At least 3 points
              "advice": ["advice 1", "advice 2", "advice 3"], // Growth suggestions, at least 3 points
              "summary": "Overall summary of the candidate's profile and job market competitiveness.",
              "jdAnalysis": { // Only if JD is provided, otherwise null or empty object
                "matchScore": 75, // Match score 0-100
                "matchingKeywords": ["Java", "Spring Boot"], // Keywords present in both Resume and JD
                "missingKeywords": ["AWS", "Docker"], // Keywords in JD but missing in Resume
                "gapAnalysis": "Analysis of the gap between resume and JD"
              }
            }
            """;
  }

  private String buildAnalysisPrompt(String extractedContent) {
    return """
        Please analyze the following resume content and return a comprehensive, structured JSON object.
        The JSON object should conform to the schema provided below. Extract as much relevant information as possible.
        If a particular piece of information is not available in the content, omit the key or set its value to null.

        Profile Content:
        """
        + extractedContent
        + """

            Please return the analysis result strictly in the following JSON format. Do not include any text before or after the JSON object.
            {
              "personalInfo": {
                "fullName": "Full Name",
                "jobTitle": "Target Job Title or Current Role",
                "email": "email@example.com",
                "phone": "Phone Number",
                "address": {
                  "city": "City",
                  "state": "State/Province",
                  "country": "Country"
                },
                "links": {
                  "linkedInUrl": "LinkedIn Profile URL",
                  "githubUrl": "GitHub Profile URL",
                  "portfolioUrl": "Personal Website/Portfolio URL"
                }
              },
              "summary": "A 2-4 sentence professional summary.",
              "experience": [
                {
                  "companyName": "Company Name",
                  "jobTitle": "Position Held",
                  "location": "City, State",
                  "startDate": "YYYY-MM",
                  "endDate": "YYYY-MM or Present",
                  "responsibilities": [
                    "Achievement or responsibility 1.",
                    "Achievement or responsibility 2."
                  ]
                }
              ],
              "education": [
                {
                  "institutionName": "University/College Name",
                  "location": "City, State",
                  "degree": "Degree (e.g., Bachelor of Science)",
                  "major": "Field of Study (e.g., Computer Science)",
                  "gpa": "Grade Point Average (if available)",
                  "graduationDate": "YYYY-MM",
                  "relevantCoursework": ["Course 1", "Course 2"],
                  "honors": "Academic Honors (e.g., Dean's List)"
                }
              ],
              "skills": {
                "technicalSkills": ["Skill A", "Skill B"],
                "softSkills": ["Skill C", "Skill D"],
                "tools": ["Tool E", "Tool F"]
              },
              "projects": [
                {
                  "projectName": "Project Name",
                  "description": "A brief description of the project.",
                  "technologiesUsed": ["Tech 1", "Tech 2"],
                  "projectUrl": "Live project URL",
                  "repositoryUrl": "Source code URL"
                }
              ],
              "certifications": [
                {
                  "name": "Certification Name",
                  "issuingOrganization": "Issuing Body",
                  "issueDate": "YYYY-MM",
                  "credentialId": "ID or Code (if available)"
                }
              ],
              "languages": [
                {
                  "language": "Language (e.g., English)",
                  "proficiency": "Proficiency Level (e.g., Native, Fluent, Conversational)"
                }
              ]
            }
            """;
  }
}