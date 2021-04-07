package com.speech.ailotok.model;

public class FlowNode {

    private String question;
    private String negativeResponse;
    private String positiveResponse;
    private boolean nodeFinished = false;


    public FlowNode(){
    }

    public String getPositiveResponse() {
        nodeFinished = true;
        return positiveResponse;
    }

    public String getNegativeResponse() {
        nodeFinished = true;
        return negativeResponse;
    }

    public String getQuestion() {
        if (negativeResponse == null && positiveResponse == null)
            nodeFinished = true;
        return question;
    }

    public boolean isNodeFinished() {
        return nodeFinished;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public void setNegativeResponse(String negativeResponse) {
        this.negativeResponse = negativeResponse;
    }

    public void setPositiveResponse(String positiveResponse) {
        this.positiveResponse = positiveResponse;
    }
}
