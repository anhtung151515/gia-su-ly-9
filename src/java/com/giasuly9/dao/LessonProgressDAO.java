package com.giasuly9.dao;

import com.giasuly9.model.LessonProgress;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class LessonProgressDAO extends DBContext {

    public LessonProgressDAO() {
        super();
        ensureTable();
    }

    private void ensureTable() {
        String sql = "IF OBJECT_ID('dbo.LessonProgress', 'U') IS NULL "
                + "CREATE TABLE dbo.LessonProgress ("
                + " UserID INT NOT NULL,"
                + " LessonID INT NOT NULL,"
                + " PercentValue INT NOT NULL DEFAULT 0,"
                + " Completed BIT NOT NULL DEFAULT 0,"
                + " UpdatedAt DATETIME NOT NULL DEFAULT GETDATE(),"
                + " PRIMARY KEY (UserID, LessonID)"
                + ")";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Map<Integer, LessonProgress> mapByUser(int userId) {
        Map<Integer, LessonProgress> map = new HashMap<>();
        String sql = "SELECT UserID, LessonID, PercentValue, Completed FROM LessonProgress WHERE UserID = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    LessonProgress p = new LessonProgress();
                    p.setUserId(rs.getInt("UserID"));
                    p.setLessonId(rs.getInt("LessonID"));
                    p.setPercent(rs.getInt("PercentValue"));
                    p.setCompleted(rs.getBoolean("Completed"));
                    map.put(p.getLessonId(), p);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }

    public int countCompleted(int userId) {
        String sql = "SELECT COUNT(*) FROM LessonProgress WHERE UserID = ? AND Completed = 1";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, userId);
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

    /**
     * Lấy tiến độ đã lưu của một bài giảng (0 nếu chưa xem).
     */
    public int findPercent(int userId, int lessonId) {
        String sql = "SELECT PercentValue FROM LessonProgress WHERE UserID = ? AND LessonID = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, lessonId);
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

    public void upsert(int userId, int lessonId, int percent, boolean completed) {
        if (percent < 0) {
            percent = 0;
        }
        if (percent > 100) {
            percent = 100;
        }
        if (percent >= 90) {
            completed = true;
            percent = 100;
        }
        String sql = "MERGE LessonProgress AS t "
                + "USING (SELECT ? AS UserID, ? AS LessonID, ? AS PercentValue, ? AS Completed) AS s "
                + "ON t.UserID = s.UserID AND t.LessonID = s.LessonID "
                + "WHEN MATCHED THEN UPDATE SET PercentValue = CASE WHEN s.PercentValue > t.PercentValue THEN s.PercentValue ELSE t.PercentValue END, "
                + " Completed = CASE WHEN s.Completed = 1 OR t.Completed = 1 THEN 1 ELSE 0 END, UpdatedAt = GETDATE() "
                + "WHEN NOT MATCHED THEN INSERT (UserID, LessonID, PercentValue, Completed, UpdatedAt) "
                + "VALUES (s.UserID, s.LessonID, s.PercentValue, s.Completed, GETDATE());";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, lessonId);
            stmt.setInt(3, percent);
            stmt.setBoolean(4, completed);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Đặt tiến độ theo ý muốn của học sinh (nút "Đánh dấu đã học").
     * <p>Khác với {@link #upsert} ở chỗ có thể ghi đè cả tiến độ cũ xuống 0
     * khi học sinh bấm bỏ đánh dấu.
     */
    public void setManual(int userId, int lessonId, int percent, boolean completed) {
        if (percent < 0) {
            percent = 0;
        }
        if (percent > 100) {
            percent = 100;
        }
        if (percent >= 90) {
            completed = true;
            percent = 100;
        }
        String sql = "MERGE LessonProgress AS t "
                + "USING (SELECT ? AS UserID, ? AS LessonID, ? AS PercentValue, ? AS Completed) AS s "
                + "ON t.UserID = s.UserID AND t.LessonID = s.LessonID "
                + "WHEN MATCHED THEN UPDATE SET PercentValue = s.PercentValue, "
                + " Completed = s.Completed, UpdatedAt = GETDATE() "
                + "WHEN NOT MATCHED THEN INSERT (UserID, LessonID, PercentValue, Completed, UpdatedAt) "
                + "VALUES (s.UserID, s.LessonID, s.PercentValue, s.Completed, GETDATE());";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, lessonId);
            stmt.setInt(3, percent);
            stmt.setBoolean(4, completed);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
