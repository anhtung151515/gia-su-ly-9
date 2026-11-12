<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.giasuly9.model.Topic" %>
<%@ page import="com.giasuly9.model.PracticeSession" %>
<!doctype html>
<html lang="vi">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Bảng điều khiển | Gia Sư Lý 9</title>
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
            <h2 class="page-title">Chào mừng, ${sessionScope.user.fullName}!</h2>
            <div class="page-sub">Tiếp tục học Vật lý 9 của bạn hôm nay nhé</div>
        </div>

        <% if (session.getAttribute("flash") != null) { %>
            <div class="alert alert-success alert-dismissible fade show">
                <i class="bi bi-check-circle"></i> <%= session.getAttribute("flash") %>
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
            <% session.removeAttribute("flash"); %>
        <% } %>

        <div class="row g-3 mb-4">
            <div class="col-6 col-lg-3">
                <div class="stat-card">
                    <div class="stat-icon blue"><i class="bi bi-collection-play"></i></div>
                    <div>
                        <div class="stat-value"><%= request.getAttribute("totalQuestions") %></div>
                        <div class="stat-label">Câu đã luyện</div>
                    </div>
                </div>
            </div>
            <div class="col-6 col-lg-3">
                <div class="stat-card">
                    <div class="stat-icon green"><i class="bi bi-check2-all"></i></div>
                    <div>
                        <div class="stat-value"><%= request.getAttribute("totalSessions") %></div>
                        <div class="stat-label">Phiên luyện tập</div>
                    </div>
                </div>
            </div>
            <div class="col-6 col-lg-3">
                <div class="stat-card">
                    <div class="stat-icon orange"><i class="bi bi-bullseye"></i></div>
                    <div>
                        <div class="stat-value"><%= String.format("%.0f", (Double) request.getAttribute("avgScore")) %>%</div>
                        <div class="stat-label">Điểm trung bình</div>
                    </div>
                </div>
            </div>
                <div class="col-6 col-lg-3">
                    <a class="stat-card" href="${pageContext.request.contextPath}/student/learning-progress"
                       style="text-decoration:none;">
                        <div class="stat-icon purple"><i class="bi bi-play-btn"></i></div>
                        <div>
                            <div class="stat-value"><%= request.getAttribute("completedLessons") %></div>
                            <div class="stat-label">Bài giảng đã xem</div>
                        </div>
                    </a>
                </div>
        </div>

        <div class="row g-3">
            <div class="col-lg-7">
                <div class="card h-100">
                    <div class="card-head">
                        <h5 class="card-title">Chủ đề Vật lý 9</h5>
                        <a href="${pageContext.request.contextPath}/topics" class="btn btn-sm btn-outline-primary">Xem tất cả</a>
                    </div>
                    <div class="card-body">
                        <%
                            List<Topic> topics = (List<Topic>) request.getAttribute("topics");
                            if (topics == null || topics.isEmpty()) {
                        %>
                            <div class="empty-state py-4">
                                <i class="bi bi-journal-x"></i>
                                <p class="mb-0">Chưa có chủ đề nào.</p>
                            </div>
                        <% } else {
                            int limit = Math.min(6, topics.size());
                            for (int i = 0; i < limit; i++) {
                                Topic t = topics.get(i);
                                pageContext.setAttribute("t", t);
                        %>
                            <a class="topic-row" href="${pageContext.request.contextPath}/student/practice-setup?topicId=${t.topicId}&type=MCQ">
                                <div class="d-flex align-items-center gap-3">
                                    <div class="topic-thumb">
                                        <% if (t.getImageUrl() != null && !t.getImageUrl().trim().isEmpty()) { %>
                                            <img src="${pageContext.request.contextPath}${t.imageUrl}" alt="${t.name}">
                                        <% } else { %>
                                            <i class="bi bi-atom"></i>
                                        <% } %>
                                    </div>
                                    <div>
                                        <div class="topic-name">${t.name}</div>
                                        <div class="text-muted" style="font-size:.82rem;">
                                            ${t.lessonCount} bài giảng
                                        </div>
                                    </div>
                                </div>
                                <i class="bi bi-arrow-right text-muted"></i>
                            </a>
                        <% } } %>
                    </div>
                </div>
            </div>

            <div class="col-lg-5">
                <div class="card h-100">
                    <div class="card-head">
                        <h5 class="card-title">Luyện tập gần đây</h5>
                        <a href="${pageContext.request.contextPath}/student/history" class="btn btn-sm btn-outline-primary">Lịch sử</a>
                    </div>
                    <div class="card-body">
                        <%
                            List<PracticeSession> recent = (List<PracticeSession>) request.getAttribute("recentSessions");
                            if (recent == null || recent.isEmpty()) {
                        %>
                            <div class="empty-state py-4">
                                <i class="bi bi-clipboard-check"></i>
                                <p class="mb-2">Bạn chưa luyện tập nào.</p>
                                <a href="${pageContext.request.contextPath}/student/practice-setup" class="btn btn-sm btn-primary">
                                    Bắt đầu luyện tập
                                </a>
                            </div>
                        <% } else {
                            for (PracticeSession s : recent.subList(0, Math.min(5, recent.size()))) {
                                pageContext.setAttribute("s", s);
                        %>
                            <a class="topic-row" href="${pageContext.request.contextPath}/student/result?sessionId=${s.sessionId}">
                                <div class="d-flex align-items-center gap-3">
                                    <div class="topic-index">
                                        <%= s.getPracticeType() != null && "ESSAY".equals(s.getPracticeType()) ? "T" : "N" %>
                                    </div>
                                    <div>
                                        <div class="topic-name">${s.topicName}</div>
                                        <div class="text-muted" style="font-size:.8rem;">
                                            ${s.correctCount}/${s.totalQuestions} đúng
                                        </div>
                                    </div>
                                </div>
                                <span class="badge-soft <%= s.getScore() >= 8 ? "badge-easy" : (s.getScore() >= 5 ? "badge-medium" : "badge-hard") %>">
                                    <%= String.format("%.1f", s.getScore()) %>/10
                                </span>
                            </a>
                        <% } } %>
                    </div>
                </div>
            </div>
        </div>

        <div class="row g-3 mt-1">
            <div class="col-md-4">
                <a class="subject-card" href="${pageContext.request.contextPath}/student/practice-setup?type=MCQ">
                    <div class="subject-body text-center py-4">
                        <div class="stat-icon blue mx-auto mb-3" style="width:52px;height:52px;font-size:1.4rem;">
                            <i class="bi bi-ui-checks-grid"></i>
                        </div>
                        <h6 class="fw-bold">Luyện trắc nghiệm</h6>
                        <p class="text-muted mb-0" style="font-size:.85rem;">Chọn đáp án, tự chấm điểm ngay, AI nhận xét câu sai</p>
                    </div>
                </a>
            </div>
            <div class="col-md-4">
                <a class="subject-card" href="${pageContext.request.contextPath}/student/practice-setup?type=ESSAY">
                    <div class="subject-body text-center py-4">
                        <div class="stat-icon cyan mx-auto mb-3" style="width:52px;height:52px;font-size:1.4rem;">
                            <i class="bi bi-pencil-square"></i>
                        </div>
                        <h6 class="fw-bold">Luyện tự luận</h6>
                        <p class="text-muted mb-0" style="font-size:.85rem;">Upload ảnh bài làm, AI chấm thang 10 và sửa lỗi</p>
                    </div>
                </a>
            </div>
            <div class="col-md-4">
                <a class="subject-card" href="${pageContext.request.contextPath}/teachers">
                    <div class="subject-body text-center py-4">
                        <div class="stat-icon purple mx-auto mb-3" style="width:52px;height:52px;font-size:1.4rem;">
                            <i class="bi bi-person-video3"></i>
                        </div>
                        <h6 class="fw-bold">Giáo viên Vật lý</h6>
                        <p class="text-muted mb-0" style="font-size:.85rem;">Xem video bài giảng của 3 thầy cô</p>
                    </div>
                </a>
            </div>
        </div>
    </div>
</section>

<%@ include file="/views/common/_footer.jspf" %>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
