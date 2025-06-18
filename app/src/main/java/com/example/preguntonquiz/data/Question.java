package com.example.preguntonquiz.data;

import java.util.List;

public class Question {
    private String question;
    private String correctAnswer;
    private List<String> incorrectAnswers;

    public Question(String question, String correctAnswer, List<String> incorrectAnswers) {
        this.question = question;
        this.correctAnswer = correctAnswer;
        this.incorrectAnswers = incorrectAnswers;
    }

    // Getters
    public String getQuestion() { return question; }
    public String getCorrectAnswer() { return correctAnswer; }
    public List<String> getIncorrectAnswers() { return incorrectAnswers; }

    // Setters
    public void setQuestion(String question) { this.question = question; }
    public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }
    public void setIncorrectAnswers(List<String> incorrectAnswers) { this.incorrectAnswers = incorrectAnswers; }
}
