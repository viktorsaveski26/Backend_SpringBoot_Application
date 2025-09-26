package com.telusko.quizapp.model.Enum;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum QuizDifficultyLevel {
    EASY, MEDIUM, HARD;

    @JsonCreator
    public static QuizDifficultyLevel fromString(String value) {
        return QuizDifficultyLevel.valueOf(value.toUpperCase());
    }
}
