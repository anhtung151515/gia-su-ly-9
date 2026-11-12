<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!doctype html>
<html lang="vi">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Tổng quan quản trị | Gia Sư Lý 9</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Manrope:wght@300;400;600;700;800&display=swap" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/main.css" rel="stylesheet">
</head>
<body>
<div class="app-shell">
    <%@ include file="/views/common/_sidebar.jspf" %>
    <div class="app-main">
        <% request.setAttribute("pageTitle", "Tổng quan quản trị"); %>
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
                <div class="page-sub">Toàn quyền quản lý nội dung học tập, người dùng và hệ thống AI</div>
            </div>

            <div class="row g-3 mb-4">
                <div class="col-6 col-lg-3">
                    <div class="stat-card">
                        <div class="stat-icon cyan"><i class="bi bi-mortarboard"></i></div>
                        <div>
                            <div class="stat-value">${totalStudents}</div>
                            <div class="stat-label">Học sinh</div>
                        </div>
                    </div>
                </div>
                <div class="col-6 col-lg-3">
                    <div class="stat-card">
                        <div class="stat-icon green"><i class="bi bi-person-badge"></i></div>
                        <div>
                            <div class="stat-value">${totalTeachers}</div>
                            <div class="stat-label">Giáo viên</div>
                        </div>
                    </div>
                </div>
                <div class="col-6 col-lg-3">
                    <div class="stat-card">
                        <div class="stat-icon blue"><i class="bi bi-collection-play"></i></div>
                        <div>
                            <div class="stat-value">${totalLessons}</div>
                            <div class="stat-label">Bài giảng</div>
                        </div>
                    </div>
                </div>
                <div class="col-6 col-lg-3">
                    <div class="stat-card">
                        <div class="stat-icon purple"><i class="bi bi-patch-question"></i></div>
                        <div>
                            <div class="stat-value">${totalQuestions}</div>
                            <div class="stat-label">Câu hỏi</div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="row g-3 mb-4">
                <div class="col-6 col-lg-3">
                    <div class="stat-card">
                        <div class="stat-icon orange"><i class="bi bi-atom"></i></div>
                        <div>
                            <div class="stat-value">${totalSubjects}</div>
                            <div class="stat-label">Môn học</div>
                        </div>
                    </div>
                </div>
                <div class="col-6 col-lg-3">
                    <div class="stat-card">
                        <div class="stat-icon blue"><i class="bi bi-folder2"></i></div>
                        <div>
                            <div class="stat-value">${totalTopics}</div>
                            <div class="stat-label">Chủ đề</div>
                        </div>
                    </div>
                </div>
                <div class="col-6 col-lg-3">
                    <div class="stat-card">
                        <div class="stat-icon green"><i class="bi bi-clipboard-data"></i></div>
                        <div>
                            <div class="stat-value">${totalPractices}</div>
                            <div class="stat-label">Phiếu luyện tập</div>
                        </div>
                    </div>
                </div>
                <div class="col-6 col-lg-3">
                    <a class="stat-card" href="${pageContext.request.contextPath}/admin/question-stats"
                       style="text-decoration:none;">
                        <div class="stat-icon red"><i class="bi bi-graph-up-arrow"></i></div>
                        <div>
                            <div class="stat-value">${totalWrong}</div>
                            <div class="stat-label">Lượt làm sai</div>
                        </div>
                    </a>
                </div>
                <div class="col-6 col-lg-3">
                    <a class="stat-card" href="${pageContext.request.contextPath}/admin/ai-logs"
                       style="text-decoration:none;">
                        <div class="stat-icon purple"><i class="bi bi-stars"></i></div>
                        <div>
                            <div class="stat-value">${totalAiCalls}</div>
                            <div class="stat-label">Lượt gọi AI</div>
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
                            <a class="topic-row" href="${pageContext.request.contextPath}/admin/user-form">
                                <div class="d-flex align-items-center gap-3">
                                    <div class="topic-index"><i class="bi bi-person-plus"></i></div>
                                    <div class="topic-name">Thêm tài khoản</div>
                                </div>
                                <i class="bi bi-arrow-right text-muted"></i>
                            </a>
                            <a class="topic-row" href="${pageContext.request.contextPath}/admin/subjects">
                                <div class="d-flex align-items-center gap-3">
                                    <div class="topic-index"><i class="bi bi-plus-lg"></i></div>
                                    <div class="topic-name">Thêm / sửa môn học</div>
                                </div>
                                <i class="bi bi-arrow-right text-muted"></i>
                            </a>
                            <a class="topic-row" href="${pageContext.request.contextPath}/admin/topics">
                                <div class="d-flex align-items-center gap-3">
                                    <div class="topic-index"><i class="bi bi-plus-lg"></i></div>
                                    <div class="topic-name">Thêm / sửa chủ đề</div>
                                </div>
                                <i class="bi bi-arrow-right text-muted"></i>
                            </a>
                            <a class="topic-row" href="${pageContext.request.contextPath}/admin/ai-settings">
                                <div class="d-flex align-items-center gap-3">
                                    <div class="topic-index"><i class="bi bi-gear"></i></div>
                                    <div>
                                        <div class="topic-name">Cấu hình API AI</div>
                                        <div class="text-muted" style="font-size:.8rem;">Provider, model, prompt và API key</div>
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
                            <h5 class="card-title">Nhật ký AI gần đây</h5>
                            <a href="${pageContext.request.contextPath}/admin/ai-logs" class="btn btn-sm btn-outline-primary">Tất cả</a>
                        </div>
                        <div class="card-body">
                            <%
                                java.util.List logs = (java.util.List) request.getAttribute("recentLogs");
                                if (logs == null || logs.isEmpty()) {
                            %>
                                <div class="empty-state py-4">
                                    <i class="bi bi-journal-text"></i>
                                    <p class="mb-0">Chưa có lượt gọi AI nào.</p>
                                </div>
                            <% } else {
                                int lim = Math.min(6, logs.size());
                                for (int i = 0; i < lim; i++) {
                                    com.giasuly9.model.AiLog l = (com.giasuly9.model.AiLog) logs.get(i);
                            %>
                            <div class="topic-row">
                                <div class="d-flex align-items-center gap-3 flex-grow-1">
                                    <div class="topic-index"><i class="bi bi-stars"></i></div>
                                    <div>
                                        <div class="topic-name">${l.feature}</div>
                                        <div class="text-muted" style="font-size:.8rem;">
                                            ${l.fullName == null ? "Không rõ" : l.fullName} · ${l.tokensUsed} token
                                        </div>
                                    </div>
                                </div>
                                <span class="badge-soft <%= l.isSuccess() ? "badge-on" : "badge-off" %>">
                                    <%= l.isSuccess() ? "OK" : "Lỗi" %>
                                </span>
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
