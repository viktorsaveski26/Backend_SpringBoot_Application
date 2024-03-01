package com.telusko.quizapp.service;

import com.telusko.quizapp.model.Question;
import com.telusko.quizapp.repository.QuestionDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class QuestionService {
    @Autowired
    QuestionDao questionDao;
    public ResponseEntity<List<Question>> getAllQuestions() {
       try{
           return new ResponseEntity<>(questionDao.findAll(), HttpStatus.OK);
       }
       catch (Exception e){
           e.printStackTrace(); // obicna funkcija sto pecati inforamcii za greskata
       }
       return new ResponseEntity<>(new ArrayList<>(),HttpStatus.BAD_REQUEST);

    }

    public ResponseEntity<List<Question>> getQuestionsByCategory(String category) {

        try{
            return new ResponseEntity<>(questionDao.findByCategory(category),HttpStatus.OK);
        }
        catch (Exception e){
            e.printStackTrace(); // obicna funkcija sto pecati inforamcii za greskata
        }
        return new ResponseEntity<>(new ArrayList<>(),HttpStatus.BAD_REQUEST);

    }
    // nemam bash delete i update od crud operacii no zasega ova e dovolono i ako treba toa se 5min rabota
    //vo kontroler dokolku gi pravam mapinzite ke se Delete za prisenje i Put za editiranje

    public ResponseEntity<String>  addQuestion(Question question) {
         questionDao.save(question);
         return new ResponseEntity<>( "success",HttpStatus.CREATED);//mislam .created bidejki dodavame(kreirame)
        //biejki ne e samo 200ok, tuku e created, vo postman ke dobieme 201status i porakata ke bide success bidejki toa go obravme da go napiseme nie ako se prati kako sto treba
    }
}
