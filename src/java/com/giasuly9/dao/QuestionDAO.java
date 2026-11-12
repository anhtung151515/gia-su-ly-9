package com.giasuly9.dao;

import com.giasuly9.model.Question;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class QuestionDAO extends DBContext {

    public QuestionDAO() {
        super();
    }

    public QuestionDAO(java.sql.Connection conn) {
        super(conn);
    }

    private String baseSelect() {
        return "SELECT q.QuestionID, q.SubjectID, q.TopicID, s.Name AS SubjectName, t.Name AS TopicName, "
                + "q.Content, q.QuestionType, q.CorrectAnswer, q.OptionA, q.OptionB, q.OptionC, q.OptionD, "
                + "q.Explanation, q.Difficulty, q.CreatedBy, u.FullName AS CreatedByName, q.Source, "
                + "q.IsActive, q.CreatedAt FROM Questions q "
                + "LEFT JOIN Subjects s ON q.SubjectID = s.SubjectID "
                + "LEFT JOIN Topics t ON q.TopicID = t.TopicID "
                + "LEFT JOIN Users u ON q.CreatedBy = u.UserID ";
    }

    public List<Question> listByTopic(int topicId, String type) {
        List<Question> list = new ArrayList<>();
        String sql = baseSelect() + "WHERE q.TopicID = ? AND q.IsActive = 1 AND q.QuestionType = ? "
                + "ORDER BY NEWID()";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, topicId);
            stmt.setString(2, type);
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

    public List<Question> listByTopicAndType(int topicId, String type, int limit) {
        List<Question> list = new ArrayList<>();
        String sql = baseSelect() + "WHERE q.TopicID = ? AND q.IsActive = 1 AND q.QuestionType = ? "
                + "ORDER BY NEWID()";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, topicId);
            stmt.setString(2, type);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next() && list.size() < limit) {
                    list.add(map(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Question> listAll() {
        List<Question> list = new ArrayList<>();
        String sql = baseSelect() + "ORDER BY q.QuestionID DESC";
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

    public List<Question> listByCreator(int userId) {
        List<Question> list = new ArrayList<>();
        String sql = baseSelect() + "WHERE q.CreatedBy = ? ORDER BY q.QuestionID DESC";
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

    public Question findById(int questionId) {
        String sql = baseSelect() + "WHERE q.QuestionID = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, questionId);
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

    public int countByTopicAndType(int topicId, String type) {
        String sql = "SELECT COUNT(*) FROM Questions WHERE TopicID = ? AND QuestionType = ? AND IsActive = 1";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, topicId);
            stmt.setString(2, type);
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

    public int countAll() {
        String sql = "SELECT COUNT(*) FROM Questions WHERE IsActive = 1";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
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

    public int create(Question q) {
        String sql = "INSERT INTO Questions (SubjectID, TopicID, Content, QuestionType, CorrectAnswer, "
                + "OptionA, OptionB, OptionC, OptionD, Explanation, Difficulty, CreatedBy, Source, "
                + "IsActive, CreatedAt) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 1, GETDATE())";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, q.getSubjectId());
            stmt.setInt(2, q.getTopicId());
            stmt.setString(3, q.getContent());
            stmt.setString(4, q.getType());
            stmt.setString(5, q.getCorrectAnswer());
            stmt.setString(6, q.getOptionA());
            stmt.setString(7, q.getOptionB());
            stmt.setString(8, q.getOptionC());
            stmt.setString(9, q.getOptionD());
            stmt.setString(10, q.getExplanation());
            stmt.setString(11, q.getDifficulty());
            stmt.setInt(12, q.getCreatedBy());
            stmt.setString(13, q.getSource());
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

    /**
     * Chèn nhiều câu hỏi AI sinh ra trong 1 lần.
     * <p>Sau khi chèn, {@code questionId} được gán lại cho từng đối tượng
     * trong danh sách để giao diện có thể xoá / sửa từng câu.
     * <p>Chèn từng câu một (không dùng addBatch) vì SQL Server không trả về
     * generated keys khi thực thi batch - không thể gán questionId nếu dùng batch.
     */
    public int createBatch(List<Question> list) {
        if (list == null || list.isEmpty()) {
            return 0;
        }
        int count = 0;
        for (Question q : list) {
            int newId = create(q);
            if (newId > 0) {
                q.setQuestionId(newId);
                count++;
            }
        }
        return count;
    }

    public boolean update(Question q) {
        String sql = "UPDATE Questions SET SubjectID = ?, TopicID = ?, Content = ?, QuestionType = ?, "
                + "CorrectAnswer = ?, OptionA = ?, OptionB = ?, OptionC = ?, OptionD = ?, "
                + "Explanation = ?, Difficulty = ? WHERE QuestionID = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, q.getSubjectId());
            stmt.setInt(2, q.getTopicId());
            stmt.setString(3, q.getContent());
            stmt.setString(4, q.getType());
            stmt.setString(5, q.getCorrectAnswer());
            stmt.setString(6, q.getOptionA());
            stmt.setString(7, q.getOptionB());
            stmt.setString(8, q.getOptionC());
            stmt.setString(9, q.getOptionD());
            stmt.setString(10, q.getExplanation());
            stmt.setString(11, q.getDifficulty());
            stmt.setInt(12, q.getQuestionId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean setActive(int questionId, boolean active) {
        String sql = "UPDATE Questions SET IsActive = ? WHERE QuestionID = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setBoolean(1, active);
            stmt.setInt(2, questionId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(int questionId) {
        String sql = "DELETE FROM Questions WHERE QuestionID = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, questionId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Question map(ResultSet rs) throws SQLException {
        Question q = new Question();
        q.setQuestionId(rs.getInt("QuestionID"));
        q.setSubjectId(rs.getInt("SubjectID"));
        q.setTopicId(rs.getInt("TopicID"));
        try {
            q.setSubjectName(rs.getString("SubjectName"));
        } catch (SQLException ignored) {
        }
        try {
            q.setTopicName(rs.getString("TopicName"));
        } catch (SQLException ignored) {
        }
        q.setContent(rs.getString("Content"));
        q.setType(rs.getString("QuestionType"));
        q.setCorrectAnswer(rs.getString("CorrectAnswer"));
        q.setOptionA(rs.getString("OptionA"));
        q.setOptionB(rs.getString("OptionB"));
        q.setOptionC(rs.getString("OptionC"));
        q.setOptionD(rs.getString("OptionD"));
        q.setExplanation(rs.getString("Explanation"));
        q.setDifficulty(rs.getString("Difficulty"));
        q.setCreatedBy(rs.getInt("CreatedBy"));
        try {
            q.setCreatedByName(rs.getString("CreatedByName"));
        } catch (SQLException ignored) {
        }
        try {
            q.setSource(rs.getString("Source"));
        } catch (SQLException ignored) {
        }
        q.setActive(rs.getBoolean("IsActive"));
        try {
            q.setCreatedAt(rs.getTimestamp("CreatedAt"));
        } catch (SQLException ignored) {
        }
        return q;
    }
}
