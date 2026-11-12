package com.giasuly9.controller;

import com.giasuly9.dao.LessonDAO;
import com.giasuly9.dao.LessonProgressDAO;
import com.giasuly9.dao.PracticeDAO;
import com.giasuly9.dao.QuestionDAO;
import com.giasuly9.dao.TopicDAO;
import com.giasuly9.filter.SessionUtil;
import com.giasuly9.model.Lesson;
import com.giasuly9.model.LessonProgress;
import com.giasuly9.model.PracticeAnswer;
import com.giasuly9.model.PracticeSession;
import com.giasuly9.model.Topic;
import com.giasuly9.model.TopicLearning;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * Các trang của học sinh: dashboard, xem bài giảng, chọn chủ đề, lịch sử luyện tập.
 *
 * <p>Trang này chỉ phục vụ môn Vật lý 9 nên không có bước chọn môn học:
 * học sinh đi thẳng tới chủ đề, bài giảng và luyện tập.
 */
public class StudentController extends HttpServlet {

    private final TopicDAO topicDAO = new TopicDAO();
    private final LessonDAO lessonDAO = new LessonDAO();
    private final QuestionDAO questionDAO = new QuestionDAO();
    private final PracticeDAO practiceDAO = new PracticeDAO();
    private final LessonProgressDAO lessonProgressDAO = new LessonProgressDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getServletPath();
        int userId = SessionUtil.currentUserId(request);

        switch (path) {
            case "/student/dashboard":
                showDashboard(request, response, userId);
                return;
            case "/student/lessons":
                showLessons(request, response, userId);
                return;
            case "/student/learning-progress":
                showLearningProgress(request, response, userId);
                return;
            case "/student/practice-setup":
                showPracticeSetup(request, response);
                return;
            case "/student/history":
                showHistory(request, response, userId);
                return;
            case "/student/result":
                showResult(request, response, userId);
                return;
            default:
                response.sendRedirect(request.getContextPath() + "/student/dashboard");
        }
    }

    /**
     * Cập nhật tiến độ xem video (AJAX). Trả về JSON cho client.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String path = request.getServletPath();

        if ("/student/lesson-progress".equals(path)) {
            updateLessonProgress(request, response);
            return;
        }
        response.sendRedirect(request.getContextPath() + "/student/lessons");
    }

    // ==================================================================
    // Tiến độ xem bài giảng
    // ==================================================================
    private void updateLessonProgress(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");

        int userId = SessionUtil.currentUserId(request);
        int lessonId = PageController.parseInt(request.getParameter("lessonId"), 0);
        int percent = PageController.parseInt(request.getParameter("percent"), 0);

        if (userId <= 0) {
            response.getWriter().write("{\"ok\":false,\"message\":\"Chưa đăng nhập\"}");
            return;
        }
        if (lessonId <= 0) {
            response.getWriter().write("{\"ok\":false,\"message\":\"Bài giảng không hợp lệ\"}");
            return;
        }

        boolean completed = percent >= 90;
        boolean manual = "1".equals(request.getParameter("manual"));

        if (manual) {
            // Học sinh bấm nút "Đánh dấu đã học" — ghi đè được cả xuống 0 khi bỏ đánh dấu
            lessonProgressDAO.setManual(userId, lessonId, percent, completed);
        } else {
            lessonProgressDAO.upsert(userId, lessonId, percent, completed);
        }

        int stored = lessonProgressDAO.findPercent(userId, lessonId);
        response.getWriter().write("{\"ok\":true,\"percent\":" + stored
                + ",\"completed\":" + (stored >= 90 ? "true" : "false") + "}");
    }

    // ==================================================================
    // Các trang
    // ==================================================================
    private void showDashboard(HttpServletRequest request, HttpServletResponse response, int userId)
            throws ServletException, IOException {

        List<PracticeSession> recent = practiceDAO.listSessionsByStudent(userId);

        int totalSessions = recent.size();
        int totalCorrect = 0;
        int totalQuestions = 0;
        double scoreSum = 0;
        int scoredSessions = 0;
        for (PracticeSession s : recent) {
            totalCorrect += s.getCorrectCount();
            totalQuestions += s.getTotalQuestions();
            scoreSum += s.getScore();
            scoredSessions++;
        }
        // Điểm phiên lưu theo thang 10 -> quy đổi ra % cho khớp nhãn hiển thị
        double avgScore = scoredSessions > 0 ? (scoreSum / scoredSessions * 10.0) : 0;

        request.setAttribute("topics", topicDAO.listAll());
        request.setAttribute("recentSessions", recent);
        request.setAttribute("totalSessions", totalSessions);
        request.setAttribute("totalQuestions", totalQuestions);
        request.setAttribute("avgScore", avgScore);
        request.setAttribute("completedLessons", lessonProgressDAO.countCompleted(userId));
        request.setAttribute("activeMenu", "dashboard");
        request.getRequestDispatcher("/views/student/dashboard.jsp").forward(request, response);
    }

    private void showLessons(HttpServletRequest request, HttpServletResponse response, int userId)
            throws ServletException, IOException {

        int topicId = PageController.parseInt(request.getParameter("topicId"), 0);

        List<Topic> topics = topicDAO.listAll();

        List<Lesson> lessons = topicId > 0
                ? lessonDAO.listByTopic(topicId)
                : lessonDAO.listAll();

        Map<Integer, LessonProgress> progress = lessonProgressDAO.mapByUser(userId);

        request.setAttribute("topics", topics);
        request.setAttribute("lessons", lessons);
        request.setAttribute("progressMap", progress);
        request.setAttribute("selectedTopicId", topicId);
        request.setAttribute("activeMenu", "lessons");
        request.getRequestDispatcher("/views/student/lessons.jsp").forward(request, response);
    }

    /**
     * Trang "Tiến độ học": học sinh xem được mình đã học bài nào, đang học bài nào
     * và còn bài nào chưa học — chia theo từng chủ đề.
     */
    private void showLearningProgress(HttpServletRequest request, HttpServletResponse response, int userId)
            throws ServletException, IOException {

        List<Topic> topics = topicDAO.listAll();
        List<Lesson> lessons = lessonDAO.listAll();
        Map<Integer, LessonProgress> progressMap = lessonProgressDAO.mapByUser(userId);

        // Gom bài giảng theo chủ đề
        Map<Integer, List<Lesson>> lessonsByTopic = new java.util.HashMap<>();
        for (Lesson l : lessons) {
            lessonsByTopic.computeIfAbsent(l.getTopicId(), k -> new java.util.ArrayList<>()).add(l);
        }

        List<TopicLearning> topicLearnings = new java.util.ArrayList<>();
        int totalLessons = 0;
        int doneLessons = 0;
        int doingLessons = 0;
        int notStartedLessons = 0;

        for (Topic t : topics) {
            TopicLearning tl = new TopicLearning();
            tl.setTopicId(t.getTopicId());
            tl.setTopicName(t.getName());
            tl.setTopicImage(t.getImageUrl());

            List<Lesson> topicLessons = lessonsByTopic.get(t.getTopicId());
            int total = topicLessons == null ? 0 : topicLessons.size();
            int done = 0;
            int doing = 0;
            int notStarted = 0;

            if (topicLessons != null) {
                for (Lesson l : topicLessons) {
                    LessonProgress lp = progressMap.get(l.getLessonId());
                    if (lp == null || lp.getPercent() <= 0) {
                        notStarted++;
                    } else if (lp.isCompleted() || lp.getPercent() >= 90) {
                        done++;
                    } else {
                        doing++;
                    }
                }
            }

            tl.setTotalLessons(total);
            tl.setDoneLessons(done);
            tl.setInProgressLessons(doing);
            tl.setNotStartedLessons(notStarted);
            topicLearnings.add(tl);

            totalLessons += total;
            doneLessons += done;
            doingLessons += doing;
            notStartedLessons += notStarted;
        }

        request.setAttribute("topics", topics);
        request.setAttribute("lessons", lessons);
        request.setAttribute("progressMap", progressMap);
        request.setAttribute("topicLearnings", topicLearnings);
        request.setAttribute("totalLessons", totalLessons);
        request.setAttribute("doneLessons", doneLessons);
        request.setAttribute("doingLessons", doingLessons);
        request.setAttribute("notStartedLessons", notStartedLessons);
        request.setAttribute("overallPercent", totalLessons > 0
                ? (int) Math.round(doneLessons * 100.0 / totalLessons) : 0);
        request.setAttribute("activeMenu", "learning-progress");
        request.getRequestDispatcher("/views/student/learning-progress.jsp").forward(request, response);
    }

    private void showPracticeSetup(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int topicId = PageController.parseInt(request.getParameter("topicId"), 0);
        String type = request.getParameter("type");
        if (type == null || type.isEmpty()) {
            type = "MCQ";
        }

        List<Topic> topics = topicDAO.listAll();

        Topic topic = topicId > 0 ? topicDAO.findById(topicId) : null;
        int mcqCount = topicId > 0 ? questionDAO.countByTopicAndType(topicId, "MCQ") : 0;
        int essayCount = topicId > 0 ? questionDAO.countByTopicAndType(topicId, "ESSAY") : 0;

        request.setAttribute("topics", topics);
        request.setAttribute("topic", topic);
        request.setAttribute("practiceType", type);
        request.setAttribute("mcqCount", mcqCount);
        request.setAttribute("essayCount", essayCount);
        request.setAttribute("activeMenu", "practice");
        request.getRequestDispatcher("/views/student/practice-setup.jsp").forward(request, response);
    }

    private void showHistory(HttpServletRequest request, HttpServletResponse response, int userId)
            throws ServletException, IOException {
        List<PracticeSession> sessions = practiceDAO.listSessionsByStudent(userId);
        request.setAttribute("sessions", sessions);
        request.setAttribute("activeMenu", "history");
        request.getRequestDispatcher("/views/student/history.jsp").forward(request, response);
    }

    private void showResult(HttpServletRequest request, HttpServletResponse response, int userId)
            throws ServletException, IOException {
        int sessionId = PageController.parseInt(request.getParameter("sessionId"), 0);
        PracticeSession session = practiceDAO.findSessionById(sessionId);
        if (session == null || session.getStudentId() != userId) {
            response.sendRedirect(request.getContextPath() + "/student/history");
            return;
        }

        List<PracticeAnswer> answers = practiceDAO.listAnswersBySession(sessionId);

        // Tính lại điểm từ câu trả lời: các task chấm AI nền có thể vừa xong
        // sau lúc bấm "Nộp bài", nên điểm phiên trong DB có thể còn cũ.
        int correct = 0;
        int wrong = 0;
        double sum = 0;
        int total = session.getTotalQuestions();
        for (PracticeAnswer a : answers) {
            if (a.isCorrect()) {
                correct++;
            } else {
                wrong++;
            }
            sum += a.getScore();
        }
        int answered = correct + wrong;
        if (answered < total) {
            wrong += (total - answered);
        }
        if (total <= 0) {
            total = answered;
        }
        if (total > 0) {
            double avg = sum / total;
            if (Math.abs(avg - session.getScore()) > 0.01) {
                session.setScore(avg);
                practiceDAO.finishSession(sessionId, correct, wrong, avg);
            }
        }
        session.setCorrectCount(correct);
        session.setWrongCount(wrong);

        request.setAttribute("session", session);
        request.setAttribute("answers", answers);
        request.setAttribute("activeMenu", "history");
        request.getRequestDispatcher("/views/student/result.jsp").forward(request, response);
    }
}
