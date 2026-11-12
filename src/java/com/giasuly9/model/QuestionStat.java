package com.giasuly9.model;

/**
 * Thống kê một câu hỏi trong ngân hàng: học sinh làm sai bao nhiêu lần.
 */
public class QuestionStat {

    private int questionId;
    private String content;
    /** MCQ | ESSAY */
    private String questionType;
    /** EASY | MEDIUM | HARD */
    private String difficulty;
    private int topicId;
    private String topicName;

    /** Tổng số lượt học sinh đã làm câu này. */
    private int totalAttempts;
    /** Số lượt làm sai. */
    private int wrongCount;
    /** Số lượt làm đúng. */
    private int correctCount;
    /** Điểm trung bình của câu (thang 0-10). */
    private double avgScore;

    public QuestionStat() {
    }

    public boolean isMcq() {
        return "MCQ".equalsIgnoreCase(questionType);
    }

    public boolean isEssay() {
        return "ESSAY".equalsIgnoreCase(questionType);
    }

    /**
     * Tỉ lệ sai (0-100).
     */
    public double getWrongRate() {
        if (totalAttempts <= 0) {
            return 0;
        }
        return wrongCount * 100.0 / totalAttempts;
    }

    /**
     * Mức độ nguy hiểm: càng nhiều lượt sai trên một câu càng cần chú ý.
     * Dùng để tô màu cảnh báo trên giao diện.
     */
    public String getRiskLevel() {
        if (wrongCount <= 0) {
            return "NONE";
        }
        double rate = getWrongRate();
        if (wrongCount >= 5 || rate >= 80) {
            return "HIGH";
        }
        if (wrongCount >= 3 || rate >= 50) {
            return "MEDIUM";
        }
        return "LOW";
    }

    public int getQuestionId() {
        return questionId;
    }

    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getQuestionType() {
        return questionType;
    }

    public void setQuestionType(String questionType) {
        this.questionType = questionType;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public int getTopicId() {
        return topicId;
    }

    public void setTopicId(int topicId) {
        this.topicId = topicId;
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public int getTotalAttempts() {
        return totalAttempts;
    }

    public void setTotalAttempts(int totalAttempts) {
        this.totalAttempts = totalAttempts;
    }

    public int getWrongCount() {
        return wrongCount;
    }

    public void setWrongCount(int wrongCount) {
        this.wrongCount = wrongCount;
    }

    public int getCorrectCount() {
        return correctCount;
    }

    public void setCorrectCount(int correctCount) {
        this.correctCount = correctCount;
    }

    public double getAvgScore() {
        return avgScore;
    }

    public void setAvgScore(double avgScore) {
        this.avgScore = avgScore;
    }
}
