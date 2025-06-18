package com.example.preguntonquiz.model;

import java.util.List;
import java.util.Map;

public class User {
    private String uid;
    private String nickname;
    private int score;
    private Map<String, Boolean> answeredQuestions;

    public User() {
        // Constructor vacío requerido por Firebase
    }

    public User(String uid, String nickname, int score, Map<String, Boolean> answeredQuestions) {
        this.uid = uid;
        this.nickname = nickname;
        this.score = score;
        this.answeredQuestions = answeredQuestions;
    }

    // Getters y setters...
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public Map<String, Boolean> getAnsweredQuestions() { return answeredQuestions; }
    public void setAnsweredQuestions(Map<String, Boolean> answeredQuestions) { this.answeredQuestions = answeredQuestions; }
}
