<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.giasuly9.model.PracticeSession" %>
<!doctype html>
<html lang="vi">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Luyện tập | Gia Sư Lý 9</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Manrope:wght@300;400;600;700;800&display=swap" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/main.css" rel="stylesheet">
</head>
<body>
<div class="app-shell">
    <%@ include file="/views/common/_sidebar.jspf" %>
    <div class="app-main">
        <% request.setAttribute("pageTitle", "Luyện tập"); %>
        <%@ include file="/views/common/_topbar.jspf" %>

        <div class="app-content">
            <% if (session.getAttribute("flash") != null) { %>
                <div class="alert alert-success alert-dismissible fade show">
                    <i class="bi bi-check-circle"></i> <%= session.getAttribute("flash") %>
                    <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                </div>
                <% session.removeAttribute("flash"); %>
            <% } %>

            <div class="page-head">
                <h2 class="page-title">Theo dõi luyện tập</h2>
                <div class="page-sub">Lịch sử luyện tập của toàn bộ học sinh</div>
            </div>

            <%
                List<PracticeSession> sessions = (List<PracticeSession>) request.getAttribute("sessions");
                if (sessions == null || sessions.isEmpty()) {
            %>
                <div class="card">
                    <div class="empty-state">
                        <i class="bi bi-clipboard-data"></i>
                        <h5>Chưa có phiếu luyện tập nào</h5>
                    </div>
                </div>
            <% } else { %>
                <div class="card">
                    <div class="table-wrap">
                        <table class="table">
                            <thead>
                            <tr>
                                <th style="width:50px;">#</th>
                                <th>Học sinh</th>
                                <th>Chủ đề</th>
                                <th>Loại</th>
                                <th>Tổng câu</th>
                                <th>Đúng / Sai</th>
                                <th>Điểm</th>
                                <th>Trạng thái</th>
                                <th>Thời gian</th>
                            </tr>
                            </thead>
                            <tbody>
                            <%
                                int i = 1;
                                for (PracticeSession s : sessions) {
                                    pageContext.setAttribute("s", s);

                            %>
                            <tr>
                                <td class="text-muted" style="font-size:.83rem;"><%= i %></td>
                                <td>
                                    <div class="fw-bold" style="font-size:.9rem;">${s.studentName}</div>
                                    <div class="text-muted" style="font-size:.78rem;">${s.subjectName}</div>
                                </td>
                                <td class="text-muted" style="font-size:.85rem;">${s.topicName}</td>
                                <td>
                                    <span class="badge-soft <%= "MCQ".equals(s.getPracticeType()) ? "badge-mcq" : "badge-essay" %>">
                                        <%= "MCQ".equals(s.getPracticeType()) ? "Trắc nghiệm" : "Tự luận" %>
                                    </span>
                                </td>
                                <td class="text-muted" style="font-size:.85rem;"><%= s.getTotalQuestions() %></td>
                                <td style="font-size:.85rem;">
                                    <span class="text-success fw-bold"><%= s.getCorrectCount() %></span>
                                    <span class="text-muted">/</span>
                                    <span class="text-danger fw-bold"><%= s.getWrongCount() %></span>
                                </td>
                                <td>
                                    <span class="badge-soft <%= s.getScore() >= 8 ? "badge-easy" : (s.getScore() >= 5 ? "badge-medium" : "badge-hard") %>">
                                        <%= String.format("%.1f", s.getScore()) %>/10
                                    </span>
                                </td>
                                <td>
                                    <span class="badge-soft <%= "COMPLETED".equals(s.getStatus()) ? "badge-on" : "badge-medium" %>">
                                        <%= "COMPLETED".equals(s.getStatus()) ? "Hoàn thành" : "Đang làm" %>
                                    </span>
                                </td>
                                <td class="text-muted" style="font-size:.8rem;">
                                    <%= s.getStartedAt() == null ? "—" : s.getStartedAt() %>
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
