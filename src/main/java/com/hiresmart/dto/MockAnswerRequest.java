package com.hiresmart.dto;

public class MockAnswerRequest {
    private Long questionId;
    private String answerText;

    public MockAnswerRequest() {}
    public MockAnswerRequest(Long questionId, String answerText) {
        this.questionId = questionId;
        this.answerText = answerText;
    }

    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }

    public String getAnswerText() { return answerText; }
    public void setAnswerText(String answerText) { this.answerText = answerText; }

    public String getCandidateAnswer() { return answerText; }
    public void setCandidateAnswer(String candidateAnswer) {
        if (candidateAnswer != null && !candidateAnswer.isBlank()) {
            this.answerText = candidateAnswer;
        }
    }
}
