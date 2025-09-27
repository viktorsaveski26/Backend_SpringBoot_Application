package com.telusko.quizapp.AI.controller;

import com.telusko.quizapp.AI.service.AIService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai")
public class AIController {

    private final AIService aiService;

    public AIController(AIService aiService) {
        this.aiService = aiService;
    }

    @GetMapping("/message")
    public ResponseEntity<String> getAIMessage(@RequestParam("question") String question) {

        if (question == null || question.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Question cannot be empty.");
        }

        String response = aiService.sendMessageToAI(question);
        return ResponseEntity.ok(response);
    }
}