package com.telusko.quizapp.service;

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

    public ResponseEntity<String> createQuiz(String category, int numQ, String title) {
        List<Question> questions = questionDao.findRandomQuestionsByCategory(category,numQ); //ne sakame find all za da ne ni se zemat site prasanja od taa baza
       //davame parametar od koja kategorija i kolku prasanja sakame
        Quiz quiz = new Quiz(); // we are creating a quiz
        quiz.setTitle(title);
        quiz.setQuestions(questions);
         quizDao.save(quiz);
         return new ResponseEntity<>("Success", HttpStatus.CREATED);
    }

    public ResponseEntity<List<QuestionWrapper>> getQuizQuestions(Integer id) {
       Optional<Quiz> quiz =   quizDao.findById(id); // optional because id might not exist
        List<Question> questionsFromDB = quiz.get().getQuestions(); // because of optional, first we take the object, and after that getQuestions()
        List<QuestionWrapper> questionsForUser = new ArrayList<>();
        //here we made an empty list, and we just need to add data
        for (Question q : questionsFromDB){
            QuestionWrapper qw = new QuestionWrapper(q.getId(),q.getQuestionTitle(),q.getOption1(),q.getOption2(), q.getOption3(),q.getOption4());
       //we had a constructor and we created an ojbect with all these values.
            questionsForUser.add(qw);
        }

        return new ResponseEntity<>(questionsForUser,HttpStatus.OK);
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
}
