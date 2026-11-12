package com.giasuly9.model;

import java.sql.Timestamp;

/**
 * Môn học (chỉ môn Lý 9 trong phạm vi assignment, nhưng thiết kế để mở rộng).
 */
public class Subject {

    private int subjectId;
    private String name;
    private String code;
    private String description;
    private String imageUrl;
    private int teacherId;
    private String teacherName;
    private boolean active;
    private Timestamp createdAt;

    public Subject() {
    }

    public Subject(int subjectId, String name, String code, String description, String imageUrl,
            int teacherId, String teacherName, boolean active) {
        this.subjectId = subjectId;
        this.name = name;
        this.code = code;
        this.description = description;
        this.imageUrl = imageUrl;
        this.teacherId = teacherId;
        this.teacherName = teacherName;
        this.active = active;
    }

    public int getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(int subjectId) {
        this.subjectId = subjectId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
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

    public int getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(int teacherId) {
        this.teacherId = teacherId;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
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
}
