package com.telusko.quizapp.AI.controller;

import com.telusko.quizapp.AI.service.AIService;
import com.telusko.quizapp.AI.service.QueryExecutionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ai")
public class AIController {

    private final AIService aiService;
    private final QueryExecutionService queryExecutionService;

    public AIController(AIService aiService, QueryExecutionService queryExecutionService) {
        this.aiService = aiService;
        this.queryExecutionService = queryExecutionService;
    }

    @GetMapping("/message")
    public ResponseEntity<?> executeAIQuery(@RequestParam("question") String question) {
        if (question == null || question.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Question cannot be empty.");
        }

        // Generate SQL query or general response
        String aiResponse = aiService.generateSQL(question);

        // Check if the response is a valid SQL query
        if (aiResponse.toLowerCase().startsWith("select")) {
            try {
                // Execute the query and return results
                List<Map<String, Object>> results = queryExecutionService.executeQuery(aiResponse);
                return ResponseEntity.ok(results);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body("Invalid query: " + e.getMessage());
            } catch (Exception e) {
                return ResponseEntity.badRequest().body("Error executing query: " + e.getMessage());
            }
        }

        // If not a SQL query, return the general response
        return ResponseEntity.ok(Map.of("response", aiResponse));
    }
}