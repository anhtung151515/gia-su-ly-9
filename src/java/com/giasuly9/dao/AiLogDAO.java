package com.giasuly9.dao;

import com.giasuly9.model.AiLog;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class AiLogDAO extends DBContext {

    public AiLogDAO() {
        super();
    }

    public int save(AiLog log) {
        String sql = "INSERT INTO AiLogs (UserID, Feature, PromptText, ResponseText, TokensUsed, "
                + "Success, ErrorMessage, CreatedAt) VALUES (?, ?, ?, ?, ?, ?, ?, GETDATE())";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (log.getUserId() > 0) {
                stmt.setInt(1, log.getUserId());
            } else {
                stmt.setNull(1, java.sql.Types.INTEGER);
            }
            stmt.setString(2, log.getFeature());
            stmt.setString(3, truncate(log.getPromptText(), 4000));
            stmt.setString(4, truncate(log.getResponseText(), 8000));
            stmt.setInt(5, log.getTokensUsed());
            stmt.setBoolean(6, log.isSuccess());
            stmt.setString(7, log.getErrorMessage());
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

    public List<AiLog> listRecent(int limit) {
        List<AiLog> list = new ArrayList<>();
        String sql = "SELECT TOP " + Math.max(1, limit) + " l.LogID, l.UserID, u.FullName, l.Feature, "
                + "l.PromptText, l.ResponseText, l.TokensUsed, l.Success, l.ErrorMessage, l.CreatedAt "
                + "FROM AiLogs l LEFT JOIN Users u ON l.UserID = u.UserID ORDER BY l.LogID DESC";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    AiLog log = new AiLog();
                    log.setLogId(rs.getInt("LogID"));
                    log.setUserId(rs.getInt("UserID"));
                    try {
                        log.setFullName(rs.getString("FullName"));
                    } catch (SQLException ignored) {
                    }
                    log.setFeature(rs.getString("Feature"));
                    log.setPromptText(rs.getString("PromptText"));
                    log.setResponseText(rs.getString("ResponseText"));
                    log.setTokensUsed(rs.getInt("TokensUsed"));
                    log.setSuccess(rs.getBoolean("Success"));
                    log.setErrorMessage(rs.getString("ErrorMessage"));
                    try {
                        log.setCreatedAt(rs.getTimestamp("CreatedAt"));
                    } catch (SQLException ignored) {
                    }
                    list.add(log);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public int countAll() {
        String sql = "SELECT COUNT(*) FROM AiLogs";
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

    private String truncate(String s, int max) {
        if (s == null) {
            return null;
        }
        return s.length() <= max ? s : s.substring(0, max);
    }
}
