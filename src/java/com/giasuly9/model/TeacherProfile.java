package com.giasuly9.model;

/**
 * Hồ sơ giáo viên hiển thị trên trang công khai: tên, học vị, chuyên môn,
 * số video bài giảng Vật lý 9 đã đăng.
 */
public class TeacherProfile {

    private int profileId;
    private int userId;
    private String fullName;
    private String degree;
    private String title;
    private String school;
    private String specialty;
    private String bio;
    private String avatarUrl;
    private String color;
    private int yearsExp;
    private int studentCount;
    private int videoCount;
    private boolean active;

    public TeacherProfile() {
    }

    public int getProfileId() {
        return profileId;
    }

    public void setProfileId(int profileId) {
        this.profileId = profileId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getDegree() {
        return degree;
    }

    public void setDegree(String degree) {
        this.degree = degree;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getYearsExp() {
        return yearsExp;
    }

    public void setYearsExp(int yearsExp) {
        this.yearsExp = yearsExp;
    }

    public int getStudentCount() {
        return studentCount;
    }

    public void setStudentCount(int studentCount) {
        this.studentCount = studentCount;
    }

    public int getVideoCount() {
        return videoCount;
    }

    public void setVideoCount(int videoCount) {
        this.videoCount = videoCount;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Chữ cái đầu tiên dùng cho avatar dự phòng.
     */
    public String getInitial() {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "GV";
        }
        String name = fullName.trim();
        // Bỏ các từ chức danh "Thầy"/"Cô"
        if (name.startsWith("Thầy ") || name.startsWith("Cô ")) {
            name = name.substring(4).trim();
        }
        String[] parts = name.split("\\s+");
        if (parts.length == 0) {
            return "GV";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(parts[0].charAt(0));
        if (parts.length > 1) {
            sb.append(parts[parts.length - 1].charAt(0));
        }
        return sb.toString().toUpperCase();
    }
}
