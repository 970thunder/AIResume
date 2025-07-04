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