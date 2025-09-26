package com.telusko.quizapp.model.DTO;

import com.telusko.quizapp.model.Enum.QuizDifficultyLevel;

public class QuizRequest {
    private String category;
    private int numQ;
    private String title;
    private QuizDifficultyLevel quizDifficultyLevel;

    // Getters and Setters
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getNumQ() { return numQ; }
    public void setNumQ(int numQ) { this.numQ = numQ; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public QuizDifficultyLevel getQuizDifficultyLevel() { return quizDifficultyLevel; }
    public void setQuizDifficultyLevel(QuizDifficultyLevel quizDifficultyLevel) { this.quizDifficultyLevel = quizDifficultyLevel; }
}