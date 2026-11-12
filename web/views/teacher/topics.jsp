<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.giasuly9.model.Topic" %>
<!doctype html>
<html lang="vi">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Chủ đề môn học | Gia Sư Lý 9</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Manrope:wght@300;400;600;700;800&display=swap" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/main.css" rel="stylesheet">
</head>
<body>
<div class="app-shell">
    <%@ include file="/views/common/_sidebar.jspf" %>
    <div class="app-main">
        <% request.setAttribute("pageTitle", "Chủ đề môn học"); %>
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
                    <h2 class="page-title">Chủ đề môn học</h2>
                    <div class="page-sub">Chủ đề là nơi nhóm bài giảng và câu hỏi cho học sinh luyện tập</div>
                </div>
                <a href="${pageContext.request.contextPath}/teacher/topic-form" class="btn btn-primary btn-sm">
                    <i class="bi bi-plus-lg"></i> Thêm chủ đề
                </a>
            </div>

            <%
                List<Topic> topics = (List<Topic>) request.getAttribute("topics");
                if (topics == null || topics.isEmpty()) {
            %>
                <div class="card">
                    <div class="empty-state">
                        <i class="bi bi-folder2-open"></i>
                        <h5>Chưa có chủ đề nào</h5>
                        <a href="${pageContext.request.contextPath}/teacher/topic-form" class="btn btn-sm btn-primary mt-2">
                            Thêm chủ đề đầu tiên
                        </a>
                    </div>
                </div>
            <% } else { %>
                <div class="card">
                    <div class="table-wrap">
                        <table class="table">
                            <thead>
                            <tr>
                                <th style="width:50px;">TT</th>
                                <th>Tên chủ đề</th>
                                <th>Môn học</th>
                                <th>Bài giảng</th>
                                <th class="text-end">Thao tác</th>
                            </tr>
                            </thead>
                            <tbody>
                            <%
                                int i = 1;
                                for (Topic t : topics) {
                                    pageContext.setAttribute("t", t);

                            %>
                            <tr>
                                <td class="text-muted" style="font-size:.83rem;"><%= i %></td>
                                <td>
                                    <div class="fw-bold" style="font-size:.9rem;">${t.name}</div>
                                    <% if (t.getDescription() != null && !t.getDescription().trim().isEmpty()) { %>
                                        <div class="text-muted" style="font-size:.8rem;">
                                            <%= t.getDescription().length() > 80 ? t.getDescription().substring(0, 80) + "..." : t.getDescription() %>
                                        </div>
                                    <% } %>
                                </td>
                                <td class="text-muted" style="font-size:.85rem;">${t.subjectName}</td>
                                <td>
                                    <span class="badge-soft badge-mcq">${t.lessonCount} bài</span>
                                </td>
                                <td class="text-end">
                                    <div class="d-flex gap-1 justify-content-end">
                                        <a href="${pageContext.request.contextPath}/teacher/topic-form?id=${t.topicId}"
                                           class="btn btn-sm btn-outline-primary"><i class="bi bi-pencil"></i></a>
                                        <a href="${pageContext.request.contextPath}/teacher/topic-delete?id=${t.topicId}"
                                           class="btn btn-sm btn-outline-danger"
                                           onclick="return confirm('Xoá chủ đề này? Các bài giảng và câu hỏi thuộc chủ đề cũng sẽ bị ảnh hưởng.');">
                                            <i class="bi bi-trash"></i>
                                        </a>
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
