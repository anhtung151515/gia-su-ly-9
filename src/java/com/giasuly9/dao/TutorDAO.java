package com.giasuly9.dao;

import com.giasuly9.model.TutorStep;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class TutorDAO extends DBContext {

    public TutorDAO() {
        super();
    }

    public TutorDAO(java.sql.Connection conn) {
        super(conn);
    }

    public int addStep(TutorStep s) {
        String sql = "INSERT INTO TutorSteps (SessionID, QuestionID, StepOrder, Speaker, Message, "
                + "StepType, AwaitingUser, CreatedAt) VALUES (?, ?, ?, ?, ?, ?, ?, GETDATE())";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, s.getSessionId());
            stmt.setInt(2, s.getQuestionId());
            stmt.setInt(3, s.getStepOrder());
            stmt.setString(4, s.getSpeaker());
            stmt.setString(5, s.getMessage());
            stmt.setString(6, s.getStepType());
            stmt.setBoolean(7, s.isAwaitingUser());
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

    public List<TutorStep> listBySession(int sessionId) {
        List<TutorStep> list = new ArrayList<>();
        String sql = "SELECT StepID, SessionID, QuestionID, StepOrder, Speaker, Message, StepType, "
                + "AwaitingUser, CreatedAt FROM TutorSteps WHERE SessionID = ? ORDER BY StepOrder, StepID";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, sessionId);
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

    public List<TutorStep> listBySessionAndQuestion(int sessionId, int questionId) {
        List<TutorStep> list = new ArrayList<>();
        String sql = "SELECT StepID, SessionID, QuestionID, StepOrder, Speaker, Message, StepType, "
                + "AwaitingUser, CreatedAt FROM TutorSteps WHERE SessionID = ? AND QuestionID = ? "
                + "ORDER BY StepOrder, StepID";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, sessionId);
            stmt.setInt(2, questionId);
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

    public int nextStepOrder(int sessionId) {
        String sql = "SELECT ISNULL(MAX(StepOrder), 0) + 1 FROM TutorSteps WHERE SessionID = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, sessionId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 1;
    }

    private TutorStep map(ResultSet rs) throws SQLException {
        TutorStep s = new TutorStep();
        s.setStepId(rs.getInt("StepID"));
        s.setSessionId(rs.getInt("SessionID"));
        s.setQuestionId(rs.getInt("QuestionID"));
        s.setStepOrder(rs.getInt("StepOrder"));
        s.setSpeaker(rs.getString("Speaker"));
        s.setMessage(rs.getString("Message"));
        s.setStepType(rs.getString("StepType"));
        s.setAwaitingUser(rs.getBoolean("AwaitingUser"));
        try {
            s.setCreatedAt(rs.getTimestamp("CreatedAt"));
        } catch (SQLException ignored) {
        }
        return s;
    }
}
