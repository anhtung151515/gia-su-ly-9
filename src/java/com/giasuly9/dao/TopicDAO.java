package com.giasuly9.dao;

import com.giasuly9.model.Topic;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class TopicDAO extends DBContext {

    public TopicDAO() {
        super();
    }

    public TopicDAO(java.sql.Connection conn) {
        super(conn);
    }

    public List<Topic> listBySubject(int subjectId) {
        List<Topic> list = new ArrayList<>();
        String sql = "SELECT t.TopicID, t.SubjectID, s.Name AS SubjectName, t.Name, t.Description, "
                + "t.ImageUrl, t.OrderIndex, t.IsActive, t.CreatedAt, "
                + "(SELECT COUNT(*) FROM Lessons l WHERE l.TopicID = t.TopicID AND l.IsActive = 1) AS LessonCount "
                + "FROM Topics t JOIN Subjects s ON t.SubjectID = s.SubjectID "
                + "WHERE t.SubjectID = ? ORDER BY t.OrderIndex, t.Name";
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

    public List<Topic> listAll() {
        List<Topic> list = new ArrayList<>();
        String sql = "SELECT t.TopicID, t.SubjectID, s.Name AS SubjectName, t.Name, t.Description, "
                + "t.ImageUrl, t.OrderIndex, t.IsActive, t.CreatedAt, "
                + "(SELECT COUNT(*) FROM Lessons l WHERE l.TopicID = t.TopicID AND l.IsActive = 1) AS LessonCount "
                + "FROM Topics t JOIN Subjects s ON t.SubjectID = s.SubjectID "
                + "ORDER BY s.Name, t.OrderIndex, t.Name";
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

    public Topic findById(int topicId) {
        String sql = "SELECT t.TopicID, t.SubjectID, s.Name AS SubjectName, t.Name, t.Description, "
                + "t.ImageUrl, t.OrderIndex, t.IsActive, t.CreatedAt FROM Topics t "
                + "JOIN Subjects s ON t.SubjectID = s.SubjectID WHERE t.TopicID = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, topicId);
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

    public int create(int subjectId, String name, String description, int orderIndex) {
        String sql = "INSERT INTO Topics (SubjectID, Name, Description, OrderIndex, IsActive, CreatedAt) "
                + "VALUES (?, ?, ?, ?, 1, GETDATE())";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, subjectId);
            stmt.setString(2, name);
            stmt.setString(3, description);
            stmt.setInt(4, orderIndex);
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

    public boolean update(int topicId, int subjectId, String name, String description, int orderIndex) {
        String sql = "UPDATE Topics SET SubjectID = ?, Name = ?, Description = ?, OrderIndex = ? WHERE TopicID = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, subjectId);
            stmt.setString(2, name);
            stmt.setString(3, description);
            stmt.setInt(4, orderIndex);
            stmt.setInt(5, topicId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean setActive(int topicId, boolean active) {
        String sql = "UPDATE Topics SET IsActive = ? WHERE TopicID = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setBoolean(1, active);
            stmt.setInt(2, topicId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(int topicId) {
        String sql = "DELETE FROM Topics WHERE TopicID = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, topicId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Topic map(ResultSet rs) throws SQLException {
        Topic t = new Topic();
        t.setTopicId(rs.getInt("TopicID"));
        t.setSubjectId(rs.getInt("SubjectID"));
        try {
            t.setSubjectName(rs.getString("SubjectName"));
        } catch (SQLException ignored) {
        }
        t.setName(rs.getString("Name"));
        t.setDescription(rs.getString("Description"));
        try {
            t.setImageUrl(rs.getString("ImageUrl"));
        } catch (SQLException ignored) {
        }
        t.setOrderIndex(rs.getInt("OrderIndex"));
        t.setActive(rs.getBoolean("IsActive"));
        try {
            t.setCreatedAt(rs.getTimestamp("CreatedAt"));
        } catch (SQLException ignored) {
        }
        try {
            t.setLessonCount(rs.getInt("LessonCount"));
        } catch (SQLException ignored) {
        }
        return t;
    }
}
