package com.giasuly9.dao;

import com.giasuly9.model.Lesson;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class LessonDAO extends DBContext {

    public LessonDAO() {
        super();
    }

    public LessonDAO(java.sql.Connection conn) {
        super(conn);
    }

    private String baseSelect() {
        return "SELECT l.LessonID, l.SubjectID, l.TopicID, s.Name AS SubjectName, t.Name AS TopicName, "
                + "l.Title, l.Description, l.LessonType, l.VideoUrl, l.DocumentUrl, l.Content, "
                + "l.DurationMinutes, l.CreatedBy, u.FullName AS CreatedByName, l.IsActive, l.CreatedAt "
                + "FROM Lessons l "
                + "LEFT JOIN Subjects s ON l.SubjectID = s.SubjectID "
                + "LEFT JOIN Topics t ON l.TopicID = t.TopicID "
                + "LEFT JOIN Users u ON l.CreatedBy = u.UserID ";
    }

    public List<Lesson> listByTopic(int topicId) {
        List<Lesson> list = new ArrayList<>();
        String sql = baseSelect() + "WHERE l.TopicID = ? AND l.IsActive = 1 ORDER BY l.LessonID DESC";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, topicId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Lesson> listBySubject(int subjectId) {
        List<Lesson> list = new ArrayList<>();
        String sql = baseSelect() + "WHERE l.SubjectID = ? AND l.IsActive = 1 ORDER BY l.LessonID DESC";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, subjectId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Lesson> listAll() {
        List<Lesson> list = new ArrayList<>();
        String sql = baseSelect() + "ORDER BY l.LessonID DESC";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Lesson> listByCreator(int userId) {
        List<Lesson> list = new ArrayList<>();
        String sql = baseSelect() + "WHERE l.CreatedBy = ? ORDER BY l.LessonID DESC";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Danh sách bài giảng do một giáo viên tạo (chỉ bài đang hoạt động).
     */
    public List<Lesson> listActiveByCreator(int userId) {
        List<Lesson> list = new ArrayList<>();
        String sql = baseSelect() + "WHERE l.CreatedBy = ? AND l.IsActive = 1 ORDER BY l.LessonID DESC";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Bài giảng video (không tính bài đọc TEXT) của một giáo viên.
     */
    public List<Lesson> listVideosByCreator(int userId) {
        List<Lesson> list = new ArrayList<>();
        String sql = baseSelect() + "WHERE l.CreatedBy = ? AND l.IsActive = 1 "
                + "AND l.LessonType IN ('VIDEO_YOUTUBE','VIDEO_FILE') ORDER BY l.LessonID DESC";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Lọc bài giảng theo giáo viên và (tuỳ chọn) theo chủ đề.
     */
    public List<Lesson> listByCreatorAndTopic(int userId, int topicId) {
        List<Lesson> list = new ArrayList<>();
        String sql = baseSelect() + "WHERE l.CreatedBy = ? AND l.IsActive = 1 "
                + (topicId > 0 ? "AND l.TopicID = ? " : "")
                + "ORDER BY l.LessonID DESC";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, userId);
            if (topicId > 0) {
                stmt.setInt(2, topicId);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Lesson findById(int lessonId) {
        String sql = baseSelect() + "WHERE l.LessonID = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, lessonId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int create(Lesson l) {
        String sql = "INSERT INTO Lessons (SubjectID, TopicID, Title, Description, LessonType, VideoUrl, "
                + "DocumentUrl, Content, DurationMinutes, CreatedBy, IsActive, CreatedAt) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 1, GETDATE())";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, l.getSubjectId());
            stmt.setInt(2, l.getTopicId());
            stmt.setString(3, l.getTitle());
            stmt.setString(4, l.getDescription());
            stmt.setString(5, l.getLessonType());
            stmt.setString(6, l.getVideoUrl());
            stmt.setString(7, l.getDocumentUrl());
            stmt.setString(8, l.getContent());
            stmt.setInt(9, l.getDurationMinutes());
            stmt.setInt(10, l.getCreatedBy());
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public boolean update(Lesson l) {
        String sql = "UPDATE Lessons SET SubjectID = ?, TopicID = ?, Title = ?, Description = ?, "
                + "LessonType = ?, VideoUrl = ?, DocumentUrl = ?, Content = ?, DurationMinutes = ? "
                + "WHERE LessonID = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, l.getSubjectId());
            stmt.setInt(2, l.getTopicId());
            stmt.setString(3, l.getTitle());
            stmt.setString(4, l.getDescription());
            stmt.setString(5, l.getLessonType());
            stmt.setString(6, l.getVideoUrl());
            stmt.setString(7, l.getDocumentUrl());
            stmt.setString(8, l.getContent());
            stmt.setInt(9, l.getDurationMinutes());
            stmt.setInt(10, l.getLessonId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean setActive(int lessonId, boolean active) {
        String sql = "UPDATE Lessons SET IsActive = ? WHERE LessonID = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setBoolean(1, active);
            stmt.setInt(2, lessonId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(int lessonId) {
        String sql = "DELETE FROM Lessons WHERE LessonID = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, lessonId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public int countByTopic(int topicId) {
        String sql = "SELECT COUNT(*) FROM Lessons WHERE TopicID = ? AND IsActive = 1";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, topicId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private Lesson map(ResultSet rs) throws SQLException {
        Lesson l = new Lesson();
        l.setLessonId(rs.getInt("LessonID"));
        l.setSubjectId(rs.getInt("SubjectID"));
        l.setTopicId(rs.getInt("TopicID"));
        try {
            l.setSubjectName(rs.getString("SubjectName"));
        } catch (SQLException ignored) {
        }
        try {
            l.setTopicName(rs.getString("TopicName"));
        } catch (SQLException ignored) {
        }
        l.setTitle(rs.getString("Title"));
        l.setDescription(rs.getString("Description"));
        l.setLessonType(rs.getString("LessonType"));
        l.setVideoUrl(rs.getString("VideoUrl"));
        l.setDocumentUrl(rs.getString("DocumentUrl"));
        l.setContent(rs.getString("Content"));
        l.setDurationMinutes(rs.getInt("DurationMinutes"));
        l.setCreatedBy(rs.getInt("CreatedBy"));
        try {
            l.setCreatedByName(rs.getString("CreatedByName"));
        } catch (SQLException ignored) {
        }
        l.setActive(rs.getBoolean("IsActive"));
        try {
            l.setCreatedAt(rs.getTimestamp("CreatedAt"));
        } catch (SQLException ignored) {
        }
        return l;
    }
}
