package com.telusko.quizapp.model;

import com.telusko.quizapp.model.Enum.QuizDifficultyLevel;
import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Data
public class Quiz {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "quiz_difficulty_level")
    private QuizDifficultyLevel quizDifficultyLevel;

    @ManyToMany
    private List<Question> questions;

    @ManyToMany
    @JoinTable(
            name = "user_quiz",
            joinColumns = @JoinColumn(name = "quiz_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> users;
}