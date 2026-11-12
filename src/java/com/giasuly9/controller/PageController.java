package com.giasuly9.controller;

import com.giasuly9.dao.LessonDAO;
import com.giasuly9.dao.LessonProgressDAO;
import com.giasuly9.dao.SubjectDAO;
import com.giasuly9.dao.TeacherProfileDAO;
import com.giasuly9.dao.TopicDAO;
import com.giasuly9.filter.SessionUtil;
import com.giasuly9.model.Lesson;
import com.giasuly9.model.Subject;
import com.giasuly9.model.TeacherProfile;
import com.giasuly9.model.Topic;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * Các trang công khai: trang chủ, danh sách chủ đề Vật lý 9, danh sách giáo viên.
 * Trang này chỉ phục vụ môn Vật lý 9 nên không có bước chọn môn học.
 */
public class PageController extends HttpServlet {

    private final SubjectDAO subjectDAO = new SubjectDAO();
    private final TopicDAO topicDAO = new TopicDAO();
    private final LessonDAO lessonDAO = new LessonDAO();
    private final TeacherProfileDAO teacherProfileDAO = new TeacherProfileDAO();
    private final LessonProgressDAO lessonProgressDAO = new LessonProgressDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getServletPath();

        switch (path) {
            case "/home":
            case "/":
                showHome(request, response);
                return;
            case "/subjects":
                // Không còn mục chọn môn học -> chuyển thẳng sang danh sách chủ đề
                response.sendRedirect(request.getContextPath() + "/topics");
                return;
            case "/subject-detail":
                response.sendRedirect(request.getContextPath() + "/topics");
                return;
            case "/topics":
                showTopics(request, response);
                return;
            case "/teachers":
                showTeachers(request, response);
                return;
            case "/about":
            case "/contact":
                request.getRequestDispatcher("/views/public/static.jsp").forward(request, response);
                return;
            default:
                response.sendRedirect(request.getContextPath() + "/home");
        }
    }

    private void showHome(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<Subject> subjects = subjectDAO.listActive();
        List<Topic> topics = topicDAO.listAll();
        List<TeacherProfile> teachers = teacherProfileDAO.listActive();
        request.setAttribute("subjects", subjects);
        request.setAttribute("topics", topics);
        request.setAttribute("teachers", teachers);
        request.setAttribute("activeMenu", "home");
        request.getRequestDispatcher("/views/public/home.jsp").forward(request, response);
    }

    /**
     * Trang danh sách chủ đề Vật lý 9 (thay cho trang "Môn học").
     */
    private void showTopics(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String q = trim(request.getParameter("q"));

        List<Topic> topics = topicDAO.listAll();
        if (!q.isEmpty()) {
            String key = q.toLowerCase();
            topics.removeIf(t -> {
                String name = t.getName() == null ? "" : t.getName().toLowerCase();
                String desc = t.getDescription() == null ? "" : t.getDescription().toLowerCase();
                return !(name.contains(key) || desc.contains(key));
            });
        }

        request.setAttribute("topics", topics);
        request.setAttribute("keyword", q);
        request.setAttribute("activeMenu", "topics");
        request.getRequestDispatcher("/views/public/topics.jsp").forward(request, response);
    }

    /**
     * Trang giáo viên: danh sách thầy cô kèm video bài giảng Vật lý 9.
     */
    private void showTeachers(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int teacherId = parseInt(request.getParameter("teacherId"), 0);

        List<TeacherProfile> teachers = teacherProfileDAO.listActive();
        teacherProfileDAO.refreshVideoCounts();
        teachers = teacherProfileDAO.listActive();

        TeacherProfile selected = null;
        for (TeacherProfile t : teachers) {
            if (t.getUserId() == teacherId) {
                selected = t;
                break;
            }
        }

        List<Lesson> lessons = java.util.Collections.emptyList();
        if (selected != null) {
            lessons = lessonDAO.listActiveByCreator(selected.getUserId());
        }

        int userId = SessionUtil.currentUserId(request);

        request.setAttribute("teachers", teachers);
        request.setAttribute("selectedTeacher", selected);
        request.setAttribute("lessons", lessons);
        request.setAttribute("progressMap", lessonProgressDAO.mapByUser(userId));
        request.setAttribute("activeMenu", "teachers");
        request.getRequestDispatcher("/views/public/teachers.jsp").forward(request, response);
    }

    static int parseInt(String s, int def) {
        if (s == null) {
            return def;
        }
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }
}
