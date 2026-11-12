package com.giasuly9.controller;

import com.giasuly9.dao.PracticeDAO;
import com.giasuly9.dao.QuestionDAO;
import com.giasuly9.dao.SubjectDAO;
import com.giasuly9.dao.TopicDAO;
import com.giasuly9.filter.SessionUtil;
import com.giasuly9.model.PracticeAnswer;
import com.giasuly9.model.PracticeSession;
import com.giasuly9.model.Question;
import com.giasuly9.model.Subject;
import com.giasuly9.model.Topic;
import com.giasuly9.util.AiService;
import com.giasuly9.util.AutoScoreService;
import com.giasuly9.util.FileUploadUtil;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Luồng luyện tập của học sinh:
 * - MCQ: chọn đáp án, tự chấm ngay, AI nhận xét câu sai
 * - ESSAY: viết bài / upload ảnh, AI chấm điểm + nhận xét
 * - Khi làm xong: hệ thống tự chấm tổng điểm, xếp loại và hiển thị chi tiết.
 *
 * <p>Ba điểm đã sửa so với bản cũ:
 * <ol>
 *   <li><b>Không còn nhảy về câu trước</b>: form gửi kèm {@code qIndex} của câu đang hiển thị.
 *       Server dùng đúng index đó thay vì lấy {@code practiceIndex} trong session
 *       (nếu học sinh bấm nút 2 lần, request cũ có thể tới sau và ghi đè index).</li>
 *   <li><b>Chấm điểm AI chạy nền</b>: học sinh chuyển câu ngay, không phải chờ AI
 *       (AI có thể mất 2-5 giây, thậm chí 429). Điểm + nhận xét được ghi vào DB sau.</li>
 *   <li><b>Trả lời trùng không còn làm sai điểm</b>: {@code PracticeDAO.saveAnswer} giờ
 *       upsert theo (SessionID, QuestionID).</li>
 * </ol>
 */
public class PracticeController extends HttpServlet {

    private final QuestionDAO questionDAO = new QuestionDAO();
    private final PracticeDAO practiceDAO = new PracticeDAO();
    private final SubjectDAO subjectDAO = new SubjectDAO();
    private final TopicDAO topicDAO = new TopicDAO();

    private static final int DEFAULT_QUESTION_COUNT = 10;

    /**
     * Các task chấm điểm AI đang chạy nền, key = answerId.
     * Dùng để tránh chấm trùng và để trang kết quả biết câu nào đang chờ.
     */
    private static final Map<Integer, Boolean> PENDING_GRADES = new ConcurrentHashMap<>();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();

        if ("/practice/start".equals(path)) {
            startPractice(request, response);
            return;
        }
        if ("/practice/question".equals(path)) {
            showQuestion(request, response);
            return;
        }
        if ("/practice/navigate".equals(path)) {
            navigate(request, response);
            return;
        }
        if ("/practice/finish".equals(path)) {
            // Đến hết câu hỏi -> chấm điểm tự động rồi chuyển sang trang kết quả
            finish(request, response);
            return;
        }
        response.sendRedirect(request.getContextPath() + "/student/practice-setup");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String path = request.getServletPath();

        if ("/practice/start".equals(path)) {
            startPractice(request, response);
            return;
        }
        if ("/practice/answer-mcq".equals(path)) {
            answerMcq(request, response);
            return;
        }
        if ("/practice/answer-essay".equals(path)) {
            answerEssay(request, response);
            return;
        }
        if ("/practice/navigate".equals(path)) {
            navigate(request, response);
            return;
        }
        if ("/practice/finish".equals(path)) {
            finish(request, response);
            return;
        }
        response.sendRedirect(request.getContextPath() + "/student/practice-setup");
    }

    // ==================================================================
    // Bắt đầu luyện tập
    // ==================================================================
    private void startPractice(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int userId = SessionUtil.currentUserId(request);
        int topicId = PageController.parseInt(request.getParameter("topicId"), 0);
        String type = request.getParameter("type");
        if (type == null || (!type.equalsIgnoreCase("MCQ") && !type.equalsIgnoreCase("ESSAY"))) {
            type = "MCQ";
        }
        int count = PageController.parseInt(request.getParameter("count"), DEFAULT_QUESTION_COUNT);
        if (count <= 0 || count > 50) {
            count = DEFAULT_QUESTION_COUNT;
        }

        Topic topic = topicDAO.findById(topicId);
        if (topic == null) {
            request.setAttribute("error", "Vui lòng chọn một chủ đề hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/student/practice-setup");
            return;
        }

        // Lấy câu hỏi từ ngân hàng; nếu không đủ thì đề nghị giáo viên/AI bổ sung
        List<Question> questions = questionDAO.listByTopicAndType(topicId, type.toUpperCase(), count);
        if (questions.isEmpty()) {
            request.getSession().setAttribute("flashError",
                    "Chủ đề \"" + topic.getName() + "\" chưa có câu hỏi "
                            + (type.equalsIgnoreCase("MCQ") ? "trắc nghiệm" : "tự luận")
                            + ". Hãy nhờ giáo viên thêm hoặc dùng AI tạo câu hỏi.");
            response.sendRedirect(request.getContextPath() + "/student/practice-setup?topicId=" + topicId);
            return;
        }

        PracticeSession session = new PracticeSession();
        session.setStudentId(userId);
        session.setSubjectId(topic.getSubjectId());
        session.setTopicId(topicId);
        session.setPracticeType(type.toUpperCase());
        session.setTotalQuestions(questions.size());

        int sessionId = practiceDAO.createSession(session);
        if (sessionId <= 0) {
            request.getSession().setAttribute("flashError", "Không tạo được phiên luyện tập.");
            response.sendRedirect(request.getContextPath() + "/student/practice-setup?topicId=" + topicId);
            return;
        }

        // Lưu câu hỏi vào session để render tuần tự
        request.getSession().setAttribute("practiceSessionId", sessionId);
        request.getSession().setAttribute("practiceQuestions", questions);
        request.getSession().setAttribute("practiceIndex", 0);
        request.getSession().setAttribute("practiceCorrect", 0);
        request.getSession().setAttribute("practiceWrong", 0);
        request.getSession().setAttribute("practiceTypeAttr", type.toUpperCase());
        // Lưu câu đã trả lời để vẽ trạng thái trên thanh điều hướng
        request.getSession().setAttribute("practiceAnswered", new java.util.HashSet<Integer>());

        response.sendRedirect(request.getContextPath() + "/practice/question");
    }

    // ==================================================================
    // Hiển thị câu hỏi hiện tại
    // ==================================================================
    @SuppressWarnings("unchecked")
    private void showQuestion(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        jakarta.servlet.http.HttpSession sess = request.getSession();
        List<Question> questions = (List<Question>) sess.getAttribute("practiceQuestions");
        Integer indexObj = (Integer) sess.getAttribute("practiceIndex");
        Integer sessionIdObj = (Integer) sess.getAttribute("practiceSessionId");

        if (questions == null || indexObj == null || sessionIdObj == null) {
            response.sendRedirect(request.getContextPath() + "/student/practice-setup");
            return;
        }

        int index = indexObj;
        if (index >= questions.size()) {
            response.sendRedirect(request.getContextPath() + "/practice/finish");
            return;
        }

        Question q = questions.get(index);
        request.setAttribute("question", q);
        request.setAttribute("index", index);
        request.setAttribute("total", questions.size());
        request.setAttribute("sessionId", sessionIdObj);
        request.setAttribute("practiceType", sess.getAttribute("practiceTypeAttr"));
        request.setAttribute("answeredSet", answeredSet(sess));
        request.setAttribute("activeMenu", "practice");

        request.getRequestDispatcher("/views/student/practice-question.jsp").forward(request, response);
    }

    // ==================================================================
    // Điều hướng: câu trước / câu tiếp theo
    // ==================================================================
    /**
     * Chuyển tới câu trước hoặc câu tiếp theo mà KHÔNG ghi đè đáp án đã làm.
     *
     * <p>Câu đã trả lời vẫn hiển thị đáp án đã chọn (đọc từ DB) để học sinh
     * soát lại được, nhưng không được sửa điểm.
     */
    @SuppressWarnings("unchecked")
    private void navigate(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        jakarta.servlet.http.HttpSession sess = request.getSession();
        List<Question> questions = (List<Question>) sess.getAttribute("practiceQuestions");
        Integer sessionIdObj = (Integer) sess.getAttribute("practiceSessionId");

        if (questions == null || sessionIdObj == null) {
            response.sendRedirect(request.getContextPath() + "/student/practice-setup");
            return;
        }

        int total = questions.size();
        int current = PageController.parseInt(request.getParameter("index"), -1);
        if (current < 0 || current >= total) {
            current = (Integer) sess.getAttribute("practiceIndex") != null
                    ? (Integer) sess.getAttribute("practiceIndex") : 0;
        }

        String dir = request.getParameter("dir");
        int target = current;
        if ("prev".equalsIgnoreCase(dir)) {
            target = current - 1;
        } else if ("next".equalsIgnoreCase(dir)) {
            target = current + 1;
        } else {
            // Nhảy tới câu cụ thể (bấm vào số thứ tự trên thanh điều hướng)
            target = PageController.parseInt(request.getParameter("to"), current);
        }

        if (target < 0) {
            target = 0;
        }
        if (target >= total) {
            // Sang quá câu cuối -> nộp bài và xem điểm
            response.sendRedirect(request.getContextPath() + "/practice/finish");
            return;
        }

        sess.setAttribute("practiceIndex", target);

        // Nếu câu đã trả lời, lấy lại đáp án để hiển thị
        Question q = questions.get(target);
        PracticeAnswer saved = findSavedAnswer(sessionIdObj, q.getQuestionId());
        if (saved != null) {
            request.setAttribute("savedAnswer", saved);
        }
        request.setAttribute("question", q);
        request.setAttribute("index", target);
        request.setAttribute("total", total);
        request.setAttribute("sessionId", sessionIdObj);
        request.setAttribute("practiceType", sess.getAttribute("practiceTypeAttr"));
        request.setAttribute("answeredSet", answeredSet(sess));
        request.setAttribute("activeMenu", "practice");
        request.getRequestDispatcher("/views/student/practice-question.jsp").forward(request, response);
    }

    private java.util.Set<Integer> answeredSet(jakarta.servlet.http.HttpSession sess) {
        Object o = sess.getAttribute("practiceAnswered");
        if (o instanceof java.util.Set) {
            @SuppressWarnings("unchecked")
            java.util.Set<Integer> s = (java.util.Set<Integer>) o;
            return s;
        }
        return new java.util.HashSet<>();
    }

    private PracticeAnswer findSavedAnswer(int sessionId, int questionId) {
        List<PracticeAnswer> list = practiceDAO.listAnswersBySession(sessionId);
        for (PracticeAnswer a : list) {
            if (a.getQuestionId() == questionId) {
                return a;
            }
        }
        return null;
    }

    // ==================================================================
    // Trả lời câu trắc nghiệm - TỰ CHẤM ĐIỂM NGAY
    // ==================================================================
    @SuppressWarnings("unchecked")
    private void answerMcq(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        jakarta.servlet.http.HttpSession sess = request.getSession();
        List<Question> questions = (List<Question>) sess.getAttribute("practiceQuestions");
        Integer sessionIdObj = (Integer) sess.getAttribute("practiceSessionId");
        int userId = SessionUtil.currentUserId(request);

        if (questions == null || sessionIdObj == null) {
            response.sendRedirect(request.getContextPath() + "/student/practice-setup");
            return;
        }

        // Index của câu đang hiển thị (do form gửi lên) - tránh nhảy về câu trước
        int index = PageController.parseInt(request.getParameter("qIndex"), -1);
        if (index < 0 || index >= questions.size()) {
            Integer idx = (Integer) sess.getAttribute("practiceIndex");
            index = (idx == null) ? 0 : idx;
        }

        Question q = questions.get(index);
        String selected = request.getParameter("answer");
        if (selected == null) {
            selected = "";
        }
        selected = selected.trim().toUpperCase();

        // ===== TỰ CHẤM ĐIỂM: đúng 10đ, sai 0đ =====
        boolean correct = selected.equalsIgnoreCase(q.getCorrectAnswer());
        double score = AutoScoreService.scoreMcq(selected, q.getCorrectAnswer());

        PracticeAnswer pa = new PracticeAnswer();
        pa.setSessionId(sessionIdObj);
        pa.setQuestionId(q.getQuestionId());
        pa.setSelectedAnswer(selected);
        pa.setCorrectAnswer(q.getCorrectAnswer());
        pa.setCorrect(correct);
        pa.setOrderIndex(index);
        pa.setScore(score);
        int answerId = practiceDAO.saveAnswer(pa);

        // Đếm đúng/sai theo câu đã trả lời (mỗi câu chỉ tính 1 lần)
        recountCounts(sess, sessionIdObj);

        markAnswered(sess, index);

        // ===== Nhận xét AI cho câu sai: chạy NỀN, không chặn người học =====
        if (!correct && answerId > 0) {
            gradeInBackground(answerId, q, selected, userId);
        }

        // Chuyển sang câu tiếp theo (không hiện đáp án/điểm -> xem kết quả khi làm xong)
        sess.setAttribute("practiceIndex", index + 1);
        response.sendRedirect(request.getContextPath() + "/practice/question");
    }

    // ==================================================================
    // Trả lời câu tự luận - AI CHẤM ĐIỂM (có thể upload ảnh)
    // ==================================================================
    @SuppressWarnings("unchecked")
    private void answerEssay(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        jakarta.servlet.http.HttpSession sess = request.getSession();
        List<Question> questions = (List<Question>) sess.getAttribute("practiceQuestions");
        Integer sessionIdObj = (Integer) sess.getAttribute("practiceSessionId");
        int userId = SessionUtil.currentUserId(request);

        if (questions == null || sessionIdObj == null) {
            response.sendRedirect(request.getContextPath() + "/student/practice-setup");
            return;
        }

        int index = PageController.parseInt(request.getParameter("qIndex"), -1);
        if (index < 0 || index >= questions.size()) {
            Integer idx = (Integer) sess.getAttribute("practiceIndex");
            index = (idx == null) ? 0 : idx;
        }

        Question q = questions.get(index);
        String studentAnswer = request.getParameter("studentAnswer");
        if (studentAnswer == null) {
            studentAnswer = "";
        }
        studentAnswer = studentAnswer.trim();

        // Upload ảnh bài làm (nếu có) - đọc và chuẩn hóa 1 lần để AI đọc ổn định
        String imagePath = null;
        byte[] imageBytes = null;
        String imageMime = null;
        FileUploadUtil.ImageData image = FileUploadUtil.readImage(request, "answerImage");
        if (image != null) {
            imageBytes = image.getBytes();
            imageMime = image.getMime();
            imagePath = FileUploadUtil.saveImageBytes(imageBytes, imageMime,
                    FileUploadUtil.getUploadBasePath(request), "answers");
        }

        if (studentAnswer.isEmpty() && imagePath == null) {
            request.setAttribute("error", "Bạn cần viết câu trả lời hoặc upload ảnh bài làm.");
            request.setAttribute("question", q);
            request.setAttribute("index", index);
            request.setAttribute("total", questions.size());
            request.setAttribute("sessionId", sessionIdObj);
            request.setAttribute("practiceType", sess.getAttribute("practiceTypeAttr"));
            request.setAttribute("answeredSet", answeredSet(sess));
            request.setAttribute("activeMenu", "practice");
            request.getRequestDispatcher("/views/student/practice-question.jsp").forward(request, response);
            return;
        }

        // Lưu bài ngay với điểm tạm = 0, AI sẽ chấm nền rồi cập nhật sau
        PracticeAnswer pa = new PracticeAnswer();
        pa.setSessionId(sessionIdObj);
        pa.setQuestionId(q.getQuestionId());
        pa.setSelectedAnswer(null);
        pa.setUserAnswer(studentAnswer);
        pa.setImagePath(imagePath);
        pa.setCorrect(false);
        pa.setOrderIndex(index);
        pa.setScore(0);
        int answerId = practiceDAO.saveAnswer(pa);

        markAnswered(sess, index);

        // ===== AI CHẤM ĐIỂM: chạy NỀN =====
        if (answerId > 0) {
            gradeEssayInBackground(answerId, q, studentAnswer, imageBytes, imageMime, userId);
        }

        sess.setAttribute("practiceIndex", index + 1);
        response.sendRedirect(request.getContextPath() + "/practice/question");
    }

    /**
     * Chấm điểm + nhận xét AI cho câu trắc nghiệm sai, chạy trên luồng nền.
     * Học sinh chuyển câu ngay, không phải chờ AI (AI có thể mất vài giây hoặc bị 429).
     */
    private void gradeInBackground(int answerId, Question q, String selected, int userId) {
        if (PENDING_GRADES.putIfAbsent(answerId, Boolean.TRUE) != null) {
            return;
        }
        Thread t = new Thread(() -> {
            PracticeDAO dao = new PracticeDAO();
            try {
                String feedback;
                try {
                    AiService ai = new AiService();
                    feedback = ai.explainWrongMcq(q, selected, userId);
                } catch (Exception e) {
                    e.printStackTrace();
                    feedback = "Đáp án đúng là " + q.getCorrectAnswer() + ". "
                            + (q.getExplanation() == null ? "" : q.getExplanation());
                }
                // Chỉ cập nhật nhận xét, giữ nguyên điểm đã tự chấm
                dao.updateGrade(answerId, 0, false, feedback);
            } finally {
                PENDING_GRADES.remove(answerId);
                dao.close();
            }
        }, "practice-grade-" + answerId);
        t.setDaemon(true);
        t.start();
    }

    /**
     * Chấm điểm bài tự luận bằng AI, chạy trên luồng nền.
     */
    private void gradeEssayInBackground(int answerId, Question q, String studentAnswer,
            byte[] imageBytes, String imageMime, int userId) {
        if (PENDING_GRADES.putIfAbsent(answerId, Boolean.TRUE) != null) {
            return;
        }
        Thread t = new Thread(() -> {
            PracticeDAO dao = new PracticeDAO();
            try {
                String feedback;
                double score = 0;
                try {
                    AiService ai = new AiService();
                    if (imageBytes != null) {
                        // Bài gửi kèm ảnh: AI đánh giá mức độ liên quan của ảnh.
                        // Ảnh không liên quan -> chắc chắn 0 điểm.
                        AiService.EssayGrade grade = ai.gradeEssayWithImage(q, studentAnswer,
                                imageBytes, imageMime, userId);
                        score = grade.getScore();
                        feedback = grade.toDisplayText();
                    } else {
                        AiService.EssayGrade grade = ai.gradeEssay(q, studentAnswer, userId);
                        score = grade.getScore();
                        feedback = grade.toDisplayText();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    feedback = "AI chưa phản hồi được. Bài của bạn đã được ghi nhận.";
                    score = 0;
                }
                if (score < 0) {
                    score = 0;
                }
                if (score > 10) {
                    score = 10;
                }
                // Đạt 5 điểm mới được tính là "đúng" trong thống kê.
                dao.updateGrade(answerId, score, score >= 5.0, feedback);
            } finally {
                PENDING_GRADES.remove(answerId);
                dao.close();
            }
        }, "practice-essay-" + answerId);
        t.setDaemon(true);
        t.start();
    }

    /**
     * Đánh dấu câu đã trả lời (để thanh điều hướng hiển thị trạng thái).
     */
    private void markAnswered(jakarta.servlet.http.HttpSession sess, int index) {
        Object o = sess.getAttribute("practiceAnswered");
        java.util.Set<Integer> set;
        if (o instanceof java.util.Set) {
            @SuppressWarnings("unchecked")
            java.util.Set<Integer> s = (java.util.Set<Integer>) o;
            set = s;
        } else {
            set = new java.util.HashSet<>();
            sess.setAttribute("practiceAnswered", set);
        }
        set.add(index);
    }

    /**
     * Tính lại số câu đúng/sai từ các câu đã lưu trong DB (chống đếm trùng khi
     * học sinh bấm nút nhiều lần hoặc quay lại làm lại câu đã trả lời).
     */
    private void recountCounts(jakarta.servlet.http.HttpSession sess, int sessionId) {
        List<PracticeAnswer> list = practiceDAO.listAnswersBySession(sessionId);
        int correct = 0;
        int wrong = 0;
        for (PracticeAnswer a : list) {
            if (a.isCorrect()) {
                correct++;
            } else {
                wrong++;
            }
        }
        sess.setAttribute("practiceCorrect", correct);
        sess.setAttribute("practiceWrong", wrong);
    }

    // ==================================================================
    // Kết thúc - TỰ CHẤM TỔNG ĐIỂM
    // ==================================================================
    private void finish(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        jakarta.servlet.http.HttpSession sess = request.getSession();
        Integer sessionIdObj = (Integer) sess.getAttribute("practiceSessionId");
        List<Question> questions = (List<Question>) sess.getAttribute("practiceQuestions");

        if (sessionIdObj == null) {
            response.sendRedirect(request.getContextPath() + "/student/practice-setup");
            return;
        }

        int total = (questions == null) ? 0 : questions.size();

        // Đếm lại từ DB cho chính xác (không phụ thuộc bộ đếm trong session)
        List<PracticeAnswer> answers = practiceDAO.listAnswersBySession(sessionIdObj);
        int correct = 0;
        int wrong = 0;
        for (PracticeAnswer a : answers) {
            if (a.isCorrect()) {
                correct++;
            } else {
                wrong++;
            }
        }
        // Câu bỏ trống (không nộp) vẫn tính là sai
        int answered = correct + wrong;
        if (answered < total) {
            wrong += (total - answered);
        }

        // ===== TỰ CHẤM: điểm trung bình thang 10 từ các câu đã lưu =====
        // Chờ tối đa 12 giây cho các task chấm AI đang chạy nền hoàn tất,
        // để trang kết quả hiển thị đầy đủ điểm + nhận xét ngay.
        waitForPendingGrades(12000);

        double avgScore10 = practiceDAO.averageScore(sessionIdObj);
        if (avgScore10 < 0) {
            // Dự phòng: tính từ số câu đúng
            avgScore10 = total > 0 ? (correct * 10.0 / total) : 0;
        }
        // Lưu điểm theo thang 10 trong cột Score
        practiceDAO.finishSession(sessionIdObj, correct, wrong, avgScore10);
        sess.removeAttribute("practiceQuestions");
        sess.removeAttribute("practiceIndex");
        sess.removeAttribute("practiceCorrect");
        sess.removeAttribute("practiceWrong");
        sess.removeAttribute("practiceTypeAttr");
        sess.removeAttribute("practiceAnswered");
        sess.removeAttribute("practiceFlash");
        response.sendRedirect(request.getContextPath() + "/student/result?sessionId=" + sessionIdObj);
    }

    /**
     * Chờ các task chấm AI nền hoàn tất (tối đa maxMillis).
     * Nếu hết thời gian thì bỏ qua - điểm vẫn được chấm theo phần đã có.
     */
    private void waitForPendingGrades(long maxMillis) {
        long deadline = System.currentTimeMillis() + maxMillis;
        while (!PENDING_GRADES.isEmpty() && System.currentTimeMillis() < deadline) {
            try {
                Thread.sleep(120);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }
}
