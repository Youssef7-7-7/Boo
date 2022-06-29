package com.jotech.boo.models;

public class Answer_Model {
    private String answerer;

    public Answer_Model() {
    }

    public Answer_Model(String caller) {
        this.answerer = caller;
    }

    public String getAnswerer() { return answerer; }

    public void setAnswerer(String answerer) { this.answerer = answerer; }
}
