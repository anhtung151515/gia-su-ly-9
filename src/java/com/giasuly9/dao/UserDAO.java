package com.giasuly9.dao;

import com.giasuly9.model.User;
import com.giasuly9.util.SecurityUtil;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class UserDAO extends DBContext {

    public UserDAO() {
        super();
    }

    public UserDAO(java.sql.Connection conn) {
        super(conn);
    }

    // ------------------------------------------------------------------
    // Truy vấn
    // ------------------------------------------------------------------
    public User authenticate(String username, String rawPassword) {
        String sql = "SELECT UserID, Username, PasswordHash, Salt, FullName, Email, Phone, Role, "
                + "AvatarUrl, IsActive, CreatedAt FROM Users WHERE Username = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User u = map(rs);
                    String salt = u.getSalt();
                    if (salt == null) {
                        // Trường hợp dữ liệu cũ lưu plaintext
                        if (rawPassword != null && rawPassword.equals(u.getPasswordHash())) {
                            return u;
                        }
                        return null;
                    }
                    String hashed = SecurityUtil.hashPassword(rawPassword, salt);
                    if (hashed.equals(u.getPasswordHash())) {
                        return u;
                    }
                    return null;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public User findById(int userId) {
        String sql = "SELECT UserID, Username, PasswordHash, Salt, FullName, Email, Phone, Role, "
                + "AvatarUrl, IsActive, CreatedAt FROM Users WHERE UserID = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, userId);
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

    public boolean existsByUsername(String username) {
        String sql = "SELECT COUNT(*) FROM Users WHERE Username = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<User> listByRole(String role) {
        List<User> list = new ArrayList<>();
        String sql = "SELECT UserID, Username, PasswordHash, Salt, FullName, Email, Phone, Role, "
                + "AvatarUrl, IsActive, CreatedAt FROM Users WHERE Role = ? ORDER BY FullName";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, role);
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

    public List<User> listAll() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT UserID, Username, PasswordHash, Salt, FullName, Email, Phone, Role, "
                + "AvatarUrl, IsActive, CreatedAt FROM Users ORDER BY Role, FullName";
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

    public int countByRole(String role) {
        String sql = "SELECT COUNT(*) FROM Users WHERE Role = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, role);
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

    // ------------------------------------------------------------------
    // Ghi
    // ------------------------------------------------------------------
    /**
     * Tạo tài khoản. Trả về userId, -1 nếu lỗi.
     */
    public int create(String username, String rawPassword, String fullName, String email,
            String phone, String role, String className) {
        String salt = SecurityUtil.newSalt();
        String hash = SecurityUtil.hashPassword(rawPassword, salt);
        String sql = "INSERT INTO Users (Username, PasswordHash, Salt, FullName, Email, Phone, Role, "
                + "ClassName, AvatarUrl, IsActive, CreatedAt) VALUES (?, ?, ?, ?, ?, ?, ?, ?, null, 1, GETDATE())";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, username);
            stmt.setString(2, hash);
            stmt.setString(3, salt);
            stmt.setString(4, fullName);
            stmt.setString(5, email);
            stmt.setString(6, phone);
            stmt.setString(7, role);
            stmt.setString(8, className);
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

    public boolean update(int userId, String fullName, String email, String phone, String className) {
        String sql = "UPDATE Users SET FullName = ?, Email = ?, Phone = ?, ClassName = ? WHERE UserID = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, fullName);
            stmt.setString(2, email);
            stmt.setString(3, phone);
            stmt.setString(4, className);
            stmt.setInt(5, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateRole(int userId, String role) {
        String sql = "UPDATE Users SET Role = ? WHERE UserID = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, role);
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean setActive(int userId, boolean active) {
        String sql = "UPDATE Users SET IsActive = ? WHERE UserID = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setBoolean(1, active);
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean changePassword(int userId, String rawPassword) {
        String salt = SecurityUtil.newSalt();
        String hash = SecurityUtil.hashPassword(rawPassword, salt);
        String sql = "UPDATE Users SET PasswordHash = ?, Salt = ? WHERE UserID = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, hash);
            stmt.setString(2, salt);
            stmt.setInt(3, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Xoá tài khoản và toàn bộ dữ liệu phụ thuộc.
     *
     * Các bảng con (AiLogs, LessonProgress, PracticeSessions + PracticeAnswers,
     * TeacherProfiles, Subjects, Lessons, Questions, AI_SETTINGS) đều có khoá
     * ngoại trỏ về Users với ON DELETE NO_ACTION, nên phải dọn dữ liệu con
     * TRƯỚC rồi mới xoá dòng trong Users.
     */
    public boolean delete(int userId) {
        String[] dependentSqls = {
            // Xoá câu trả lời của các phiên luyện tập thuộc học sinh trước
            "DELETE FROM PracticeAnswers WHERE SessionID IN "
                    + "(SELECT SessionID FROM PracticeSessions WHERE StudentID = ?)",
            "DELETE FROM PracticeSessions WHERE StudentID = ?",
            "DELETE FROM LessonProgress WHERE UserID = ?",
            "DELETE FROM TeacherProfiles WHERE UserID = ?",
            "DELETE FROM AiLogs WHERE UserID = ?",
            // NULL hoá các tham chiếu có thể để trống
            "UPDATE Subjects SET TeacherID = NULL WHERE TeacherID = ?",
            "UPDATE Lessons SET CreatedBy = NULL WHERE CreatedBy = ?",
            "UPDATE Questions SET CreatedBy = NULL WHERE CreatedBy = ?",
            "UPDATE AI_SETTINGS SET CreatedBy = NULL WHERE CreatedBy = ?"
        };

        try {
            boolean previousAutoCommit = getConnection().getAutoCommit();
            getConnection().setAutoCommit(false);
            try {
                for (String sql : dependentSqls) {
                    try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
                        stmt.setInt(1, userId);
                        stmt.executeUpdate();
                    }
                }
                try (PreparedStatement stmt = getConnection().prepareStatement("DELETE FROM Users WHERE UserID = ?")) {
                    stmt.setInt(1, userId);
                    boolean deleted = stmt.executeUpdate() > 0;
                    getConnection().commit();
                    getConnection().setAutoCommit(previousAutoCommit);
                    return deleted;
                }
            } catch (SQLException e) {
                getConnection().rollback();
                getConnection().setAutoCommit(previousAutoCommit);
                e.printStackTrace();
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private User map(ResultSet rs) throws SQLException {
        User u = new User();
        u.setUserId(rs.getInt("UserID"));
        u.setUsername(rs.getString("Username"));
        u.setPasswordHash(rs.getString("PasswordHash"));
        try {
            u.setSalt(rs.getString("Salt"));
        } catch (SQLException ignored) {
        }
        u.setFullName(rs.getString("FullName"));
        u.setEmail(rs.getString("Email"));
        u.setPhone(rs.getString("Phone"));
        u.setRole(rs.getString("Role"));
        try {
            u.setAvatarUrl(rs.getString("AvatarUrl"));
        } catch (SQLException ignored) {
        }
        u.setActive(rs.getBoolean("IsActive"));
        try {
            u.setCreatedAt(rs.getTimestamp("CreatedAt"));
        } catch (SQLException ignored) {
        }
        try {
            u.setClassName(rs.getString("ClassName"));
        } catch (SQLException ignored) {
        }
        return u;
    }
}
