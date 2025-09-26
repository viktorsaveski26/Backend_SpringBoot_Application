package com.telusko.quizapp.web.OpenAiQuizController;

import com.telusko.quizapp.model.QuestionWrapper;
import com.telusko.quizapp.service.OpenAi.OpenAiQuizService;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/openai-quiz")
public class OpenAiQuizController {

    @Autowired
    private OpenAiQuizService openAiService;

    @GetMapping("/generate/openai")
    public ResponseEntity<List<QuestionWrapper>> generateOpenAiQuiz(
            @RequestParam String category,
            @RequestParam int numQ) throws JSONException {

        List<QuestionWrapper> questions = openAiService.generateQuestions(category, numQ);
        return ResponseEntity.ok(questions);
    }
}
