package com.telusko.quizapp.web;

import com.telusko.quizapp.model.Question;
import com.telusko.quizapp.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("question")
public class QuestionController {
    @Autowired
    QuestionService questionService;
    @GetMapping("allQuestions")
    public ResponseEntity<List<Question>> getAllQuestions(){ // ovde pravime vaka za pokraj Data, da pratime i status na baranjeto(200 Ok,404 i sl)
        return  questionService.getAllQuestions();
    }
    @GetMapping("category/{category}") //kakva vrednost ke imam za category toa ke se mapira
    public ResponseEntity<List<Question>> getQuestionsByCategory(@PathVariable String category){
        return questionService.getQuestionsByCategory(category);
    }
    @PostMapping("add") //ova e kako save zatoa e post, fakticki objektot se zema od http baranjeo i se pravi json od formatot na korisnikot za da se vnese vo baza
   //requstbody konvertira vo urlto celiot objekt vo json, ako oevme so requeest param ke trebese namesto so eden objekt koj vo spring avtomatski se konvertira, da odime so parametri od site atributi koi ke se prakaa vo url-to, pa od niv nie da napravime eden nash objekt
    //iako nemam kako ocekuvano nekoja si forma za get mapping za add, ova e direktno za post, a za toa dali i kako  raboti mozeme da koristime Postman
    public ResponseEntity<String> addQuestion(@RequestBody Question question){
        return questionService.addQuestion(question);
    }
}
