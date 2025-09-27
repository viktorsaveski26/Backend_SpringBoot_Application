package com.telusko.quizapp.AI.service;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class AIService {

    private static final String API_URL = "https://openrouter.ai/api/v1/chat/completions";
    private static final String API_KEY = "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";

    public String sendMessageToAI(String message) {
        RestTemplate restTemplate = new RestTemplate();

        // Set headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + API_KEY);
        headers.set("HTTP-Referer", "http://localhost:3000");
        headers.set("X-Title", "Quiz App");

        // Create request body
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
                            return (String) content;
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
