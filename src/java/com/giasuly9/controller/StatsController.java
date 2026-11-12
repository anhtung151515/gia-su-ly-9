package com.giasuly9.controller;

import com.giasuly9.dao.PracticeDAO;
import com.giasuly9.dao.TopicDAO;
import com.giasuly9.model.QuestionStat;
import com.giasuly9.model.TopicStat;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Thống kê câu hỏi học sinh làm sai nhiều.
 * <p>Dùng chung cho khu vực giáo viên (/teacher/question-stats) và quản trị
 * (/admin/question-stats) — cả hai đều trỏ tới cùng một trang giao diện.
 */
public class StatsController extends HttpServlet {

    private final PracticeDAO practiceDAO = new PracticeDAO();
    private final TopicDAO topicDAO = new TopicDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int topicId = PageController.parseInt(request.getParameter("topicId"), 0);
        String questionType = request.getParameter("questionType");
        int limit = PageController.parseInt(request.getParameter("limit"), 50);
        if (limit <= 0) {
            limit = 50;
        }
        if (limit > 500) {
            limit = 500;
        }

        // Mặc định chỉ hiện câu có ít nhất 1 lượt sai (thống kê "làm sai nhiều").
        // Tick "Hiện cả câu làm đúng" (showAll=1) thì hiện tất cả câu đã được làm.
        boolean filterWrong = !"1".equals(request.getParameter("showAll"));

        List<QuestionStat> stats = practiceDAO.listQuestionStats(topicId, questionType, limit, filterWrong);
        List<TopicStat> topicStats = practiceDAO.listTopicStats();

        request.setAttribute("stats", stats);
        request.setAttribute("topicStats", topicStats);
        request.setAttribute("topics", topicDAO.listAll());
        request.setAttribute("selectedTopicId", topicId);
        request.setAttribute("selectedType", questionType);
        request.setAttribute("selectedLimit", limit);
        request.setAttribute("onlyWrong", filterWrong);
        request.setAttribute("totalWrong", practiceDAO.countAllWrong());
        request.setAttribute("questionsWithWrong", practiceDAO.countQuestionsWithWrong());

        // Đường dẫn quay lại đúng theo khu vực đang xem
        String backPath = request.getServletPath() != null
                && request.getServletPath().startsWith("/admin") ? "/admin" : "/teacher";
        request.setAttribute("backPath", backPath);
        request.setAttribute("activeMenu", "question-stats");

        request.getRequestDispatcher("/views/common/question-stats.jsp").forward(request, response);
    }
}
