package com.telusko.quizapp.repository;

import com.telusko.quizapp.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionDao extends JpaRepository<Question,Integer> {
    List<Question> findByCategory(String category);
    //sega e potrebno da zememe so querry toa sto ni treba od bazata
    @Query(value = "SELECT * FROM question q Where q.category=:category ORDER BY RANDOM() LIMIT :numQ",nativeQuery = true) // native querry koristime
    List<Question> findRandomQuestionsByCategory(String category, int numQ);
}
