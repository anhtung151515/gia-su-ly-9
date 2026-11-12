package com.giasuly9.model;

/**
 * Tiến độ học của học sinh theo từng chủ đề.
 */
public class TopicLearning {

    private int topicId;
    private String topicName;
    private String topicImage;

    /** Tổng số bài giảng đang hoạt động của chủ đề. */
    private int totalLessons;
    /** Số bài đã học xong. */
    private int doneLessons;
    /** Số bài đang học dở (đã xem một phần). */
    private int inProgressLessons;
    /** Số bài chưa bắt đầu. */
    private int notStartedLessons;

    public TopicLearning() {
    }

    /**
     * Tỉ lệ đã học (0-100).
     */
    public int getPercent() {
        if (totalLessons <= 0) {
            return 0;
        }
        return (int) Math.round(doneLessons * 100.0 / totalLessons);
    }

    /**
     * Trạng thái tổng thể của chủ đề.
     */
    public String getStatus() {
        if (totalLessons <= 0) {
            return "EMPTY";
        }
        if (doneLessons >= totalLessons) {
            return "DONE";
        }
        if (doneLessons > 0 || inProgressLessons > 0) {
            return "DOING";
        }
        return "NOT_STARTED";
    }

    public boolean isFullyDone() {
        return totalLessons > 0 && doneLessons >= totalLessons;
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

    public String getTopicImage() {
        return topicImage;
    }

    public void setTopicImage(String topicImage) {
        this.topicImage = topicImage;
    }

    public int getTotalLessons() {
        return totalLessons;
    }

    public void setTotalLessons(int totalLessons) {
        this.totalLessons = totalLessons;
    }

    public int getDoneLessons() {
        return doneLessons;
    }

    public void setDoneLessons(int doneLessons) {
        this.doneLessons = doneLessons;
    }

    public int getInProgressLessons() {
        return inProgressLessons;
    }

    public void setInProgressLessons(int inProgressLessons) {
        this.inProgressLessons = inProgressLessons;
    }

    public int getNotStartedLessons() {
        return notStartedLessons;
    }

    public void setNotStartedLessons(int notStartedLessons) {
        this.notStartedLessons = notStartedLessons;
    }
}
