package com.giasuly9.dao;

import com.giasuly9.model.TeacherProfile;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TeacherProfileDAO extends DBContext {

    public TeacherProfileDAO() {
        super();
    }

    public TeacherProfileDAO(java.sql.Connection conn) {
        super(conn);
    }

    private static final String BASE_SELECT =
            "SELECT tp.ProfileID, tp.UserID, tp.FullName, tp.Degree, tp.Title, tp.School, "
            + "tp.Specialty, tp.Bio, tp.AvatarUrl, tp.Color, tp.YearsExp, tp.StudentCount, "
            + "tp.VideoCount, tp.IsActive "
            + "FROM TeacherProfiles tp ";

    /**
     * Danh sách giáo viên đang hoạt động, kèm số video bài giảng thực tế.
     */
    public List<TeacherProfile> listActive() {
        List<TeacherProfile> list = new ArrayList<>();
        String sql = BASE_SELECT
                + "WHERE tp.IsActive = 1 "
                + "ORDER BY tp.VideoCount DESC, tp.YearsExp DESC, tp.FullName";
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

    public TeacherProfile findByUserId(int userId) {
        String sql = BASE_SELECT + "WHERE tp.UserID = ?";
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

    /**
     * Cập nhật lại số lượng video bài giảng cho từng giáo viên.
     */
    public void refreshVideoCounts() {
        String sql = "UPDATE tp SET tp.VideoCount = "
                + "(SELECT COUNT(*) FROM Lessons l WHERE l.CreatedBy = tp.UserID AND l.IsActive = 1) "
                + "FROM TeacherProfiles tp";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private TeacherProfile map(ResultSet rs) throws SQLException {
        TeacherProfile t = new TeacherProfile();
        t.setProfileId(rs.getInt("ProfileID"));
        t.setUserId(rs.getInt("UserID"));
        t.setFullName(rs.getString("FullName"));
        t.setDegree(rs.getString("Degree"));
        t.setTitle(rs.getString("Title"));
        t.setSchool(rs.getString("School"));
        t.setSpecialty(rs.getString("Specialty"));
        t.setBio(rs.getString("Bio"));
        t.setAvatarUrl(rs.getString("AvatarUrl"));
        t.setColor(rs.getString("Color"));
        t.setYearsExp(rs.getInt("YearsExp"));
        t.setStudentCount(rs.getInt("StudentCount"));
        t.setVideoCount(rs.getInt("VideoCount"));
        t.setActive(rs.getBoolean("IsActive"));
        return t;
    }
}
