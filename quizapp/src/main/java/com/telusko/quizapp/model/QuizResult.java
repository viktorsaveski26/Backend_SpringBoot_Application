package com.telusko.quizapp.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class QuizResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    private Integer correctAnswers;
    private Integer totalQuestions;
}
