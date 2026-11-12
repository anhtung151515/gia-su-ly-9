package com.giasuly9.dao;

import com.giasuly9.model.PracticeAnswer;
import com.giasuly9.model.PracticeSession;
import com.giasuly9.model.QuestionStat;
import com.giasuly9.model.TopicStat;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PracticeDAO extends DBContext {

    public PracticeDAO() {
        super();
    }

    public PracticeDAO(java.sql.Connection conn) {
        super(conn);
    }

    // ------------------------------------------------------------------
    // Session
    // ------------------------------------------------------------------
    public int createSession(PracticeSession s) {
        String sql = "INSERT INTO PracticeSessions (StudentID, SubjectID, TopicID, PracticeType, "
                + "TotalQuestions, CorrectCount, WrongCount, Score, Status, StartedAt) "
                + "VALUES (?, ?, ?, ?, ?, 0, 0, 0, 'IN_PROGRESS', GETDATE())";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, s.getStudentId());
            stmt.setInt(2, s.getSubjectId());
            stmt.setInt(3, s.getTopicId());
            stmt.setString(4, s.getPracticeType());
            stmt.setInt(5, s.getTotalQuestions());
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

    public void finishSession(int sessionId, int correct, int wrong, double score) {
        String sql = "UPDATE PracticeSessions SET CorrectCount = ?, WrongCount = ?, Score = ?, "
                + "Status = 'COMPLETED', FinishedAt = GETDATE() WHERE SessionID = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, correct);
            stmt.setInt(2, wrong);
            stmt.setDouble(3, score);
            stmt.setInt(4, sessionId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Tính lại điểm của phiên từ các câu trả lời đã lưu (điểm trung bình thang 10).
     *
     * <p>Điểm = tổng điểm các câu đã trả lời / TỔNG SỐ CÂU của phiên.
     * Như vậy câu bỏ trống vẫn tính là 0 điểm, không làm tăng điểm trung bình.
     *
     * @return điểm trung bình thang 10 (0-10), -1 nếu không có câu nào
     */
    public double averageScore(int sessionId) {
        String sql = "SELECT SUM(CAST(Score AS FLOAT)) AS TotalScore, COUNT(*) AS Answered, "
                + "(SELECT TotalQuestions FROM PracticeSessions WHERE SessionID = ?) AS Total "
                + "FROM PracticeAnswers WHERE SessionID = ? AND Score IS NOT NULL";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, sessionId);
            stmt.setInt(2, sessionId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    double totalScore = rs.getDouble("TotalScore");
                    if (rs.wasNull()) {
                        return -1;
                    }
                    int answered = rs.getInt("Answered");
                    int total = rs.getInt("Total");
                    if (total <= 0) {
                        total = answered;
                    }
                    if (total <= 0) {
                        return 0;
                    }
                    double avg = totalScore / total;
                    if (avg < 0) {
                        return 0;
                    }
                    return avg > 10 ? 10 : avg;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public PracticeSession findSessionById(int sessionId) {
        String sql = "SELECT ps.SessionID, ps.StudentID, u.FullName AS StudentName, ps.SubjectID, ps.TopicID, "
                + "s.Name AS SubjectName, t.Name AS TopicName, ps.PracticeType, ps.TotalQuestions, "
                + "ps.CorrectCount, ps.WrongCount, ps.Score, ps.Status, ps.StartedAt, ps.FinishedAt "
                + "FROM PracticeSessions ps "
                + "LEFT JOIN Users u ON ps.StudentID = u.UserID "
                + "LEFT JOIN Subjects s ON ps.SubjectID = s.SubjectID "
                + "LEFT JOIN Topics t ON ps.TopicID = t.TopicID WHERE ps.SessionID = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, sessionId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapSession(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<PracticeSession> listSessionsByStudent(int studentId) {
        List<PracticeSession> list = new ArrayList<>();
        String sql = "SELECT ps.SessionID, ps.StudentID, u.FullName AS StudentName, ps.SubjectID, ps.TopicID, "
                + "s.Name AS SubjectName, t.Name AS TopicName, ps.PracticeType, ps.TotalQuestions, "
                + "ps.CorrectCount, ps.WrongCount, ps.Score, ps.Status, ps.StartedAt, ps.FinishedAt "
                + "FROM PracticeSessions ps "
                + "LEFT JOIN Users u ON ps.StudentID = u.UserID "
                + "LEFT JOIN Subjects s ON ps.SubjectID = s.SubjectID "
                + "LEFT JOIN Topics t ON ps.TopicID = t.TopicID "
                + "WHERE ps.StudentID = ? ORDER BY ps.StartedAt DESC";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapSession(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<PracticeSession> listAllSessions() {
        List<PracticeSession> list = new ArrayList<>();
        String sql = "SELECT ps.SessionID, ps.StudentID, u.FullName AS StudentName, ps.SubjectID, ps.TopicID, "
                + "s.Name AS SubjectName, t.Name AS TopicName, ps.PracticeType, ps.TotalQuestions, "
                + "ps.CorrectCount, ps.WrongCount, ps.Score, ps.Status, ps.StartedAt, ps.FinishedAt "
                + "FROM PracticeSessions ps "
                + "LEFT JOIN Users u ON ps.StudentID = u.UserID "
                + "LEFT JOIN Subjects s ON ps.SubjectID = s.SubjectID "
                + "LEFT JOIN Topics t ON ps.TopicID = t.TopicID ORDER BY ps.StartedAt DESC";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapSession(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ------------------------------------------------------------------
    // Answer
    // ------------------------------------------------------------------
    /**
     * Lưu (hoặc cập nhật nếu đã có) câu trả lời của một câu hỏi trong phiên.
     *
     * <p>Học sinh có thể bấm nút nhiều lần / double-click, hoặc quay lại làm lại câu đã trả lời.
     * Nếu cứ INSERT thì sẽ sinh nhiều bản ghi cho cùng một câu, làm điểm trung bình bị tính sai
     * (đã phát hiện 17 nhóm bản ghi trùng trong DB). Vì vậy dùng UPDATE nếu đã tồn tại.
     *
     * @return AnswerID của bản ghi vừa lưu, -1 nếu lỗi
     */
    public int saveAnswer(PracticeAnswer a) {
        Integer existing = findAnswerId(a.getSessionId(), a.getQuestionId());
        if (existing != null) {
            updateAnswer(existing, a);
            return existing;
        }
        String sql = "INSERT INTO PracticeAnswers (SessionID, QuestionID, SelectedAnswer, CorrectAnswer, "
                + "IsCorrect, AiFeedback, UserAnswer, ImagePath, AnsweredAt, OrderIndex, Score) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, GETDATE(), ?, ?)";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, a.getSessionId());
            stmt.setInt(2, a.getQuestionId());
            stmt.setString(3, a.getSelectedAnswer());
            stmt.setString(4, a.getCorrectAnswer());
            stmt.setBoolean(5, a.isCorrect());
            stmt.setString(6, a.getAiFeedback());
            stmt.setString(7, a.getUserAnswer());
            stmt.setString(8, a.getImagePath());
            stmt.setInt(9, a.getOrderIndex());
            stmt.setDouble(10, a.getScore());
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
     * Tìm AnswerID của câu đã trả lời trong phiên (null nếu chưa trả lời).
     */
    public Integer findAnswerId(int sessionId, int questionId) {
        String sql = "SELECT TOP 1 AnswerID FROM PracticeAnswers WHERE SessionID = ? AND QuestionID = ? "
                + "ORDER BY AnswerID DESC";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, sessionId);
            stmt.setInt(2, questionId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Cập nhật câu trả lời đã tồn tại (giữ nguyên phần AI đã chấm nếu có).
     */
    private void updateAnswer(int answerId, PracticeAnswer a) {
        String sql = "UPDATE PracticeAnswers SET SelectedAnswer = ?, CorrectAnswer = ?, IsCorrect = ?, "
                + "AiFeedback = COALESCE(?, AiFeedback), UserAnswer = COALESCE(?, UserAnswer), "
                + "ImagePath = COALESCE(?, ImagePath), OrderIndex = ?, Score = ?, AnsweredAt = GETDATE() "
                + "WHERE AnswerID = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, a.getSelectedAnswer());
            stmt.setString(2, a.getCorrectAnswer());
            stmt.setBoolean(3, a.isCorrect());
            stmt.setString(4, a.getAiFeedback());
            stmt.setString(5, a.getUserAnswer());
            stmt.setString(6, a.getImagePath());
            stmt.setInt(7, a.getOrderIndex());
            stmt.setDouble(8, a.getScore());
            stmt.setInt(9, answerId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Cập nhật điểm tự chấm của một câu trả lời.
     */
    public void updateScore(int answerId, double score) {
        String sql = "UPDATE PracticeAnswers SET Score = ? WHERE AnswerID = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setDouble(1, score);
            stmt.setInt(2, answerId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Cập nhật điểm + nhận xét AI sau khi AI chấm xong (chấm nền).
     */
    public void updateGrade(int answerId, double score, boolean correct, String feedback) {
        String sql = "UPDATE PracticeAnswers SET Score = ?, IsCorrect = ?, "
                + "AiFeedback = COALESCE(?, AiFeedback) WHERE AnswerID = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setDouble(1, score);
            stmt.setBoolean(2, correct);
            stmt.setString(3, feedback);
            stmt.setInt(4, answerId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateAiFeedback(int answerId, String feedback) {
        String sql = "UPDATE PracticeAnswers SET AiFeedback = ? WHERE AnswerID = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, feedback);
            stmt.setInt(2, answerId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<PracticeAnswer> listAnswersBySession(int sessionId) {
        List<PracticeAnswer> list = new ArrayList<>();
        String sql = "SELECT pa.AnswerID, pa.SessionID, pa.QuestionID, q.Content AS QuestionContent, "
                + "pa.SelectedAnswer, pa.CorrectAnswer, pa.IsCorrect, pa.AiFeedback, pa.UserAnswer, "
                + "pa.ImagePath, pa.AnsweredAt, pa.OrderIndex, pa.Score, q.Explanation AS QuestionExplanation "
                + "FROM PracticeAnswers pa LEFT JOIN Questions q ON pa.QuestionID = q.QuestionID "
                + "WHERE pa.SessionID = ? ORDER BY pa.OrderIndex, pa.AnswerID";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, sessionId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapAnswer(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<PracticeAnswer> listWrongAnswersBySession(int sessionId) {
        List<PracticeAnswer> list = new ArrayList<>();
        String sql = "SELECT pa.AnswerID, pa.SessionID, pa.QuestionID, q.Content AS QuestionContent, "
                + "pa.SelectedAnswer, pa.CorrectAnswer, pa.IsCorrect, pa.AiFeedback, pa.UserAnswer, "
                + "pa.ImagePath, pa.AnsweredAt, pa.OrderIndex, pa.Score "
                + "FROM PracticeAnswers pa LEFT JOIN Questions q ON pa.QuestionID = q.QuestionID "
                + "WHERE pa.SessionID = ? AND pa.IsCorrect = 0 ORDER BY pa.OrderIndex, pa.AnswerID";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, sessionId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapAnswer(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ------------------------------------------------------------------
    // Thống kê câu hỏi học sinh làm sai nhiều
    // ------------------------------------------------------------------
    /**
     * Thống kê theo câu hỏi: đếm số lượt làm đúng / sai, điểm trung bình.
     *
     * <p>Câu hỏi chỉ được xếp vào thống kê khi học sinh đã thực sự làm
     * (tồn tại bản ghi trong PracticeAnswers).
     *
     * @param topicId  lọc theo chủ đề (0 = tất cả)
     * @param questionType lọc theo loại câu hỏi (null/rỗng = tất cả)
     * @param limit    số câu trả về tối đa (0 = không giới hạn)
     * @param onlyWrong chỉ lấy câu có ít nhất 1 lượt sai
     */
    public List<QuestionStat> listQuestionStats(int topicId, String questionType, int limit,
            boolean onlyWrong) {
        List<QuestionStat> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT q.QuestionID, q.Content, q.QuestionType, q.Difficulty, q.TopicID, ")
                .append("t.Name AS TopicName, ")
                .append("COUNT(pa.AnswerID) AS TotalAttempts, ")
                .append("SUM(CASE WHEN pa.IsCorrect = 0 THEN 1 ELSE 0 END) AS WrongCount, ")
                .append("SUM(CASE WHEN pa.IsCorrect = 1 THEN 1 ELSE 0 END) AS CorrectCount, ")
                .append("AVG(CAST(pa.Score AS FLOAT)) AS AvgScore ")
                .append("FROM PracticeAnswers pa ")
                .append("JOIN Questions q ON pa.QuestionID = q.QuestionID ")
                .append("LEFT JOIN Topics t ON q.TopicID = t.TopicID ")
                .append("WHERE 1 = 1 ");
        if (topicId > 0) {
            sql.append("AND q.TopicID = ? ");
        }
        if (questionType != null && !questionType.trim().isEmpty()) {
            sql.append("AND q.QuestionType = ? ");
        }
        if (onlyWrong) {
            sql.append("AND pa.IsCorrect = 0 ");
        }
        // Sắp xếp: nhiều lượt sai nhất trước, rồi tỉ lệ sai, rồi điểm TB thấp nhất
        sql.append("GROUP BY q.QuestionID, q.Content, q.QuestionType, q.Difficulty, q.TopicID, t.Name ")
                .append("ORDER BY WrongCount DESC, (SUM(CASE WHEN pa.IsCorrect = 0 THEN 1 ELSE 0 END) ")
                .append("* 100.0 / COUNT(pa.AnswerID)) DESC, AvgScore ASC ");
        if (limit > 0) {
            sql.append("OFFSET 0 ROWS FETCH NEXT ").append(limit).append(" ROWS ONLY ");
        }

        try (PreparedStatement stmt = getConnection().prepareStatement(sql.toString())) {
            int idx = 1;
            if (topicId > 0) {
                stmt.setInt(idx++, topicId);
            }
            if (questionType != null && !questionType.trim().isEmpty()) {
                stmt.setString(idx++, questionType.trim());
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapQuestionStat(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Thống kê theo chủ đề: tổng số lượt làm sai và tỉ lệ sai của từng chủ đề.
     */
    public List<TopicStat> listTopicStats() {
        List<TopicStat> list = new ArrayList<>();
        String sql = "SELECT t.TopicID, t.Name AS TopicName, COUNT(pa.AnswerID) AS TotalAttempts, "
                + "SUM(CASE WHEN pa.IsCorrect = 0 THEN 1 ELSE 0 END) AS WrongCount "
                + "FROM PracticeAnswers pa "
                + "JOIN Questions q ON pa.QuestionID = q.QuestionID "
                + "JOIN Topics t ON q.TopicID = t.TopicID "
                + "GROUP BY t.TopicID, t.Name ORDER BY WrongCount DESC";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    TopicStat ts = new TopicStat();
                    ts.setTopicId(rs.getInt("TopicID"));
                    ts.setTopicName(rs.getString("TopicName"));
                    ts.setTotalAttempts(rs.getInt("TotalAttempts"));
                    ts.setWrongCount(rs.getInt("WrongCount"));
                    list.add(ts);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Đếm tổng số lượt làm sai trong toàn hệ thống.
     */
    public int countAllWrong() {
        String sql = "SELECT COUNT(*) FROM PracticeAnswers WHERE IsCorrect = 0";
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

    /**
     * Đếm số câu hỏi có ít nhất 1 lượt sai.
     */
    public int countQuestionsWithWrong() {
        String sql = "SELECT COUNT(DISTINCT QuestionID) FROM PracticeAnswers WHERE IsCorrect = 0";
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

    private QuestionStat mapQuestionStat(ResultSet rs) throws SQLException {
        QuestionStat st = new QuestionStat();
        st.setQuestionId(rs.getInt("QuestionID"));
        st.setContent(rs.getString("Content"));
        st.setQuestionType(rs.getString("QuestionType"));
        st.setDifficulty(rs.getString("Difficulty"));
        st.setTopicId(rs.getInt("TopicID"));
        try {
            st.setTopicName(rs.getString("TopicName"));
        } catch (SQLException ignored) {
        }
        st.setTotalAttempts(rs.getInt("TotalAttempts"));
        st.setWrongCount(rs.getInt("WrongCount"));
        st.setCorrectCount(rs.getInt("CorrectCount"));
        try {
            st.setAvgScore(rs.getDouble("AvgScore"));
        } catch (SQLException ignored) {
        }
        return st;
    }

    // ------------------------------------------------------------------
    // Mapping
    // ------------------------------------------------------------------
    private PracticeSession mapSession(ResultSet rs) throws SQLException {
        PracticeSession s = new PracticeSession();
        s.setSessionId(rs.getInt("SessionID"));
        s.setStudentId(rs.getInt("StudentID"));
        try {
            s.setStudentName(rs.getString("StudentName"));
        } catch (SQLException ignored) {
        }
        s.setSubjectId(rs.getInt("SubjectID"));
        s.setTopicId(rs.getInt("TopicId"));
        try {
            s.setSubjectName(rs.getString("SubjectName"));
        } catch (SQLException ignored) {
        }
        try {
            s.setTopicName(rs.getString("TopicName"));
        } catch (SQLException ignored) {
        }
        s.setPracticeType(rs.getString("PracticeType"));
        s.setTotalQuestions(rs.getInt("TotalQuestions"));
        s.setCorrectCount(rs.getInt("CorrectCount"));
        s.setWrongCount(rs.getInt("WrongCount"));
        s.setScore(rs.getDouble("Score"));
        s.setStatus(rs.getString("Status"));
        try {
            s.setStartedAt(rs.getTimestamp("StartedAt"));
        } catch (SQLException ignored) {
        }
        try {
            s.setFinishedAt(rs.getTimestamp("FinishedAt"));
        } catch (SQLException ignored) {
        }
        return s;
    }

    private PracticeAnswer mapAnswer(ResultSet rs) throws SQLException {
        PracticeAnswer a = new PracticeAnswer();
        a.setAnswerId(rs.getInt("AnswerID"));
        a.setSessionId(rs.getInt("SessionID"));
        a.setQuestionId(rs.getInt("QuestionID"));
        try {
            a.setQuestionContent(rs.getString("QuestionContent"));
        } catch (SQLException ignored) {
        }
        a.setSelectedAnswer(rs.getString("SelectedAnswer"));
        a.setCorrectAnswer(rs.getString("CorrectAnswer"));
        a.setCorrect(rs.getBoolean("IsCorrect"));
        a.setAiFeedback(rs.getString("AiFeedback"));
        a.setUserAnswer(rs.getString("UserAnswer"));
        a.setImagePath(rs.getString("ImagePath"));
        try {
            a.setAnsweredAt(rs.getTimestamp("AnsweredAt"));
        } catch (SQLException ignored) {
        }
        a.setOrderIndex(rs.getInt("OrderIndex"));
        try {
            a.setScore(rs.getDouble("Score"));
        } catch (SQLException ignored) {
        }
        try {
            a.setQuestionExplanation(rs.getString("QuestionExplanation"));
        } catch (SQLException ignored) {
        }
        return a;
    }
}
