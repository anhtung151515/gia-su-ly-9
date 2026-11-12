<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!doctype html>
<html lang="vi">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Tổng quan | Gia Sư Lý 9</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Manrope:wght@300;400;600;700;800&display=swap" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/main.css" rel="stylesheet">
</head>
<body>
<div class="app-shell">
    <%@ include file="/views/common/_sidebar.jspf" %>
    <div class="app-main">
        <% request.setAttribute("pageTitle", "Tổng quan giáo viên"); %>
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
                <h2 class="page-title">Xin chào, ${sessionScope.user.fullName}!</h2>
                <div class="page-sub">Quản lý bài giảng, ngân hàng câu hỏi và hỗ trợ học sinh</div>
            </div>

            <div class="row g-3 mb-4">
                <div class="col-6 col-lg-3">
                    <div class="stat-card">
                        <div class="stat-icon blue"><i class="bi bi-collection-play"></i></div>
                        <div>
                            <div class="stat-value">${totalLessons}</div>
                            <div class="stat-label">Bài giảng đã thêm</div>
                        </div>
                    </div>
                </div>
                <div class="col-6 col-lg-3">
                    <div class="stat-card">
                        <div class="stat-icon green"><i class="bi bi-patch-question"></i></div>
                        <div>
                            <div class="stat-value">${totalQuestions}</div>
                            <div class="stat-label">Câu hỏi đã thêm</div>
                        </div>
                    </div>
                </div>
                <div class="col-6 col-lg-3">
                    <div class="stat-card">
                        <div class="stat-icon cyan"><i class="bi bi-folder2"></i></div>
                    <div>
                            <div class="stat-value">${topics != null ? topics.size() : 0}</div>
                            <div class="stat-label">Chủ đề</div>
                    </div>
                </div>
                <div class="col-6 col-lg-3">
                    <a class="stat-card" href="${pageContext.request.contextPath}/teacher/ai-generator"
                       style="text-decoration:none;">
                        <div class="stat-icon purple"><i class="bi bi-stars"></i></div>
                        <div>
                            <div class="stat-value">AI</div>
                            <div class="stat-label">Tạo câu hỏi nhanh</div>
                        </div>
                    </a>
                </div>
            </div>

            <div class="row g-3 mb-4">
                <div class="col-12">
                    <a class="stat-card" href="${pageContext.request.contextPath}/teacher/question-stats"
                       style="text-decoration:none;">
                        <div class="stat-icon red"><i class="bi bi-graph-up-arrow"></i></div>
                        <div>
                            <div class="stat-value">${totalWrong}</div>
                            <div class="stat-label">
                                Lượt làm sai — xem câu hỏi nào học sinh hay sai để ôn lại kiến thức
                            </div>
                        </div>
                    </a>
                </div>
            </div>

            <div class="row g-3">
                <div class="col-lg-6">
                    <div class="card h-100">
                        <div class="card-head">
                            <h5 class="card-title">Thao tác nhanh</h5>
                        </div>
                        <div class="card-body">
                            <a class="topic-row" href="${pageContext.request.contextPath}/teacher/lesson-form">
                                <div class="d-flex align-items-center gap-3">
                                    <div class="topic-index"><i class="bi bi-plus-lg"></i></div>
                                    <div class="topic-name">Thêm bài giảng mới</div>
                                </div>
                                <i class="bi bi-arrow-right text-muted"></i>
                            </a>
                            <a class="topic-row" href="${pageContext.request.contextPath}/teacher/question-form">
                                <div class="d-flex align-items-center gap-3">
                                    <div class="topic-index"><i class="bi bi-plus-lg"></i></div>
                                    <div class="topic-name">Thêm câu hỏi trắc nghiệm / tự luận</div>
                                </div>
                                <i class="bi bi-arrow-right text-muted"></i>
                            </a>
                            <a class="topic-row" href="${pageContext.request.contextPath}/teacher/topic-form">
                                <div class="d-flex align-items-center gap-3">
                                    <div class="topic-index"><i class="bi bi-plus-lg"></i></div>
                                    <div class="topic-name">Thêm chủ đề môn học</div>
                                </div>
                                <i class="bi bi-arrow-right text-muted"></i>
                            </a>
                            <a class="topic-row" href="${pageContext.request.contextPath}/teacher/ai-generator">
                                <div class="d-flex align-items-center gap-3">
                                    <div class="topic-index"><i class="bi bi-stars"></i></div>
                                    <div>
                                        <div class="topic-name">AI sinh câu hỏi theo chủ đề</div>
                                        <div class="text-muted" style="font-size:.8rem;">Tự động tạo hàng loạt câu hỏi</div>
                                    </div>
                                </div>
                                <i class="bi bi-arrow-right text-muted"></i>
                            </a>
                        </div>
                    </div>
                </div>

                <div class="col-lg-6">
                    <div class="card h-100">
                        <div class="card-head">
                            <h5 class="card-title">Bài giảng gần đây</h5>
                            <a href="${pageContext.request.contextPath}/teacher/lessons" class="btn btn-sm btn-outline-primary">Tất cả</a>
                        </div>
                        <div class="card-body">
                            <%
                                java.util.List lessons = (java.util.List) request.getAttribute("myLessons");
                                if (lessons == null || lessons.isEmpty()) {
                            %>
                                <div class="empty-state py-4">
                                    <i class="bi bi-collection-play"></i>
                                    <p class="mb-0">Chưa có bài giảng nào.</p>
                                </div>
                            <% } else {
                                int lim = Math.min(6, lessons.size());
                                for (int i = 0; i < lim; i++) {
                                    com.giasuly9.model.Lesson l = (com.giasuly9.model.Lesson) lessons.get(i);
                            %>
                            <div class="topic-row">
                                <div class="d-flex align-items-center gap-3 flex-grow-1">
                                    <div class="topic-index"><i class="bi bi-play-fill"></i></div>
                                    <div>
                                        <div class="topic-name">${l.title}</div>
                                        <div class="text-muted" style="font-size:.8rem;">${l.topicName}</div>
                                    </div>
                                </div>
                            </div>
                            <% } } %>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
