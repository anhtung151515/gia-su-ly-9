package com.giasuly9.dao;

import com.giasuly9.model.Subject;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class SubjectDAO extends DBContext {

    public SubjectDAO() {
        super();
    }

    public SubjectDAO(java.sql.Connection conn) {
        super(conn);
    }

    public List<Subject> listAll() {
        List<Subject> list = new ArrayList<>();
        String sql = "SELECT s.SubjectID, s.Name, s.Code, s.Description, s.ImageUrl, s.TeacherID, "
                + "u.FullName AS TeacherName, s.IsActive, s.CreatedAt "
                + "FROM Subjects s LEFT JOIN Users u ON s.TeacherID = u.UserID "
                + "ORDER BY s.Name";
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

    public List<Subject> listActive() {
        List<Subject> list = new ArrayList<>();
        String sql = "SELECT s.SubjectID, s.Name, s.Code, s.Description, s.ImageUrl, s.TeacherID, "
                + "u.FullName AS TeacherName, s.IsActive, s.CreatedAt "
                + "FROM Subjects s LEFT JOIN Users u ON s.TeacherID = u.UserID "
                + "WHERE s.IsActive = 1 ORDER BY s.Name";
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

    public Subject findById(int subjectId) {
        String sql = "SELECT s.SubjectID, s.Name, s.Code, s.Description, s.ImageUrl, s.TeacherID, "
                + "u.FullName AS TeacherName, s.IsActive, s.CreatedAt "
                + "FROM Subjects s LEFT JOIN Users u ON s.TeacherID = u.UserID WHERE s.SubjectID = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, subjectId);
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

    public int create(String name, String code, String description, String imageUrl, int teacherId) {
        String sql = "INSERT INTO Subjects (Name, Code, Description, ImageUrl, TeacherID, IsActive, CreatedAt) "
                + "VALUES (?, ?, ?, ?, ?, 1, GETDATE())";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, name);
            stmt.setString(2, code);
            stmt.setString(3, description);
            stmt.setString(4, imageUrl);
            if (teacherId > 0) {
                stmt.setInt(5, teacherId);
            } else {
                stmt.setNull(5, java.sql.Types.INTEGER);
            }
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

    public boolean update(int subjectId, String name, String code, String description, String imageUrl, int teacherId) {
        String sql = "UPDATE Subjects SET Name = ?, Code = ?, Description = ?, ImageUrl = ?, TeacherID = ? "
                + "WHERE SubjectID = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setString(2, code);
            stmt.setString(3, description);
            stmt.setString(4, imageUrl);
            if (teacherId > 0) {
                stmt.setInt(5, teacherId);
            } else {
                stmt.setNull(5, java.sql.Types.INTEGER);
            }
            stmt.setInt(6, subjectId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean setActive(int subjectId, boolean active) {
        String sql = "UPDATE Subjects SET IsActive = ? WHERE SubjectID = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setBoolean(1, active);
            stmt.setInt(2, subjectId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(int subjectId) {
        String sql = "DELETE FROM Subjects WHERE SubjectID = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, subjectId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Subject map(ResultSet rs) throws SQLException {
        Subject s = new Subject();
        s.setSubjectId(rs.getInt("SubjectID"));
        s.setName(rs.getString("Name"));
        s.setCode(rs.getString("Code"));
        s.setDescription(rs.getString("Description"));
        s.setImageUrl(rs.getString("ImageUrl"));
        try {
            int t = rs.getInt("TeacherID");
            s.setTeacherId(rs.wasNull() ? 0 : t);
        } catch (SQLException ignored) {
        }
        try {
            s.setTeacherName(rs.getString("TeacherName"));
        } catch (SQLException ignored) {
        }
        s.setActive(rs.getBoolean("IsActive"));
        try {
            s.setCreatedAt(rs.getTimestamp("CreatedAt"));
        } catch (SQLException ignored) {
        }
        return s;
    }
}
