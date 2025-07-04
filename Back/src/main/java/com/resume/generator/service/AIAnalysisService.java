package com.resume.generator.service;

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

    public String analyzeProfile(String extractedContent) {
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
                    return messageContent.get("content");
                }
            }

            throw new RuntimeException("DeepSeek API call failed with status: " + response.getStatusCode());

        } catch (Exception e) {
            throw new RuntimeException("AI analysis failed: " + e.getMessage(), e);
        }
    }

    private String buildAnalysisPrompt(String extractedContent) {
        return """
                Please analyze the following profile content and return the structured resume information in JSON format:

                Profile Content:
                """
                + extractedContent + """

                        Please return the analysis result in the following JSON format:
                        {
                            "personalInfo": {
                                "name": "Name",
                                "email": "Email",
                                "phone": "Phone",
                                "address": "Address"
                            },
                            "summary": "Personal summary (a few sentences)",
                            "experience": [
                                {
                                    "company": "Company Name",
                                    "position": "Position",
                                    "duration": "Work Duration",
                                    "description": "Job Description"
                                }
                            ],
                            "education": [
                                {
                                    "school": "School Name",
                                    "degree": "Degree",
                                    "duration": "Study Duration"
                                }
                            ],
                            "skills": ["Skill 1", "Skill 2", "Skill 3"]
                        }

                        Please only return the content in JSON format, without any other text.
                        """;
    }
}