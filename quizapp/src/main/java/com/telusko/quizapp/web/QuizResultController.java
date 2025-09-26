package com.telusko.quizapp.web;

import com.telusko.quizapp.model.QuizResult;
import com.telusko.quizapp.service.QuizResultService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("quiz-result")
public class QuizResultController {

    private final QuizResultService quizResultService;

    public QuizResultController(QuizResultService quizResultService) {
        this.quizResultService = quizResultService;
    }

    @PostMapping("/save")
    public ResponseEntity<String> saveQuizResult(@RequestBody QuizResult quizResult) {
        return quizResultService.saveQuizResult(quizResult);
    }
}