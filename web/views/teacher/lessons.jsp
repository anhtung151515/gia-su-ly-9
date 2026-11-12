<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.giasuly9.model.Lesson" %>
<!doctype html>
<html lang="vi">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Quản lý bài giảng | Gia Sư Lý 9</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Manrope:wght@300;400;600;700;800&display=swap" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/main.css" rel="stylesheet">
</head>
<body>
<div class="app-shell">
    <%@ include file="/views/common/_sidebar.jspf" %>
    <div class="app-main">
        <% request.setAttribute("pageTitle", "Bài giảng"); %>
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
                    <h2 class="page-title">Bài giảng của tôi</h2>
                    <div class="page-sub">Chỉ hiển thị các bài giảng bạn đã thêm (video YouTube, bài đọc text và link học tập)</div>
                </div>
                <a href="${pageContext.request.contextPath}/teacher/lesson-form" class="btn btn-primary btn-sm">
                    <i class="bi bi-plus-lg"></i> Thêm bài giảng
                </a>
            </div>

            <%
                List<Lesson> lessons = (List<Lesson>) request.getAttribute("lessons");
                if (lessons == null || lessons.isEmpty()) {
            %>
                <div class="card">
                    <div class="empty-state">
                        <i class="bi bi-collection-play"></i>
                        <h5>Bạn chưa có bài giảng nào</h5>
                        <p class="mb-3">Thêm bài giảng đầu tiên của bạn, học sinh sẽ thấy ngay.</p>
                        <a href="${pageContext.request.contextPath}/teacher/lesson-form" class="btn btn-sm btn-primary mt-2">
                            Thêm bài giảng đầu tiên
                        </a>
                    </div>
                </div>
            <% } else { %>
                <div class="card">
                    <div class="table-wrap">
                        <table class="table">
                            <thead>
                            <tr>
                                <th>Tiêu đề</th>
                                <th>Chủ đề</th>
                                <th>Loại</th>
                                <th>Thời lượng</th>
                                <th class="text-end">Thao tác</th>
                            </tr>
                            </thead>
                            <tbody>
                            <%
                                for (Lesson l : lessons) {
                                    pageContext.setAttribute("l", l);

                            %>
                            <tr>
                                <td>
                                    <div class="fw-bold" style="font-size:.9rem;">${l.title}</div>
                                    <% if (l.getDescription() != null && !l.getDescription().trim().isEmpty()) { %>
                                        <div class="text-muted" style="font-size:.8rem;">
                                            <%= l.getDescription().length() > 70 ? l.getDescription().substring(0, 70) + "..." : l.getDescription() %>
                                        </div>
                                    <% } %>
                                </td>
                                <td class="text-muted" style="font-size:.85rem;">${l.topicName}</td>
                                <td>
                                    <span class="badge-soft <%= "VIDEO_YOUTUBE".equals(l.getLessonType()) ? "badge-hard" : ("TEXT".equals(l.getLessonType()) ? "badge-mcq" : "badge-essay") %>">
                                        <%= "VIDEO_YOUTUBE".equals(l.getLessonType()) ? "YouTube"
                                            : ("VIDEO_FILE".equals(l.getLessonType()) ? "Video"
                                            : ("TEXT".equals(l.getLessonType()) ? "Text" : "Website")) %>
                                    </span>
                                </td>
                                <td class="text-muted" style="font-size:.85rem;">
                                    <% if (l.getDurationMinutes() > 0) { %><%= l.getDurationMinutes() %> phút<% } else { %>—<% } %>
                                </td>
                                <td class="text-end">
                                    <div class="d-flex gap-1 justify-content-end">
                                        <a href="${pageContext.request.contextPath}/teacher/lesson-form?id=${l.lessonId}"
                                           class="btn btn-sm btn-outline-primary" title="Sửa">
                                            <i class="bi bi-pencil"></i>
                                        </a>
                                        <a href="${pageContext.request.contextPath}/teacher/lesson-delete?id=${l.lessonId}"
                                           class="btn btn-sm btn-outline-danger" title="Xoá"
                                           onclick="return confirm('Xoá bài giảng này?');">
                                            <i class="bi bi-trash"></i>
                                        </a>
                                    </div>
                                </td>
                            </tr>
                            <% } %>
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
