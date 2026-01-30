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
    // Check for empty answer and skip AI call
    if (answer == null || answer.trim().isEmpty()) {
      Map<String, Object> result = new HashMap<>();
      result.put("isCorrect", false);
      result.put("evaluation", "用户未作答");
      result.put("score", 0);
      return result;
    }

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
    return buildBatchInterviewQuestionPrompt(10, techStack);
  }

  public List<Map<String, Object>> generateBatchInterviewQuestions(int count, String category) {
    try {
      Map<String, Object> requestBody = new HashMap<>();
      requestBody.put("model", deepSeekConfig.getModel());

      List<Map<String, String>> messages = new ArrayList<>();
      Map<String, String> message = new HashMap<>();
      message.put("role", "user");
      message.put("content", buildBatchInterviewQuestionPrompt(count, category));
      messages.add(message);

      requestBody.put("messages", messages);
      requestBody.put("temperature", 0.7);
      requestBody.put("max_tokens", 3500); // Increased for batch

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
      throw new RuntimeException("Batch generation failed: " + e.getMessage(), e);
    }
  }

  private String buildBatchInterviewQuestionPrompt(int count, String techStack) {
    return "请基于以下技术栈或分类生成 " + count + " 道技术面试题。\n" +
        "分类/技术栈: " + techStack + "\n\n" +
        "请包含不同难度 (EASY, MEDIUM, HARD) 和类型 (OPEN_ENDED, MULTIPLE_CHOICE) 的题目。\n" +
        "对于 MULTIPLE_CHOICE (选择题)，请提供 4 个选项和正确答案。\n" +
        "对于 OPEN_ENDED (问答题)，请提供详尽的参考答案。\n" +
        "**所有问题、选项和答案必须使用中文。**\n\n" +
        "请严格返回以下结构的 JSON 数组：\n" +
        "[\n" +
        "    {\n" +
        "        \"content\": \"此处填写题目内容\",\n" +
        "        \"type\": \"OPEN_ENDED\", // 或 \"MULTIPLE_CHOICE\"\n" +
        "        \"options\": [\"选项 A\", \"选项 B\", \"选项 C\", \"选项 D\"], // 如果是 OPEN_ENDED 则为 null\n" +
        "        \"referenceAnswer\": \"详细的参考答案或正确选项 (例如：选项 A)\",\n" +
        "        \"difficulty\": \"MEDIUM\",\n" +
        "        \"category\": \"" + techStack + "\", // 请尽可能准确\n" +
        "        \"tags\": \"" + techStack + "\" // 逗号分隔的标签\n" +
        "    }\n" +
        "]";
  }

  private String buildEvaluationPrompt(String question, String answer) {
    return """
        请评估以下面试回答。
        题目: """ + question + """
        用户回答: """ + answer + """

        **请使用中文进行评估。**

        请严格返回一个 JSON 对象：
        {
            "isCorrect": true, // 或 false。对于问答题请适当放宽标准，主要检查逻辑；对于选择题请严格判定。
            "evaluation": "详细的反馈，指出回答中的正确之处、错误之处以及改进建议。",
            "score": 85 // 0-100 分
        }
        """;
  }

  private String buildCareerAnalysisPrompt(String extractedContent, String jobDescription) {
    String jdSection = "";
    if (jobDescription != null && !jobDescription.trim().isEmpty()) {
      jdSection = """

          目标职位描述 (JD):
          """ + jobDescription + """

          请将简历与上述职位描述进行对比分析。
          """;
    }

    return """
        请分析以下简历内容，并返回一个结构化的职业能力分析 JSON 对象。
        JSON 对象必须严格遵循以下格式。
        **所有分析内容（优势、劣势、建议、总结等）必须使用中文。**
        """
        + jdSection
        + """

            简历内容:
            """
        + extractedContent
        + """

            请严格按照以下 JSON 格式返回分析结果。不要在 JSON 对象前后包含任何文本。
            {
              "score": 85, // 总分 0-100，基于整体质量或 JD 匹配度（如果提供了 JD）
              "radar": [ // 雷达图的 6 个维度
                { "name": "专业技能", "value": 80, "max": 100 },
                { "name": "沟通能力", "value": 90, "max": 100 },
                { "name": "领导力", "value": 70, "max": 100 },
                { "name": "工作经验", "value": 85, "max": 100 },
                { "name": "教育背景", "value": 80, "max": 100 },
                { "name": "发展潜力", "value": 75, "max": 100 }
              ],
              "strengths": ["优势 1", "优势 2", "优势 3"], // 至少 3 点
              "weaknesses": ["劣势 1", "劣势 2", "劣势 3"], // 至少 3 点
              "advice": ["建议 1", "建议 2", "建议 3"], // 发展建议，至少 3 点
              "summary": "候选人概况及就业市场竞争力的整体总结。",
              "jdAnalysis": { // 仅在提供了 JD 时返回，否则为 null 或空对象
                "matchScore": 75, // 匹配得分 0-100
                "matchingKeywords": ["Java", "Spring Boot"], // 简历和 JD 中都出现的关键词
                "missingKeywords": ["AWS", "Docker"], // JD 中有但简历中缺失的关键词
                "gapAnalysis": "简历与 JD 之间的差距分析"
              }
            }
            """;
  }

  private String buildAnalysisPrompt(String extractedContent) {
    return """
        请分析以下简历内容，并返回一个全面、结构化的 JSON 对象。
        JSON 对象应符合下文提供的 Schema。尽可能提取相关信息。
        如果内容中不存在特定信息，请忽略该键或将其值设置为 null。
        **所有提取的内容（如总结、职责描述等）必须使用中文（姓名、公司名、技术名词等专有名词除外）。**

        简历内容:
        """
        + extractedContent
        + """

            请严格按照以下 JSON 格式返回分析结果。不要在 JSON 对象前后包含任何文本。
            {
              "personalInfo": {
                "fullName": "姓名",
                "jobTitle": "目标职位或当前职位",
                "email": "email@example.com",
                "phone": "电话号码",
                "address": {
                  "city": "城市",
                  "state": "省/州",
                  "country": "国家"
                },
                "links": {
                  "linkedInUrl": "LinkedIn 个人资料 URL",
                  "githubUrl": "GitHub 个人资料 URL",
                  "portfolioUrl": "个人网站/作品集 URL"
                }
              },
              "summary": "2-4 句的专业总结。",
              "experience": [
                {
                  "companyName": "公司名称",
                  "jobTitle": "职位名称",
                  "location": "城市, 省份",
                  "startDate": "YYYY-MM",
                  "endDate": "YYYY-MM 或 Present",
                  "responsibilities": [
                    "成就或职责 1。",
                    "成就或职责 2。"
                  ]
                }
              ],
              "education": [
                {
                  "institutionName": "大学/学院名称",
                  "location": "城市, 省份",
                  "degree": "学位 (例如：理学学士)",
                  "major": "专业 (例如：计算机科学)",
                  "gpa": "平均绩点 (如果可用)",
                  "graduationDate": "YYYY-MM",
                  "relevantCoursework": ["课程 1", "课程 2"],
                  "honors": "学术荣誉 (例如：院长名单)"
                }
              ],
              "skills": {
                "technicalSkills": ["技能 A", "技能 B"],
                "softSkills": ["技能 C", "技能 D"],
                "tools": ["工具 E", "工具 F"]
              },
              "projects": [
                {
                  "projectName": "项目名称",
                  "description": "项目简介。",
                  "technologiesUsed": ["技术 1", "技术 2"],
                  "projectUrl": "在线项目 URL",
                  "repositoryUrl": "源代码 URL"
                }
              ],
              "certifications": [
                {
                  "name": "证书名称",
                  "issuingOrganization": "发证机构",
                  "issueDate": "YYYY-MM",
                  "credentialId": "ID 或代码 (如果可用)"
                }
              ],
              "languages": [
                {
                  "language": "语言 (例如：英语)",
                  "proficiency": "熟练程度 (例如：母语, 流利, 会话)"
                }
              ]
            }
            """;
  }
}