<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.giasuly9.model.Question" %>
<!doctype html>
<html lang="vi">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Ngân hàng câu hỏi | Gia Sư Lý 9</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Manrope:wght@300;400;600;700;800&display=swap" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/main.css" rel="stylesheet">
</head>
<body>
<div class="app-shell">
    <%@ include file="/views/common/_sidebar.jspf" %>
    <div class="app-main">
        <% request.setAttribute("pageTitle", "Ngân hàng câu hỏi"); %>
        <%@ include file="/views/common/_topbar.jspf" %>

        <div class="app-content">
            <% if (session.getAttribute("flash") != null) { %>
                <div class="alert alert-success alert-dismissible fade show">
                    <i class="bi bi-check-circle"></i> <%= session.getAttribute("flash") %>
                    <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                </div>
                <% session.removeAttribute("flash"); %>
            <% } %>

            <div class="page-head d-flex justify-content-between align-items-end flex-wrap gap-2">
                <div>
                    <h2 class="page-title">Ngân hàng câu hỏi</h2>
                    <div class="page-sub">Toàn bộ câu hỏi trắc nghiệm và tự luận</div>
                </div>
                <div class="d-flex gap-2">
                    <a href="${pageContext.request.contextPath}/teacher/ai-generator" class="btn btn-outline-primary btn-sm">
                        <i class="bi bi-stars"></i> AI tạo câu hỏi
                    </a>
                    <a href="${pageContext.request.contextPath}/teacher/question-form" class="btn btn-primary btn-sm">
                        <i class="bi bi-plus-lg"></i> Thêm câu hỏi
                    </a>
                </div>
            </div>

            <%
                List<Question> questions = (List<Question>) request.getAttribute("questions");
                if (questions == null || questions.isEmpty()) {
            %>
                <div class="card">
                    <div class="empty-state">
                        <i class="bi bi-patch-question"></i>
                        <h5>Chưa có câu hỏi nào</h5>
                    </div>
                </div>
            <% } else { %>
                <div class="card">
                    <div class="table-wrap">
                        <table class="table">
                            <thead>
                            <tr>
                                <th style="width:50px;">#</th>
                                <th>Nội dung câu hỏi</th>
                                <th>Chủ đề</th>
                                <th>Loại</th>
                                <th>Độ khó</th>
                                <th>Nguồn</th>
                                <th class="text-end">Thao tác</th>
                            </tr>
                            </thead>
                            <tbody>
                            <%
                                int i = 1;
                                for (Question q : questions) {
                                    pageContext.setAttribute("q", q);

                            %>
                            <tr>
                                <td class="text-muted" style="font-size:.83rem;"><%= i %></td>
                                <td>
                                    <div style="font-size:.89rem;max-width:460px;">
                                        <%= q.getContent() == null ? "" : (q.getContent().length() > 140 ? q.getContent().substring(0, 140) + "..." : q.getContent()) %>
                                    </div>
                                    <% if (q.isMcq() && q.getCorrectAnswer() != null) { %>
                                        <div class="text-muted" style="font-size:.78rem;margin-top:3px;">
                                            Đáp án: <strong><%= q.getCorrectAnswer() %></strong>
                                        </div>
                                    <% } %>
                                </td>
                                <td class="text-muted" style="font-size:.83rem;">${q.topicName}</td>
                                <td>
                                    <span class="badge-soft <%= q.isMcq() ? "badge-mcq" : "badge-essay" %>">
                                        <%= q.isMcq() ? "Trắc nghiệm" : "Tự luận" %>
                                    </span>
                                </td>
                                <td>
                                    <span class="badge-soft badge-<%= q.getDifficulty() == null ? "medium" : q.getDifficulty().toLowerCase() %>">
                                        <%= "EASY".equals(q.getDifficulty()) ? "Dễ" : ("HARD".equals(q.getDifficulty()) ? "Khó" : "TB") %>
                                    </span>
                                </td>
                                <td>
                                    <span class="badge-soft <%= "AI".equals(q.getSource()) ? "badge-ai" : "badge-teacher" %>">
                                        <%= "AI".equals(q.getSource()) ? "AI" : "Giáo viên" %>
                                    </span>
                                </td>
                                <td class="text-end">
                                    <div class="d-flex gap-1 justify-content-end">
                                        <a href="${pageContext.request.contextPath}/teacher/question-form?id=${q.questionId}"
                                           class="btn btn-sm btn-outline-primary" title="Sửa"><i class="bi bi-pencil"></i></a>
                                        <a href="${pageContext.request.contextPath}/admin/question-delete?id=${q.questionId}"
                                           class="btn btn-sm btn-outline-danger" title="Xoá"
                                           onclick="return confirm('Xoá câu hỏi này?');"><i class="bi bi-trash"></i></a>
                                    </div>
                                </td>
                            </tr>
                            <% i++; } %>
                            </tbody>
                        </table>
                    </div>
                </div>
            <% } %>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
