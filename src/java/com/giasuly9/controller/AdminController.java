package com.giasuly9.controller;

import com.giasuly9.dao.AiConfigDAO;
import com.giasuly9.dao.AiLogDAO;
import com.giasuly9.dao.LessonDAO;
import com.giasuly9.dao.PracticeDAO;
import com.giasuly9.dao.QuestionDAO;
import com.giasuly9.dao.SubjectDAO;
import com.giasuly9.dao.TopicDAO;
import com.giasuly9.dao.UserDAO;
import com.giasuly9.filter.SessionUtil;
import com.giasuly9.model.Lesson;
import com.giasuly9.model.PracticeSession;
import com.giasuly9.model.Question;
import com.giasuly9.model.Subject;
import com.giasuly9.model.Topic;
import com.giasuly9.model.User;
import com.giasuly9.util.AiConfig;
import com.giasuly9.util.AiService;
import com.giasuly9.util.SecurityUtil;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * Khu vực quản trị viên: đủ quyền thêm/sửa/xoá giáo viên, học sinh, môn học,
 * chủ đề, bài giảng, câu hỏi; cấu hình API AI.
 */
public class AdminController extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();
    private final SubjectDAO subjectDAO = new SubjectDAO();
    private final TopicDAO topicDAO = new TopicDAO();
    private final LessonDAO lessonDAO = new LessonDAO();
    private final QuestionDAO questionDAO = new QuestionDAO();
    private final PracticeDAO practiceDAO = new PracticeDAO();
    private final AiConfigDAO aiConfigDAO = new AiConfigDAO();
    private final AiLogDAO aiLogDAO = new AiLogDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();

        switch (path) {
            case "/admin/dashboard":
                showDashboard(request, response);
                return;
            case "/admin/users":
                showUsers(request, response);
                return;
            case "/admin/user-form":
                showUserForm(request, response);
                return;
            case "/admin/subjects":
                showSubjects(request, response);
                return;
            case "/admin/topics":
                showTopics(request, response);
                return;
            case "/admin/lessons":
                showLessons(request, response);
                return;
            case "/admin/questions":
                showQuestions(request, response);
                return;
            case "/admin/practices":
                showPractices(request, response);
                return;
            case "/admin/ai-settings":
                showAiSettings(request, response);
                return;
            case "/admin/ai-logs":
                request.setAttribute("logs", aiLogDAO.listRecent(100));
                request.setAttribute("activeMenu", "ai-logs");
                request.getRequestDispatcher("/views/admin/ai-logs.jsp").forward(request, response);
                return;

            // ---- Các thao tác quản trị được gọi bằng link GET từ danh sách ----
            case "/admin/user-toggle":
                toggleUser(request, response);
                return;
            case "/admin/user-delete":
                deleteUser(request, response);
                return;
            case "/admin/user-reset-password":
                resetPassword(request, response);
                return;
            case "/admin/subject-delete":
                deleteSubject(request, response);
                return;
            case "/admin/topic-delete":
                deleteTopic(request, response);
                return;
            case "/admin/lesson-delete":
                deleteLesson(request, response);
                return;
            case "/admin/question-delete":
                deleteQuestion(request, response);
                return;

            default:
                response.sendRedirect(request.getContextPath() + "/admin/dashboard");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String path = request.getServletPath();

        switch (path) {
            case "/admin/user-save":
                saveUser(request, response);
                return;
            case "/admin/user-toggle":
                toggleUser(request, response);
                return;
            case "/admin/user-delete":
                deleteUser(request, response);
                return;
            case "/admin/user-reset-password":
                resetPassword(request, response);
                return;
            case "/admin/subject-save":
                saveSubject(request, response);
                return;
            case "/admin/subject-delete":
                deleteSubject(request, response);
                return;
            case "/admin/topic-save":
                saveTopic(request, response);
                return;
            case "/admin/topic-delete":
                deleteTopic(request, response);
                return;
            case "/admin/lesson-delete":
                deleteLesson(request, response);
                return;
            case "/admin/question-delete":
                deleteQuestion(request, response);
                return;
            case "/admin/ai-settings-save":
                saveAiSettings(request, response);
                return;
            case "/admin/ai-test":
                testAi(request, response);
                return;
            default:
                response.sendRedirect(request.getContextPath() + "/admin/dashboard");
        }
    }

    // ==================================================================
    // Dashboard
    // ==================================================================
    private void showDashboard(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("totalStudents", userDAO.countByRole("STUDENT"));
        request.setAttribute("totalTeachers", userDAO.countByRole("TEACHER"));
        request.setAttribute("totalSubjects", subjectDAO.listAll().size());
        request.setAttribute("totalTopics", topicDAO.listAll().size());
        request.setAttribute("totalLessons", lessonDAO.listAll().size());
        request.setAttribute("totalQuestions", questionDAO.countAll());
        request.setAttribute("totalPractices", practiceDAO.listAllSessions().size());
        request.setAttribute("totalAiCalls", aiLogDAO.countAll());
        request.setAttribute("totalWrong", practiceDAO.countAllWrong());
        request.setAttribute("recentLogs", aiLogDAO.listRecent(6));
        request.setAttribute("activeMenu", "dashboard");
        request.getRequestDispatcher("/views/admin/dashboard.jsp").forward(request, response);
    }

    // ==================================================================
    // Người dùng
    // ==================================================================
    private void showUsers(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String role = request.getParameter("role");
        List<User> users = (role == null || role.isEmpty()) ? userDAO.listAll() : userDAO.listByRole(role);
        request.setAttribute("users", users);
        request.setAttribute("selectedRole", role);
        request.setAttribute("activeMenu", "users");
        request.getRequestDispatcher("/views/admin/users.jsp").forward(request, response);
    }

    private void showUserForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int id = PageController.parseInt(request.getParameter("id"), 0);
        User user = id > 0 ? userDAO.findById(id) : null;
        request.setAttribute("user", user);
        request.setAttribute("teachers", userDAO.listByRole("TEACHER"));
        request.setAttribute("activeMenu", "users");
        request.getRequestDispatcher("/views/admin/user-form.jsp").forward(request, response);
    }

    private void saveUser(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int id = PageController.parseInt(request.getParameter("id"), 0);
        String username = trim(request.getParameter("username"));
        String fullName = trim(request.getParameter("fullName"));
        String email = trim(request.getParameter("email"));
        String phone = trim(request.getParameter("phone"));
        String role = request.getParameter("role");
        String className = trim(request.getParameter("className"));
        String password = request.getParameter("password");

        if (username.isEmpty() || fullName.isEmpty()) {
            flash(request, "Vui lòng nhập tên đăng nhập và họ tên.");
            response.sendRedirect(request.getContextPath() + "/admin/user-form"
                    + (id > 0 ? "?id=" + id : ""));
            return;
        }
        if (role == null || (!role.equals("ADMIN") && !role.equals("TEACHER") && !role.equals("STUDENT"))) {
            flash(request, "Vai trò không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/admin/user-form"
                    + (id > 0 ? "?id=" + id : ""));
            return;
        }

        if (id > 0) {
            // Cập nhật
            userDAO.update(id, fullName, email, phone, className);
            userDAO.updateRole(id, role);
            if (password != null && !password.trim().isEmpty()) {
                userDAO.changePassword(id, password.trim());
            }
            flash(request, "Đã cập nhật tài khoản " + username + ".");
        } else {
            // Tạo mới
            if (password == null || password.trim().isEmpty()) {
                password = SecurityUtil.generatePassword();
                request.getSession().setAttribute("generatedPassword", password);
            }
            if (userDAO.existsByUsername(username)) {
                flash(request, "Tên đăng nhập đã tồn tại.");
                response.sendRedirect(request.getContextPath() + "/admin/user-form");
                return;
            }
            int newId = userDAO.create(username, password.trim(), fullName, email, phone, role, className);
            if (newId > 0) {
                request.getSession().setAttribute("generatedPassword", password);
                flash(request, "Đã tạo tài khoản " + username + ".");
            } else {
                flash(request, "Không tạo được tài khoản.");
            }
        }
        response.sendRedirect(request.getContextPath() + "/admin/users");
    }

    private void toggleUser(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        int id = PageController.parseInt(request.getParameter("id"), 0);
        if (id > 0) {
            User u = userDAO.findById(id);
            if (u != null) {
                // Không cho admin tự khoá tài khoản của chính mình
                if (u.getUserId() == SessionUtil.currentUserId(request)) {
                    flash(request, "Không thể vô hiệu hoá tài khoản của chính mình.");
                } else {
                    userDAO.setActive(id, !u.isActive());
                    flash(request, "Đã cập nhật trạng thái tài khoản.");
                }
            }
        }
        response.sendRedirect(request.getContextPath() + "/admin/users");
    }

    private void deleteUser(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        int id = PageController.parseInt(request.getParameter("id"), 0);
        if (id > 0 && id != SessionUtil.currentUserId(request)) {
            userDAO.delete(id);
            flash(request, "Đã xoá tài khoản.");
        } else {
            flash(request, "Không thể xoá tài khoản này.");
        }
        response.sendRedirect(request.getContextPath() + "/admin/users");
    }

    private void resetPassword(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        int id = PageController.parseInt(request.getParameter("id"), 0);
        if (id > 0) {
            String newPass = SecurityUtil.generatePassword();
            userDAO.changePassword(id, newPass);
            request.getSession().setAttribute("generatedPassword", newPass);
            flash(request, "Đã đặt lại mật khẩu cho tài khoản.");
        }
        response.sendRedirect(request.getContextPath() + "/admin/users");
    }

    // ==================================================================
    // Môn học / chủ đề / bài giảng / câu hỏi
    // ==================================================================
    private void showSubjects(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("subjects", subjectDAO.listAll());
        request.setAttribute("teachers", userDAO.listByRole("TEACHER"));
        int editId = PageController.parseInt(request.getParameter("edit"), 0);
        request.setAttribute("editSubject", editId > 0 ? subjectDAO.findById(editId) : null);
        request.setAttribute("activeMenu", "subjects");
        request.getRequestDispatcher("/views/admin/subjects.jsp").forward(request, response);
    }

    private void saveSubject(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int id = PageController.parseInt(request.getParameter("id"), 0);
        String name = trim(request.getParameter("name"));
        String code = trim(request.getParameter("code"));
        String description = trim(request.getParameter("description"));
        String imageUrl = trim(request.getParameter("imageUrl"));
        int teacherId = PageController.parseInt(request.getParameter("teacherId"), 0);

        if (name.isEmpty()) {
            flash(request, "Vui lòng nhập tên môn học.");
            response.sendRedirect(request.getContextPath() + "/admin/subjects");
            return;
        }

        if (id > 0) {
            subjectDAO.update(id, name, code, description, imageUrl, teacherId);
            flash(request, "Đã cập nhật môn học.");
        } else {
            subjectDAO.create(name, code, description, imageUrl, teacherId);
            flash(request, "Đã thêm môn học.");
        }
        response.sendRedirect(request.getContextPath() + "/admin/subjects");
    }

    private void deleteSubject(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        int id = PageController.parseInt(request.getParameter("id"), 0);
        if (id > 0) {
            subjectDAO.delete(id);
            flash(request, "Đã xoá môn học.");
        }
        response.sendRedirect(request.getContextPath() + "/admin/subjects");
    }

    private void showTopics(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("topics", topicDAO.listAll());
        request.setAttribute("subjects", subjectDAO.listAll());
        request.setAttribute("activeMenu", "topics");
        request.getRequestDispatcher("/views/admin/topics.jsp").forward(request, response);
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
            response.sendRedirect(request.getContextPath() + "/admin/topics");
            return;
        }
        if (id > 0) {
            topicDAO.update(id, subjectId, name, description, orderIndex);
            flash(request, "Đã cập nhật chủ đề.");
        } else {
            topicDAO.create(subjectId, name, description, orderIndex);
            flash(request, "Đã thêm chủ đề.");
        }
        response.sendRedirect(request.getContextPath() + "/admin/topics");
    }

    private void deleteTopic(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        int id = PageController.parseInt(request.getParameter("id"), 0);
        if (id > 0) {
            topicDAO.delete(id);
            flash(request, "Đã xoá chủ đề.");
        }
        response.sendRedirect(request.getContextPath() + "/admin/topics");
    }

    private void showLessons(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("lessons", lessonDAO.listAll());
        request.setAttribute("activeMenu", "lessons");
        request.getRequestDispatcher("/views/admin/lessons.jsp").forward(request, response);
    }

    private void deleteLesson(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        int id = PageController.parseInt(request.getParameter("id"), 0);
        if (id > 0) {
            lessonDAO.delete(id);
            flash(request, "Đã xoá bài giảng.");
        }
        response.sendRedirect(request.getContextPath() + "/admin/lessons");
    }

    private void showQuestions(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("questions", questionDAO.listAll());
        request.setAttribute("activeMenu", "questions");
        request.getRequestDispatcher("/views/admin/questions.jsp").forward(request, response);
    }

    private void deleteQuestion(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        int id = PageController.parseInt(request.getParameter("id"), 0);
        if (id > 0) {
            questionDAO.delete(id);
            flash(request, "Đã xoá câu hỏi.");
        }
        response.sendRedirect(request.getContextPath() + "/admin/questions");
    }

    private void showPractices(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("sessions", practiceDAO.listAllSessions());
        request.setAttribute("activeMenu", "practices");
        request.getRequestDispatcher("/views/admin/practices.jsp").forward(request, response);
    }

    // ==================================================================
    // Cấu hình AI
    // ==================================================================
    private void showAiSettings(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        AiConfig cfg = aiConfigDAO.getActiveConfig();
        request.setAttribute("aiConfig", cfg);
        request.setAttribute("activeMenu", "ai-settings");
        request.getRequestDispatcher("/views/admin/ai-settings.jsp").forward(request, response);
    }

    private void saveAiSettings(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        AiConfig cfg = new AiConfig();
        cfg.setProvider(trim(request.getParameter("provider")));
        cfg.setApiKey(trim(request.getParameter("apiKey")));
        cfg.setBaseUrl(trim(request.getParameter("baseUrl")));
        cfg.setModel(trim(request.getParameter("model")));
        cfg.setTemperature(Double.parseDouble(defaultStr(request.getParameter("temperature"), "0.7")));
        cfg.setMaxTokens(Integer.parseInt(defaultStr(request.getParameter("maxTokens"), "2048")));
        cfg.setSystemPrompt(request.getParameter("systemPrompt"));
        cfg.setTutorPrompt(request.getParameter("tutorPrompt"));
        cfg.setQuestionPrompt(request.getParameter("questionPrompt"));

        if (aiConfigDAO.save(cfg)) {
            flash(request, "Đã lưu cấu hình AI.");
        } else {
            flash(request, "Không lưu được cấu hình AI.");
        }
        response.sendRedirect(request.getContextPath() + "/admin/ai-settings");
    }

    private void testAi(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String message = aiConfigDAO.testConnectionDetail();
        flash(request, message);
        response.sendRedirect(request.getContextPath() + "/admin/ai-settings");
    }

    // ==================================================================
    // Helper
    // ==================================================================
    private void flash(HttpServletRequest request, String message) {
        request.getSession().setAttribute("flash", message);
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }

    private static String defaultStr(String s, String def) {
        return (s == null || s.trim().isEmpty()) ? def : s.trim();
    }
}
