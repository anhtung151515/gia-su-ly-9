package com.giasuly9.model;

import java.sql.Timestamp;

/**
 * Chủ đề (topic) trong một môn học. Ví dụ: "Điện và mạch điện", "Cơ năng", ...
 */
public class Topic {

    private int topicId;
    private int subjectId;
    private String subjectName;
    private String name;
    private String description;
    private String imageUrl;
    private int orderIndex;
    private boolean active;
    private Timestamp createdAt;
    private int lessonCount;

    public Topic() {
    }

    public Topic(int topicId, int subjectId, String subjectName, String name, String description,
            int orderIndex, boolean active) {
        this.topicId = topicId;
        this.subjectId = subjectId;
        this.subjectName = subjectName;
        this.name = name;
        this.description = description;
        this.orderIndex = orderIndex;
        this.active = active;
    }

    public int getTopicId() {
        return topicId;
    }

    public void setTopicId(int topicId) {
        this.topicId = topicId;
    }

    public int getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(int subjectId) {
        this.subjectId = subjectId;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public int getLessonCount() {
        return lessonCount;
    }

    public void setLessonCount(int lessonCount) {
        this.lessonCount = lessonCount;
    }
}
