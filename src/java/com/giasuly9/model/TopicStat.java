package com.giasuly9.model;

/**
 * Thống kê mức độ sai theo chủ đề.
 */
public class TopicStat {

    private int topicId;
    private String topicName;
    /** Tổng số lượt học sinh đã làm trong chủ đề. */
    private int totalAttempts;
    /** Số lượt làm sai trong chủ đề. */
    private int wrongCount;

    public TopicStat() {
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
}
