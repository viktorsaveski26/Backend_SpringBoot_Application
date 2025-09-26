package com.telusko.quizapp.web;

import com.telusko.quizapp.model.DTO.QuizRequest;
import com.telusko.quizapp.model.Quiz;
import com.telusko.quizapp.model.Response;
import com.telusko.quizapp.service.QuizService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("quiz")
public class QuizController {

    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    // Create a new quiz and save it in the database
    @PostMapping("/create")
    public ResponseEntity<Quiz> createQuiz(@RequestBody QuizRequest quizRequest) {
        return quizService.createQuiz(
                quizRequest.getCategory(),
                quizRequest.getNumQ(),
                quizRequest.getTitle(),
                quizRequest.getQuizDifficultyLevel()
        );
    }

    // Get quiz by ID
    @GetMapping("/get/{id}")
    public ResponseEntity<Quiz> getQuiz(@PathVariable Integer id) {
        return quizService.getQuizById(id);
    }

    // Get all quizzes
    @GetMapping("/all")
    public ResponseEntity<List<Quiz>> getAllQuizzes() {
        return quizService.getAllQuizzes();
    }

    // Delete quiz by ID
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteQuiz(@PathVariable Integer id) {
        return quizService.deleteQuiz(id);
    }

    // Submit quiz responses and calculate the number of correct answers
    @PostMapping("/submit/{id}")
    public ResponseEntity<Integer> submitQuiz(@PathVariable Integer id, @RequestBody List<Response> responses) {
        return quizService.calculateResult(id, responses);
    }
}