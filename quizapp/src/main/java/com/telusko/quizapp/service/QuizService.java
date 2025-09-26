package com.telusko.quizapp.service;

import com.telusko.quizapp.model.Enum.QuizDifficultyLevel;
import com.telusko.quizapp.model.Question;
import com.telusko.quizapp.model.QuestionWrapper;
import com.telusko.quizapp.model.Quiz;
import com.telusko.quizapp.model.Response;
import com.telusko.quizapp.repository.QuestionDao;
import com.telusko.quizapp.repository.QuizDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    public ResponseEntity<Integer> calculateResult(Integer id, List<Response> responses) {
        Quiz quiz = quizDao.findById(id).get(); // get if you do not want to make this optional or throw an exception
        List<Question> questions = quiz.getQuestions();
        int right = 0;
        int i = 0;
        for (Response response :  responses){
            if(response.getResponse().equals(questions.get(i).getRightAnswer())){
                right++;
            }
            i++;
        }
        return new ResponseEntity<>(right,HttpStatus.OK);
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