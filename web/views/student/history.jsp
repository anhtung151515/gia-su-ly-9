<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.giasuly9.model.PracticeSession" %>
<!doctype html>
<html lang="vi">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Lịch sử luyện tập | Gia Sư Lý 9</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Manrope:wght@300;400;600;700;800&display=swap" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/main.css" rel="stylesheet">
</head>
<body>
<%@ include file="/views/common/_header.jspf" %>

<section class="page">
    <div class="container">
        <div class="page-head d-flex justify-content-between align-items-end flex-wrap gap-2">
            <div>
                <h2 class="page-title">Lịch sử luyện tập</h2>
                <div class="page-sub">Xem lại kết quả và nhận xét AI cho các bài đã làm</div>
            </div>
            <a href="${pageContext.request.contextPath}/student/practice-setup" class="btn btn-primary btn-sm">
                <i class="bi bi-plus-lg"></i> Luyện tập mới
            </a>
        </div>

        <%
            List<PracticeSession> sessions = (List<PracticeSession>) request.getAttribute("sessions");
            if (sessions == null || sessions.isEmpty()) {
        %>
            <div class="card">
                <div class="empty-state">
                    <i class="bi bi-clock-history"></i>
                    <h5>Chưa có lịch sử luyện tập</h5>
                    <p class="mb-0">Bắt đầu luyện tập để xem kết quả tại đây.</p>
                </div>
            </div>
        <% } else { %>
            <div class="card">
                <div class="table-wrap">
                    <table class="table">
                        <thead>
                        <tr>
                            <th>Chủ đề</th>
                            <th>Kiểu</th>
                            <th>Kết quả</th>
                            <th>Điểm</th>
                            <th>Ngày</th>
                            <th></th>
                        </tr>
                        </thead>
                        <tbody>
                        <%
                            for (PracticeSession s : sessions) {
                                pageContext.setAttribute("s", s);
                        %>
                        <tr>
                            <td>
                                <div class="fw-bold" style="font-size:.9rem;">${s.topicName}</div>
                                <div class="text-muted" style="font-size:.8rem;">${s.subjectName}</div>
                            </td>
                            <td>
                                <span class="badge-soft <%= "ESSAY".equals(s.getPracticeType()) ? "badge-essay" : "badge-mcq" %>">
                                    <%= "ESSAY".equals(s.getPracticeType()) ? "Tự luận" : "Trắc nghiệm" %>
                                </span>
                            </td>
                            <td>
                                <span class="text-success fw-bold">${s.correctCount}</span>
                                <span class="text-muted">/ ${s.totalQuestions} đúng</span>
                            </td>
                            <td>
                                <span class="badge-soft <%= s.getScore() >= 8 ? "badge-easy" : (s.getScore() >= 5 ? "badge-medium" : "badge-hard") %>">
                                    <%= String.format("%.1f", s.getScore()) %>/10
                                </span>
                            </td>
                            <td class="text-muted" style="font-size:.83rem;">
                                <%= s.getStartedAt() == null ? "" : new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(s.getStartedAt()) %>
                            </td>
                            <td class="text-end">
                                <a href="${pageContext.request.contextPath}/student/result?sessionId=${s.sessionId}"
                                   class="btn btn-sm btn-outline-primary">
                                    <i class="bi bi-eye"></i> Chi tiết
                                </a>
                            </td>
                        </tr>
                        <% } %>
                        </tbody>
                    </table>
                </div>
            </div>
        <% } %>
    </div>
</section>

<%@ include file="/views/common/_footer.jspf" %>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
