package com.telusko.quizapp.AI.service;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AIService {

    private static final String API_URL = "https://openrouter.ai/api/v1/chat/completions";
    private static final String API_KEY = "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";

    // Enhanced SQL context with better instructions
    private static final String SQL_CONTEXT = """
    You are an expert SQL generator for a quiz application database.
    Database: PostgreSQL
    
    SCHEMA:
    1. users table:
       - id (integer, primary key)
       - email (varchar)
       - name (varchar) 
       - surname (varchar)
       - password (varchar)
       - role (varchar)
    
    2. quiz table:
       - id (integer, primary key)
       - title (varchar)
       - quiz_difficulty_level (varchar)
    
    3. quiz_result table:
       - id (integer, primary key)
       - correct_answers (integer)
       - total_questions (integer)
       - quiz_id (integer, foreign key to quiz.id)
       - user_id (integer, foreign key to users.id)
    
    STRICT RULES:
    - Generate ONLY SELECT queries
    - Never use INSERT, UPDATE, DELETE, DROP, ALTER, CREATE, or any modification commands
    - Use proper JOINs when querying multiple tables
    - Return ONLY the SQL query - no explanations, no code blocks, no formatting
    - Use proper column names and table names as specified above
    - For user searches, match against name, surname, or email fields
    - Always use ILIKE for case-insensitive text matching in PostgreSQL
    
    EXAMPLES:
    - "Show all quiz results for John" -> SELECT qr.*, u.name, u.surname, q.title FROM quiz_result qr JOIN users u ON qr.user_id = u.id JOIN quiz q ON qr.quiz_id = q.id WHERE u.name ILIKE '%John%'
    - "All users" -> SELECT * FROM users
    - "Quiz results with user names" -> SELECT qr.*, u.name, u.surname, q.title FROM quiz_result qr JOIN users u ON qr.user_id = u.id JOIN quiz q ON qr.quiz_id = q.id
    """;

    public String generateSQL(String userQuestion) {
        String finalPrompt = SQL_CONTEXT + "\n\nUser question: " + userQuestion + "\n\nGenerate SQL query:";
        String aiResponse = sendMessageToAI(finalPrompt);
        return cleanSQLResponse(aiResponse);
    }

    private String cleanSQLResponse(String aiResponse) {
        if (aiResponse == null || aiResponse.trim().isEmpty()) {
            return "SELECT 1 WHERE FALSE"; // Safe fallback that returns no results
        }

        // Remove common AI response formatting
        String cleaned = aiResponse.trim();

        // Remove code block markers
        cleaned = cleaned.replaceAll("```sql", "");
        cleaned = cleaned.replaceAll("```", "");

        // Remove common prefixes
        cleaned = cleaned.replaceAll("(?i)^(SQL:|Query:|Here's the SQL:|The SQL query is:)\\s*", "");

        // Extract SQL from the response using regex
        Pattern sqlPattern = Pattern.compile("(SELECT\\s+.*?)(?:\\s*;\\s*)?$", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher matcher = sqlPattern.matcher(cleaned);

        if (matcher.find()) {
            cleaned = matcher.group(1).trim();
        }

        // Remove trailing semicolon and whitespace
        cleaned = cleaned.replaceAll(";\\s*$", "").trim();

        // Validate it starts with SELECT (case insensitive)
        if (!cleaned.toLowerCase().startsWith("select")) {
            System.err.println("AI returned non-SELECT query: " + cleaned);
            return "SELECT 1 WHERE FALSE"; // Safe fallback
        }

        return cleaned;
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
        body.put("temperature", 0.1); // Lower temperature for more consistent SQL generation
        body.put("max_tokens", 500);

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
            return "Error: Invalid AI response format";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }
}