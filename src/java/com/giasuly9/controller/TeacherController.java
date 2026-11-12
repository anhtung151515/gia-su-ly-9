package com.giasuly9.controller;

import com.giasuly9.dao.LessonDAO;
import com.giasuly9.dao.PracticeDAO;
import com.giasuly9.dao.QuestionDAO;
import com.giasuly9.dao.SubjectDAO;
import com.giasuly9.dao.TopicDAO;
import com.giasuly9.filter.SessionUtil;
import com.giasuly9.model.Lesson;
import com.giasuly9.model.Question;
import com.giasuly9.model.Subject;
import com.giasuly9.model.Topic;
import com.giasuly9.util.AiService;
import com.giasuly9.util.FileUploadUtil;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.List;

/**
 * Khu vực giáo viên: quản lý bài giảng, câu hỏi, chủ đề, sinh câu hỏi bằng AI.
 */
public class TeacherController extends HttpServlet {

    private final SubjectDAO subjectDAO = new SubjectDAO();
    private final TopicDAO topicDAO = new TopicDAO();
    private final LessonDAO lessonDAO = new LessonDAO();
    private final QuestionDAO questionDAO = new QuestionDAO();
    private final PracticeDAO practiceDAO = new PracticeDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();
        int userId = SessionUtil.currentUserId(request);

        switch (path) {
            case "/teacher/dashboard":
                showDashboard(request, response, userId);
                return;
            case "/teacher/lessons":
                showLessons(request, response, userId);
                return;
            case "/teacher/lesson-form":
                showLessonForm(request, response, userId);
                return;
            case "/teacher/questions":
                showQuestions(request, response, userId);
                return;
            case "/teacher/question-form":
                showQuestionForm(request, response, userId);
                return;
            case "/teacher/topics":
                showTopics(request, response);
                return;
            case "/teacher/topic-form":
                showTopicForm(request, response);
                return;
            case "/teacher/ai-generator":
                showAiGenerator(request, response);
                return;
            default:
                response.sendRedirect(request.getContextPath() + "/teacher/dashboard");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String path = request.getServletPath();
        int userId = SessionUtil.currentUserId(request);

        switch (path) {
            case "/teacher/lesson-save":
                saveLesson(request, response, userId);
                return;
            case "/teacher/lesson-delete":
                deleteLesson(request, response, userId);
                return;
            case "/teacher/question-save":
                saveQuestion(request, response, userId);
                return;
            case "/teacher/question-delete":
                deleteQuestion(request, response, userId);
                return;
            case "/teacher/topic-save":
                saveTopic(request, response);
                return;
            case "/teacher/topic-delete":
                deleteTopic(request, response);
                return;
            case "/teacher/ai-generate":
                generateWithAi(request, response, userId);
                return;
            case "/teacher/ai-question-delete":
                deleteAiGeneratedQuestion(request, response, userId);
                return;
            case "/teacher/ai-questions-delete-all":
                deleteAllAiGeneratedQuestions(request, response, userId);
                return;
            default:
                response.sendRedirect(request.getContextPath() + "/teacher/dashboard");
        }
    }

    // ==================================================================
    // Dashboard
    // ==================================================================
    private void showDashboard(HttpServletRequest request, HttpServletResponse response, int userId)
            throws ServletException, IOException {
        List<Subject> subjects = subjectDAO.listAll();
        List<Topic> topics = topicDAO.listAll();
        List<Lesson> myLessons = lessonDAO.listByCreator(userId);
        List<Question> myQuestions = questionDAO.listByCreator(userId);

        request.setAttribute("subjects", subjects);
        request.setAttribute("topics", topics);
        request.setAttribute("myLessons", myLessons);
        request.setAttribute("myQuestions", myQuestions);
        request.setAttribute("totalLessons", myLessons.size());
        request.setAttribute("totalQuestions", myQuestions.size());
        request.setAttribute("totalWrong", practiceDAO.countAllWrong());
        request.setAttribute("activeMenu", "dashboard");
        request.getRequestDispatcher("/views/teacher/dashboard.jsp").forward(request, response);
    }

    // ==================================================================
    // Bài giảng
    // ==================================================================
    private void showLessons(HttpServletRequest request, HttpServletResponse response, int userId)
            throws ServletException, IOException {
        // Giáo viên chỉ thấy các bài giảng do chính mình tạo
        List<Lesson> lessons = lessonDAO.listByCreator(userId);
        List<Subject> subjects = subjectDAO.listAll();
        List<Topic> topics = topicDAO.listAll();

        request.setAttribute("lessons", lessons);
        request.setAttribute("subjects", subjects);
        request.setAttribute("topics", topics);
        request.setAttribute("activeMenu", "lessons");
        request.getRequestDispatcher("/views/teacher/lessons.jsp").forward(request, response);
    }

    private void showLessonForm(HttpServletRequest request, HttpServletResponse response, int userId)
            throws ServletException, IOException {
        int id = PageController.parseInt(request.getParameter("id"), 0);
        Lesson lesson = id > 0 ? lessonDAO.findById(id) : null;

        // Không cho sửa bài giảng của giáo viên khác
        if (lesson != null && lesson.getCreatedBy() != userId) {
            flash(request, "Bạn không có quyền sửa bài giảng này.");
            response.sendRedirect(request.getContextPath() + "/teacher/lessons");
            return;
        }

        request.setAttribute("lesson", lesson);
        request.setAttribute("subjects", subjectDAO.listAll());
        request.setAttribute("topics", topicDAO.listAll());
        request.setAttribute("activeMenu", "lessons");
        request.getRequestDispatcher("/views/teacher/lesson-form.jsp").forward(request, response);
    }

    private void saveLesson(HttpServletRequest request, HttpServletResponse response, int userId)
            throws ServletException, IOException {

        int id = PageController.parseInt(request.getParameter("id"), 0);
        int subjectId = PageController.parseInt(request.getParameter("subjectId"), 0);
        int topicId = PageController.parseInt(request.getParameter("topicId"), 0);
        String title = trim(request.getParameter("title"));
        String description = trim(request.getParameter("description"));
        String lessonType = request.getParameter("lessonType");
        String videoUrl = trim(request.getParameter("videoUrl"));
        String documentUrl = trim(request.getParameter("documentUrl"));
        String content = request.getParameter("content");
        int duration = PageController.parseInt(request.getParameter("durationMinutes"), 0);

        if (title.isEmpty()) {
            flash(request, "Vui lòng nhập tiêu đề bài giảng.");
            response.sendRedirect(request.getContextPath() + "/teacher/lesson-form"
                    + (id > 0 ? "?id=" + id : ""));
            return;
        }
        if (subjectId <= 0 || topicId <= 0) {
            flash(request, "Vui lòng chọn môn học và chủ đề.");
            response.sendRedirect(request.getContextPath() + "/teacher/lesson-form"
                    + (id > 0 ? "?id=" + id : ""));
            return;
        }

        Lesson l = new Lesson();
        l.setLessonId(id);
        l.setSubjectId(subjectId);
        l.setTopicId(topicId);
        l.setTitle(title);
        l.setDescription(description);
        l.setLessonType(lessonType == null ? "TEXT" : lessonType);
        l.setVideoUrl(videoUrl);
        l.setDocumentUrl(documentUrl);
        l.setContent(content);
        l.setDurationMinutes(duration);
        l.setCreatedBy(userId);

        if (id > 0) {
            lessonDAO.update(l);
            flash(request, "Đã cập nhật bài giảng.");
        } else {
            lessonDAO.create(l);
            flash(request, "Đã thêm bài giảng mới.");
        }
        response.sendRedirect(request.getContextPath() + "/teacher/lessons");
    }

    private void deleteLesson(HttpServletRequest request, HttpServletResponse response, int userId)
            throws IOException {
        int id = PageController.parseInt(request.getParameter("id"), 0);
        Lesson lesson = id > 0 ? lessonDAO.findById(id) : null;
        if (lesson != null && lesson.getCreatedBy() != userId) {
            flash(request, "Bạn không có quyền xoá bài giảng của giáo viên khác.");
        } else if (id > 0) {
            lessonDAO.delete(id);
            flash(request, "Đã xoá bài giảng.");
        }
        response.sendRedirect(request.getContextPath() + "/teacher/lessons");
    }

    // ==================================================================
    // Câu hỏi
    // ==================================================================
    private void showQuestions(HttpServletRequest request, HttpServletResponse response, int userId)
            throws ServletException, IOException {
        // Giáo viên chỉ thấy các câu hỏi do chính mình tạo
        List<Question> questions = questionDAO.listByCreator(userId);
        request.setAttribute("questions", questions);
        request.setAttribute("subjects", subjectDAO.listAll());
        request.setAttribute("topics", topicDAO.listAll());
        request.setAttribute("activeMenu", "questions");
        request.getRequestDispatcher("/views/teacher/questions.jsp").forward(request, response);
    }

    private void showQuestionForm(HttpServletRequest request, HttpServletResponse response, int userId)
            throws ServletException, IOException {
        int id = PageController.parseInt(request.getParameter("id"), 0);
        Question question = id > 0 ? questionDAO.findById(id) : null;

        // Không cho sửa câu hỏi của giáo viên khác
        if (question != null && question.getCreatedBy() != userId) {
            flash(request, "Bạn không có quyền sửa câu hỏi này.");
            response.sendRedirect(request.getContextPath() + "/teacher/questions");
            return;
        }

        request.setAttribute("question", question);
        request.setAttribute("subjects", subjectDAO.listAll());
        request.setAttribute("topics", topicDAO.listAll());
        request.setAttribute("activeMenu", "questions");
        request.getRequestDispatcher("/views/teacher/question-form.jsp").forward(request, response);
    }

    private void saveQuestion(HttpServletRequest request, HttpServletResponse response, int userId)
            throws ServletException, IOException {

        int id = PageController.parseInt(request.getParameter("id"), 0);
        int subjectId = PageController.parseInt(request.getParameter("subjectId"), 0);
        int topicId = PageController.parseInt(request.getParameter("topicId"), 0);
        String content = trim(request.getParameter("content"));
        String type = request.getParameter("questionType");
        String correctAnswer = trim(request.getParameter("correctAnswer"));
        String difficulty = request.getParameter("difficulty");
        String explanation = request.getParameter("explanation");

        if (content.isEmpty()) {
            flash(request, "Vui lòng nhập nội dung câu hỏi.");
            response.sendRedirect(request.getContextPath() + "/teacher/question-form"
                    + (id > 0 ? "?id=" + id : ""));
            return;
        }

        Question q = new Question();
        q.setQuestionId(id);
        q.setSubjectId(subjectId);
        q.setTopicId(topicId);
        q.setContent(content);
        q.setType(type == null ? "MCQ" : type);
        q.setCorrectAnswer(correctAnswer);
        q.setOptionA(trim(request.getParameter("optionA")));
        q.setOptionB(trim(request.getParameter("optionB")));
        q.setOptionC(trim(request.getParameter("optionC")));
        q.setOptionD(trim(request.getParameter("optionD")));
        q.setExplanation(explanation);
        q.setDifficulty(difficulty == null ? "MEDIUM" : difficulty);
        q.setCreatedBy(userId);
        q.setSource("TEACHER");

        if (id > 0) {
            questionDAO.update(q);
            flash(request, "Đã cập nhật câu hỏi.");
        } else {
            questionDAO.create(q);
            flash(request, "Đã thêm câu hỏi mới.");
        }
        response.sendRedirect(request.getContextPath() + "/teacher/questions");
    }

    private void deleteQuestion(HttpServletRequest request, HttpServletResponse response, int userId)
            throws IOException {
        int id = PageController.parseInt(request.getParameter("id"), 0);
        Question question = id > 0 ? questionDAO.findById(id) : null;
        if (question != null && question.getCreatedBy() != userId) {
            flash(request, "Bạn không có quyền xoá câu hỏi của giáo viên khác.");
        } else if (id > 0) {
            questionDAO.delete(id);
            flash(request, "Đã xoá câu hỏi.");
        }
        response.sendRedirect(request.getContextPath() + "/teacher/questions");
    }

    // ==================================================================
    // Chủ đề
    // ==================================================================
    private void showTopics(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("topics", topicDAO.listAll());
        request.setAttribute("subjects", subjectDAO.listAll());
        request.setAttribute("activeMenu", "topics");
        request.getRequestDispatcher("/views/teacher/topics.jsp").forward(request, response);
    }

    private void showTopicForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int id = PageController.parseInt(request.getParameter("id"), 0);
        Topic topic = id > 0 ? topicDAO.findById(id) : null;

        request.setAttribute("topic", topic);
        request.setAttribute("subjects", subjectDAO.listAll());
        request.setAttribute("activeMenu", "topics");
        request.getRequestDispatcher("/views/teacher/topic-form.jsp").forward(request, response);
    }

    private void saveTopic(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int id = PageController.parseInt(request.getParameter("id"), 0);
        int subjectId = PageController.parseInt(request.getParameter("subjectId"), 0);
        String name = trim(request.getParameter("name"));
        String description = trim(request.getParameter("description"));
        int orderIndex = PageController.parseInt(request.getParameter("orderIndex"), 0);

        if (name.isEmpty() || subjectId <= 0) {
            flash(request, "Vui lòng nhập tên chủ đề và chọn môn học.");
            response.sendRedirect(request.getContextPath() + "/teacher/topic-form"
                    + (id > 0 ? "?id=" + id : ""));
            return;
        }

        if (id > 0) {
            topicDAO.update(id, subjectId, name, description, orderIndex);
            flash(request, "Đã cập nhật chủ đề.");
        } else {
            topicDAO.create(subjectId, name, description, orderIndex);
            flash(request, "Đã thêm chủ đề mới.");
        }
        response.sendRedirect(request.getContextPath() + "/teacher/topics");
    }

    private void deleteTopic(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        int id = PageController.parseInt(request.getParameter("id"), 0);
        if (id > 0) {
            topicDAO.delete(id);
            flash(request, "Đã xoá chủ đề.");
        }
        response.sendRedirect(request.getContextPath() + "/teacher/topics");
    }

    // ==================================================================
    // AI sinh câu hỏi
    // ==================================================================
    private void showAiGenerator(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("subjects", subjectDAO.listAll());
        request.setAttribute("topics", topicDAO.listAll());
        request.setAttribute("activeMenu", "ai-generator");
        request.getRequestDispatcher("/views/teacher/ai-generator.jsp").forward(request, response);
    }

    private void generateWithAi(HttpServletRequest request, HttpServletResponse response, int userId)
            throws ServletException, IOException {

        int subjectId = PageController.parseInt(request.getParameter("subjectId"), 0);
        int topicId = PageController.parseInt(request.getParameter("topicId"), 0);
        String type = request.getParameter("questionType");
        String difficulty = request.getParameter("difficulty");
        int count = PageController.parseInt(request.getParameter("count"), 10);
        boolean autoSave = "1".equals(request.getParameter("autoSave"));

        Topic topic = topicDAO.findById(topicId);
        Subject subject = subjectId > 0 ? subjectDAO.findById(subjectId) : null;
        if (topic == null || subject == null) {
            flash(request, "Vui lòng chọn môn học và chủ đề hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/teacher/ai-generator");
            return;
        }

        AiService ai = new AiService();
        if (!ai.isConfigured()) {
            flash(request, "AI chưa được cấu hình. Vui lòng liên hệ quản trị viên.");
            response.sendRedirect(request.getContextPath() + "/teacher/ai-generator");
            return;
        }

        List<Question> generated = ai.generateQuestions(subjectId, topicId, subject.getName(),
                topic.getName(), type, count, difficulty, userId);

        if (generated.isEmpty()) {
            flash(request, "AI không sinh được câu hỏi nào. Hãy thử lại.");
            response.sendRedirect(request.getContextPath() + "/teacher/ai-generator");
            return;
        }

        if (autoSave) {
            int saved = questionDAO.createBatch(generated);
            flash(request, "Đã lưu " + saved + " câu hỏi AI sinh vào ngân hàng câu hỏi.");
        } else {
            flash(request, "AI đã sinh " + generated.size()
                    + " câu hỏi. Xem và chọn câu hỏi muốn lưu bên dưới.");
        }
        // Luôn giữ danh sách trong session để giáo viên xoá được câu hỏi AI sinh
        // (kể cả khi đã tự động lưu - khi đó xoá sẽ xoá thật trong DB).
        request.getSession().setAttribute("aiGeneratedQuestions", generated);

        request.setAttribute("generated", generated);
        request.setAttribute("subjects", subjectDAO.listAll());
        request.setAttribute("topics", topicDAO.listAll());
        request.setAttribute("activeMenu", "ai-generator");
        request.getRequestDispatcher("/views/teacher/ai-generator.jsp").forward(request, response);
    }

    /**
     * Xoá một câu hỏi vừa được AI sinh ra.
     * <p>Hai trường hợp:
     * <ul>
     *   <li>Câu hỏi CHƯA lưu (đang chờ giáo viên duyệt) — nằm trong session
     *       {@code aiGeneratedQuestions}, chỉ cần loại khỏi danh sách.</li>
     *   <li>Câu hỏi ĐÃ lưu vào DB (bật "Tự động lưu") — xoá thật trong database,
     *       kèm kiểm tra quyền sở hữu như các xoá câu hỏi khác.</li>
     * </ul>
     *
     * @param index vị trí trong danh sách AI sinh (dùng cho câu hỏi chưa lưu)
     * @param id    mã câu hỏi trong DB (dùng cho câu hỏi đã lưu)
     */
    private void deleteAiGeneratedQuestion(HttpServletRequest request, HttpServletResponse response,
            int userId) throws IOException, ServletException {

        int index = PageController.parseInt(request.getParameter("index"), -1);
        int id = PageController.parseInt(request.getParameter("id"), 0);

        // ---- Trường hợp 1: câu hỏi đã lưu trong DB ----
        if (id > 0) {
            Question question = questionDAO.findById(id);
            if (question == null) {
                flash(request, "Không tìm thấy câu hỏi cần xoá.");
            } else if (question.getCreatedBy() != userId) {
                flash(request, "Bạn không có quyền xoá câu hỏi của giáo viên khác.");
            } else {
                questionDAO.delete(id);
                flash(request, "Đã xoá câu hỏi AI sinh.");
            }
            response.sendRedirect(request.getContextPath() + "/teacher/questions");
            return;
        }

        // ---- Trường hợp 2: câu hỏi chưa lưu, nằm trong session ----
        if (index >= 0) {
            HttpSession sess = request.getSession();
            Object o = sess.getAttribute("aiGeneratedQuestions");
            if (o instanceof List) {
                @SuppressWarnings("unchecked")
                List<Question> list = (List<Question>) o;
                if (index < list.size()) {
                    list.remove(index);
                    sess.setAttribute("aiGeneratedQuestions", list);
                    flash(request, "Đã bỏ câu hỏi khỏi danh sách AI sinh.");
                } else {
                    flash(request, "Không tìm thấy câu hỏi cần xoá.");
                }
            }
        }

        // Trả lại trang AI generator kèm danh sách còn lại
        request.setAttribute("generated", getSessionGenerated(request));
        request.setAttribute("subjects", subjectDAO.listAll());
        request.setAttribute("topics", topicDAO.listAll());
        request.setAttribute("activeMenu", "ai-generator");
        request.getRequestDispatcher("/views/teacher/ai-generator.jsp").forward(request, response);
    }

    /**
     * Xoá toàn bộ câu hỏi AI vừa sinh.
     * <p>Câu hỏi đã lưu trong DB sẽ bị xoá thật; câu hỏi chưa lưu chỉ bị loại
     * khỏi danh sách chờ duyệt trong session.
     */
    private void deleteAllAiGeneratedQuestions(HttpServletRequest request, HttpServletResponse response,
            int userId) throws IOException, ServletException {

        List<Question> list = getSessionGenerated(request);
        int removedFromDb = 0;

        if (list != null) {
            for (Question q : list) {
                if (q.getQuestionId() <= 0) {
                    continue;
                }
                Question inDb = questionDAO.findById(q.getQuestionId());
                // Chỉ xoá được câu hỏi do chính giáo viên này tạo
                if (inDb != null && inDb.getCreatedBy() == userId) {
                    if (questionDAO.delete(inDb.getQuestionId())) {
                        removedFromDb++;
                    }
                }
            }
        }

        // Xoá sạch danh sách chờ duyệt trong session
        request.getSession().removeAttribute("aiGeneratedQuestions");

        if (removedFromDb > 0) {
            flash(request, "Đã xoá " + removedFromDb + " câu hỏi AI đã lưu trong ngân hàng câu hỏi.");
        } else {
            flash(request, "Đã bỏ toàn bộ câu hỏi AI sinh khỏi danh sách chờ duyệt.");
        }

        request.setAttribute("generated", null);
        request.setAttribute("subjects", subjectDAO.listAll());
        request.setAttribute("topics", topicDAO.listAll());
        request.setAttribute("activeMenu", "ai-generator");
        request.getRequestDispatcher("/views/teacher/ai-generator.jsp").forward(request, response);
    }

    /**
     * Lấy danh sách câu hỏi AI sinh đang chờ duyệt trong session.
     */
    @SuppressWarnings("unchecked")
    private List<Question> getSessionGenerated(HttpServletRequest request) {
        Object o = request.getSession().getAttribute("aiGeneratedQuestions");
        if (o instanceof List) {
            return (List<Question>) o;
        }
        return null;
    }

    // ==================================================================
    // Helper
    // ==================================================================
    static void flash(HttpServletRequest request, String message) {
        request.getSession().setAttribute("flash", message);
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }
}
