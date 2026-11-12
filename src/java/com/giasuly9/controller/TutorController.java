package com.giasuly9.controller;

import com.giasuly9.dao.QuestionDAO;
import com.giasuly9.dao.TutorDAO;
import com.giasuly9.filter.SessionUtil;
import com.giasuly9.model.Question;
import com.giasuly9.model.TutorStep;
import com.giasuly9.util.AiService;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Chế độ "AI giải từng bước" (Tutor).
 *
 * Luồng:
 *  1. Học sinh chọn 1 câu hỏi tự luận -> bấm "Học từng bước với AI"
 *  2. AI đưa ra BƯỚC 1, nếu cần học sinh tự tính thì AI DỪNG LẠI và hỏi
 *  3. Học sinh nhập số liệu -> gửi -> AI tiếp tục bước tiếp theo
 *  4. Lặp lại cho tới khi AI kết luận đáp án
 */
public class TutorController extends HttpServlet {

    private final QuestionDAO questionDAO = new QuestionDAO();
    private final TutorDAO tutorDAO = new TutorDAO();

    private static final String SESSION_QUESTION = "tutorQuestion";
    private static final String SESSION_HISTORY = "tutorHistory";
    private static final String SESSION_SESSION_ID = "tutorPracticeSessionId";
    private static final String SESSION_MODE = "tutorMode";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();

        if ("/tutor/open".equals(path)) {
            openTutor(request, response);
            return;
        }
        if ("/tutor/chat".equals(path)) {
            showChat(request, response);
            return;
        }
        response.sendRedirect(request.getContextPath() + "/student/practice-setup");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String path = request.getServletPath();

        if ("/tutor/start".equals(path)) {
            startTutor(request, response);
            return;
        }
        if ("/tutor/next".equals(path)) {
            nextStep(request, response);
            return;
        }
        if ("/tutor/chat".equals(path)) {
            showChat(request, response);
            return;
        }
        response.sendRedirect(request.getContextPath() + "/student/practice-setup");
    }

    // ==================================================================
    // Mở trang tutor cho 1 câu hỏi
    // ==================================================================
    private void openTutor(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int questionId = PageController.parseInt(request.getParameter("questionId"), 0);
        Question q = questionDAO.findById(questionId);
        if (q == null) {
            response.sendRedirect(request.getContextPath() + "/student/practice-setup");
            return;
        }

        HttpSession sess = request.getSession();
        sess.setAttribute(SESSION_QUESTION, q);
        sess.setAttribute(SESSION_HISTORY, new ArrayList<Map<String, String>>());
        sess.setAttribute(SESSION_MODE, "idle");

        response.sendRedirect(request.getContextPath() + "/tutor/chat");
    }

    // ==================================================================
    // Bắt đầu quy trình giải từng bước
    // ==================================================================
    @SuppressWarnings("unchecked")
    private void startTutor(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession sess = request.getSession();
        Question q = (Question) sess.getAttribute(SESSION_QUESTION);
        if (q == null) {
            response.sendRedirect(request.getContextPath() + "/student/practice-setup");
            return;
        }

        int userId = SessionUtil.currentUserId(request);
        List<Map<String, String>> history = (List<Map<String, String>>) sess.getAttribute(SESSION_HISTORY);
        if (history == null) {
            history = new ArrayList<>();
        }

        AiService ai = new AiService();
        if (!ai.isConfigured()) {
            request.setAttribute("error", "AI chưa được cấu hình. Vui lòng liên hệ quản trị viên.");
            showChat(request, response);
            return;
        }

        AiService.TutorTurn turn = ai.startTutor(q, history, userId);

        // Lưu bước AI vào DB
        saveStep(sess, q, "AI", turn.getStep(), turn.isNeedUserData() ? "NEED_DATA" : "EXPLANATION",
                turn.isNeedUserData());

        // Thêm vào lịch sử hội thoại
        history.add(msg("assistant", turn.getStep()));
        if (turn.getAskQuestion() != null && !turn.getAskQuestion().trim().isEmpty()) {
            history.add(msg("user", "[Câu hỏi của AI: " + turn.getAskQuestion() + "]"));
        }
        sess.setAttribute(SESSION_HISTORY, history);
        sess.setAttribute(SESSION_MODE, turn.isNeedUserData() ? "waiting_user" : "ai_turn");

        request.setAttribute("turn", turn);
        showChat(request, response);
    }

    // ==================================================================
    // Học sinh nhập dữ liệu -> AI giải tiếp
    // ==================================================================
    @SuppressWarnings("unchecked")
    private void nextStep(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession sess = request.getSession();
        Question q = (Question) sess.getAttribute(SESSION_QUESTION);
        if (q == null) {
            response.sendRedirect(request.getContextPath() + "/student/practice-setup");
            return;
        }

        String userInput = request.getParameter("userInput");
        if (userInput == null) {
            userInput = "";
        }
        userInput = userInput.trim();

        int userId = SessionUtil.currentUserId(request);
        List<Map<String, String>> history = (List<Map<String, String>>) sess.getAttribute(SESSION_HISTORY);
        if (history == null) {
            history = new ArrayList<>();
        }

        if (!userInput.isEmpty()) {
            // Lưu bước của học sinh
            saveStep(sess, q, "USER", userInput, "ANSWER", false);
            history.add(msg("user", userInput));
            sess.setAttribute(SESSION_HISTORY, history);
        }

        AiService ai = new AiService();
        AiService.TutorTurn turn = ai.continueTutor(q, history, userId);

        saveStep(sess, q, "AI", turn.getStep(), turn.isNeedUserData() ? "NEED_DATA" : "EXPLANATION",
                turn.isNeedUserData());
        if (turn.isFinal()) {
            saveStep(sess, q, "AI", "Đáp án: " + turn.getFinalAnswer(), "FINAL", false);
        }

        history.add(msg("assistant", turn.getStep()));
        sess.setAttribute(SESSION_HISTORY, history);
        sess.setAttribute(SESSION_MODE, turn.isNeedUserData() ? "waiting_user"
                : (turn.isFinal() ? "finished" : "ai_turn"));

        request.setAttribute("turn", turn);
        showChat(request, response);
    }

    // ==================================================================
    // Render trang chat tutor
    // ==================================================================
    @SuppressWarnings("unchecked")
    private void showChat(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession sess = request.getSession();
        Question q = (Question) sess.getAttribute(SESSION_QUESTION);
        if (q == null) {
            response.sendRedirect(request.getContextPath() + "/student/practice-setup");
            return;
        }

        List<Map<String, String>> history = (List<Map<String, String>>) sess.getAttribute(SESSION_HISTORY);
        if (history == null) {
            history = new ArrayList<>();
        }

        // Lấy các bước đã lưu trong DB
        Integer practiceSessionId = (Integer) sess.getAttribute(SESSION_SESSION_ID);
        List<TutorStep> steps = new ArrayList<>();
        if (practiceSessionId != null) {
            steps = tutorDAO.listBySession(practiceSessionId);
        }

        request.setAttribute("question", q);
        request.setAttribute("history", history);
        request.setAttribute("steps", steps);
        request.setAttribute("mode", sess.getAttribute(SESSION_MODE));
        request.setAttribute("activeMenu", "practice");
        request.getRequestDispatcher("/views/student/tutor-chat.jsp").forward(request, response);
    }

    // ==================================================================
    // Helper
    // ==================================================================
    private void saveStep(HttpSession sess, Question q, String speaker, String message,
            String stepType, boolean awaitingUser) {
        Integer sessionId = (Integer) sess.getAttribute(SESSION_SESSION_ID);
        if (sessionId == null) {
            sessionId = 0;
        }
        TutorStep step = new TutorStep();
        step.setSessionId(sessionId);
        step.setQuestionId(q.getQuestionId());
        step.setStepOrder(tutorDAO.nextStepOrder(sessionId));
        step.setSpeaker(speaker);
        step.setMessage(message);
        step.setStepType(stepType);
        step.setAwaitingUser(awaitingUser);
        tutorDAO.addStep(step);
    }

    private Map<String, String> msg(String role, String content) {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("role", role);
        m.put("content", content);
        return m;
    }
}
