package com.telusko.quizapp.service;

import com.telusko.quizapp.model.QuizResult;
import com.telusko.quizapp.repository.QuizResultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class QuizResultService {

    @Autowired
    private QuizResultRepository quizResultRepository;

    public ResponseEntity<String> saveQuizResult(QuizResult quizResult) {
        try {
            quizResultRepository.save(quizResult);
            return new ResponseEntity<>("Quiz result saved successfully", HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Failed to save quiz result", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}