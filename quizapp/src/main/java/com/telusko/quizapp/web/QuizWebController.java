//package com.telusko.quizapp.web;
//
//import com.telusko.quizapp.model.QuestionWrapper;
//import com.telusko.quizapp.model.Response;
//import com.telusko.quizapp.service.QuizService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//
//@Controller
//public class QuizWebController {
//
//    @Autowired
//    private QuizService quizService;
//
//    @GetMapping("/quiz/{id}")
//    public String showQuiz(@PathVariable Integer id, Model model) {
//        List<QuestionWrapper> questions = quizService.getQuizQuestions(id).getBody();
//        model.addAttribute("questions", questions);
//        return "quiz";
//    }
//
//    @PostMapping("/quiz-web/submit/{id}")
//    public String submitQuiz(@PathVariable Integer id, @RequestParam Map<String, String> responses, Model model) {
//        List<Response> responseList = new ArrayList<>();
//        responses.forEach((key, value) -> {
//            try {
//                int questionId = Integer.parseInt(key);
//                responseList.add(new Response(questionId, value));
//            } catch (NumberFormatException e) {
//                e.printStackTrace();
//            }
//        });
//        int result = quizService.calculateResult(id, responseList).getBody();
//        model.addAttribute("result", result);
//        return "result";
//    }
//}