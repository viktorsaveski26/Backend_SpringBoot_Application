package com.telusko.quizapp.service;

import com.telusko.quizapp.model.*;
import com.telusko.quizapp.model.Enum.QuizDifficultyLevel;
import com.telusko.quizapp.repository.QuestionDao;
import com.telusko.quizapp.repository.QuizDao;
import com.telusko.quizapp.repository.QuizResultRepository;
import com.telusko.quizapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class QuizService {
    @Autowired
    QuizDao quizDao;
    @Autowired
    QuestionDao questionDao;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private QuizResultRepository quizResultRepository;

    public ResponseEntity<Quiz> createQuiz(String category, int numQ, String title, QuizDifficultyLevel quizDifficultyLevel) {
        List<Question> questions = questionDao.findRandomQuestionsByCategory(category, numQ);
        if (questions.size() < numQ) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Quiz quiz = new Quiz();
        quiz.setTitle(title);
        quiz.setQuizDifficultyLevel(quizDifficultyLevel);
        quiz.setQuestions(questions);
        quizDao.save(quiz);
        return new ResponseEntity<>(quiz, HttpStatus.CREATED);
    }

    public ResponseEntity<List<QuestionWrapper>> getQuizQuestions(Integer id) {
        Optional<Quiz> quiz = quizDao.findById(id);
        if (quiz.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        List<Question> questionsFromDB = quiz.get().getQuestions();
        List<QuestionWrapper> questionsForUser = new ArrayList<>();
        for (Question q : questionsFromDB) {
            questionsForUser.add(new QuestionWrapper(q.getId(), q.getQuestionTitle(), q.getOption1(), q.getOption2(), q.getOption3(), q.getOption4()));
        }
        return new ResponseEntity<>(questionsForUser, HttpStatus.OK);
    }

    public QuizResult calculateResult(Integer id, List<Response> responses) {
        Quiz quiz = quizDao.findById(id).orElseThrow(() -> new RuntimeException("Quiz not found"));
        List<Question> questions = quiz.getQuestions();
        int correctAnswers = 0;

        for (int i = 0; i < responses.size(); i++) {
            if (responses.get(i).getResponse().equals(questions.get(i).getRightAnswer())) {
                correctAnswers++;
            }
        }
        // Retrieve the logged-in user's email
        String loggedInUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        // Fetch the User entity from the database
        User user = userRepository.findByEmail(loggedInUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Create and populate the QuizResult object
        QuizResult quizResult = new QuizResult();
        quizResult.setQuiz(quiz);
        quizResult.setCorrectAnswers(correctAnswers);
        quizResult.setTotalQuestions(questions.size());
        quizResult.setUser(user); // Set the logged-in user

        quizResultRepository.save(quizResult);

        return quizResult;
    }
    public ResponseEntity<List<Quiz>> getAllQuizzes() {
        return new ResponseEntity<>(quizDao.findAll(), HttpStatus.OK);
    }

    public ResponseEntity<String> editQuiz(Integer id, Quiz updatedQuiz) {
        Optional<Quiz> quizOptional = quizDao.findById(id);
        if (quizOptional.isPresent()) {
            Quiz existingQuiz = quizOptional.get();
            existingQuiz.setTitle(updatedQuiz.getTitle());
            existingQuiz.setQuizDifficultyLevel(updatedQuiz.getQuizDifficultyLevel());
            existingQuiz.setQuestions(updatedQuiz.getQuestions());
            quizDao.save(existingQuiz);
            return new ResponseEntity<>("Quiz updated successfully", HttpStatus.OK);
        }
        return new ResponseEntity<>("Quiz not found", HttpStatus.NOT_FOUND);
    }

    public ResponseEntity<String> deleteQuiz(Integer id) {
        if (quizDao.existsById(id)) {
            quizDao.deleteById(id);
            return new ResponseEntity<>("Quiz deleted successfully", HttpStatus.OK);
        }
        return new ResponseEntity<>("Quiz not found", HttpStatus.NOT_FOUND);
    }

    public ResponseEntity<Quiz> getQuizById(Integer id) {
        Optional<Quiz> quizOptional = quizDao.findById(id);
        return quizOptional.map(quiz -> {
            quiz.getQuestions().size();
            return new ResponseEntity<>(quiz, HttpStatus.OK);
        }).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}