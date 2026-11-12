package com.giasuly9.model;

import java.sql.Timestamp;

/**
 * Bước hội thoại AI trong chế độ "giải từng bước" (tutor).
 * Mỗi lượt AI sẽ dừng lại và yêu cầu học sinh nhập số liệu/dữ liệu mới.
 */
public class TutorStep {

    private int stepId;
    private int sessionId;
    private int questionId;
    private int stepOrder;
    /** USER | AI */
    private String speaker;
    private String message;
    /** CORRECT | HINT | NEED_DATA | EXPLANATION | FINAL */
    private String stepType;
    private boolean awaitingUser;
    private Timestamp createdAt;

    public TutorStep() {
    }

    public int getStepId() {
        return stepId;
    }

    public void setStepId(int stepId) {
        this.stepId = stepId;
    }

    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    public int getQuestionId() {
        return questionId;
    }

    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }

    public int getStepOrder() {
        return stepOrder;
    }

    public void setStepOrder(int stepOrder) {
        this.stepOrder = stepOrder;
    }

    public String getSpeaker() {
        return speaker;
    }

    public void setSpeaker(String speaker) {
        this.speaker = speaker;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStepType() {
        return stepType;
    }

    public void setStepType(String stepType) {
        this.stepType = stepType;
    }

    public boolean isAwaitingUser() {
        return awaitingUser;
    }

    public void setAwaitingUser(boolean awaitingUser) {
        this.awaitingUser = awaitingUser;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
