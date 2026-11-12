<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.giasuly9.model.Subject" %>
<!doctype html>
<html lang="vi">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Môn học | Gia Sư Lý 9</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Manrope:wght@300;400;600;700;800&display=swap" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/main.css" rel="stylesheet">
</head>
<body>
<%@ include file="/views/common/_header.jspf" %>

<section class="page">
    <div class="container">
        <div class="page-head">
            <h2 class="page-title">Môn học</h2>
            <div class="page-sub">Chọn môn học để bắt đầu học chủ đề và luyện tập</div>
        </div>

        <%
            List<Subject> subjects = (List<Subject>) request.getAttribute("subjects");
            if (subjects == null || subjects.isEmpty()) {
        %>
            <div class="card">
                <div class="empty-state">
                    <i class="bi bi-inbox"></i>
                    <h5>Chưa có môn học</h5>
                </div>
            </div>
        <% } else { %>
            <div class="row g-3">
                <%
                    for (Subject s : subjects) {
                        pageContext.setAttribute("s", s);

                %>
                <div class="col-md-6 col-lg-4">
                    <a class="subject-card" href="${pageContext.request.contextPath}/subject-detail?id=${s.subjectId}">
                        <div class="subject-cover">
                            <% if (s.getImageUrl() != null && !s.getImageUrl().trim().isEmpty()) { %>
                                <img src="${s.imageUrl}" alt="${s.name}">
                            <% } else { %>
                                <i class="bi bi-atom"></i>
                            <% } %>
                        </div>
                        <div class="subject-body">
                            <h5 class="subject-name">${s.name}</h5>
                            <p class="subject-desc">
                                <% if (s.getDescription() != null) { %>
                                    <%= s.getDescription().length() > 120 ? s.getDescription().substring(0, 120) + "..." : s.getDescription() %>
                                <% } %>
                            </p>
                            <div class="lesson-meta mt-2">
                                <span><i class="bi bi-person-badge"></i> <%= s.getTeacherName() == null ? "Chưa phân công" : s.getTeacherName() %></span>
                            </div>
                        </div>
                    </a>
                </div>
                <% } %>
            </div>
        <% } %>
    </div>
</section>

<%@ include file="/views/common/_footer.jspf" %>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
