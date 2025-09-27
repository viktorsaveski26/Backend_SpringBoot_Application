package com.telusko.quizapp.AI.service;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class AIService {

    private static final String API_URL = "https://openrouter.ai/api/v1/chat/completions";
    private static final String API_KEY = "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"; // <-- replace

    // Static schema + rules
    private static final String SQL_CONTEXT = """
    You are an expert SQL generator.
    The database is PostgreSQL.

    Allowed tables and columns:
    1. users:
       - id
       - email
       - name
       - surname
       - password
       - role
    2. quiz_result:
       - id
       - correct_answers
       - total_questions
       - quiz_id
       - user_id
    3. quiz:
       - id
       - title
       - quiz_difficulty_level

    Rules:
    - ONLY generate safe SELECT queries.
    - Never use INSERT, UPDATE, DELETE, DROP, ALTER, or any DDL/DML.
    - Use INNER JOIN when necessary.
    - Always output ONLY the SQL query with no explanation or formatting.
    """;

    public String generateSQL(String userQuestion) {
        String finalPrompt = SQL_CONTEXT + "\n\nUser question: " + userQuestion + "\nSQL:";
        return sendMessageToAI(finalPrompt);
    }

    public String sendMessageToAI(String message) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + API_KEY);
        headers.set("HTTP-Referer", "http://localhost:3000");
        headers.set("X-Title", "Quiz App");

        Map<String, Object> body = new HashMap<>();
        body.put("model", "x-ai/grok-4-fast:free");
        body.put("messages", List.of(
                Map.of("role", "user", "content", message)
        ));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    API_URL, HttpMethod.POST, request, Map.class);

            Map<String, Object> responseBody = response.getBody();

            if (responseBody != null && responseBody.containsKey("choices")) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");

                if (!choices.isEmpty()) {
                    Map<String, Object> choice = choices.get(0);
                    Object messageObj = choice.get("message");

                    if (messageObj instanceof Map) {
                        Map<String, Object> messageMap = (Map<String, Object>) messageObj;
                        Object content = messageMap.get("content");
                        if (content instanceof String) {
                            return ((String) content).trim();
                        }
                    }
                }
            }
            return "Invalid or empty AI response.";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error while calling AI: " + e.getMessage();
        }
    }
}
