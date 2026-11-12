<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.giasuly9.model.QuestionStat" %>
<%@ page import="com.giasuly9.model.TopicStat" %>
<%@ page import="com.giasuly9.model.Topic" %>
<!doctype html>
<html lang="vi">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Thống kê câu hỏi làm sai | Gia Sư Lý 9</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Manrope:wght@300;400;600;700;800&display=swap" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/main.css" rel="stylesheet">
</head>
<body>
<div class="app-shell">
    <%@ include file="/views/common/_sidebar.jspf" %>
    <div class="app-main">
        <% request.setAttribute("pageTitle", "Thống kê câu hỏi làm sai"); %>
        <%@ include file="/views/common/_topbar.jspf" %>

        <%
            List<QuestionStat> stats = (List<QuestionStat>) request.getAttribute("stats");
            List<TopicStat> topicStats = (List<TopicStat>) request.getAttribute("topicStats");
            List<Topic> topics = (List<Topic>) request.getAttribute("topics");
            int selectedTopicId = 0;
            Object st = request.getAttribute("selectedTopicId");
            if (st instanceof Integer) { selectedTopicId = (Integer) st; }
            String selectedType = (String) request.getAttribute("selectedType");
            int selectedLimit = 50;
            Object sl = request.getAttribute("selectedLimit");
            if (sl instanceof Integer) { selectedLimit = (Integer) sl; }
            boolean onlyWrong = !Boolean.FALSE.equals(request.getAttribute("onlyWrong"));
            Object tw = request.getAttribute("totalWrong");
            Object qw = request.getAttribute("questionsWithWrong");
            int totalWrong = (tw instanceof Integer) ? (Integer) tw : 0;
            int questionsWithWrong = (qw instanceof Integer) ? (Integer) qw : 0;
            String basePath = (String) request.getAttribute("backPath");
            if (basePath == null) { basePath = "/teacher"; }
            String statsUrl = request.getContextPath() + basePath + "/question-stats";
        %>

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
                    <h2 class="page-title"><i class="bi bi-graph-up-arrow text-danger"></i> Câu hỏi học sinh làm sai nhiều</h2>
                    <div class="page-sub">
                        Thống kê từ dữ liệu luyện tập thực tế — giúp nhận biết kiến thức nào học sinh còn yếu
                    </div>
                </div>
                <a href="${pageContext.request.contextPath}${basePath}/questions"
                   class="btn btn-outline-primary btn-sm">
                    <i class="bi bi-patch-question"></i> Ngân hàng câu hỏi
                </a>
            </div>

            <!-- ===== Thẻ số liệu tổng quan ===== -->
            <div class="row g-3 mb-4">
                <div class="col-6 col-lg-3">
                    <div class="stat-card">
                        <div class="stat-icon red"><i class="bi bi-x-octagon"></i></div>
                        <div>
                            <div class="stat-value"><%= totalWrong %></div>
                            <div class="stat-label">Lượt làm sai</div>
                        </div>
                    </div>
                </div>
                <div class="col-6 col-lg-3">
                    <div class="stat-card">
                        <div class="stat-icon orange"><i class="bi bi-exclamation-triangle"></i></div>
                        <div>
                            <div class="stat-value"><%= questionsWithWrong %></div>
                            <div class="stat-label">Câu hỏi có bị sai</div>
                        </div>
                    </div>
                </div>
                <div class="col-6 col-lg-3">
                    <div class="stat-card">
                        <div class="stat-icon purple"><i class="bi bi-collection"></i></div>
                        <div>
                            <div class="stat-value"><%= topicStats == null ? 0 : topicStats.size() %></div>
                            <div class="stat-label">Chủ đề có lượt làm</div>
                        </div>
                    </div>
                </div>
                <div class="col-6 col-lg-3">
                    <div class="stat-card">
                        <div class="stat-icon blue"><i class="bi bi-list-ol"></i></div>
                        <div>
                            <div class="stat-value"><%= stats == null ? 0 : stats.size() %></div>
                            <div class="stat-label">Câu đang hiển thị</div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- ===== Bộ lọc ===== -->
            <div class="card mb-3">
                <div class="card-body">
                    <form method="get" action="<%= statsUrl %>" class="row g-3 align-items-end">
                        <div class="col-md-4">
                            <label class="form-label">Chủ đề</label>
                            <select name="topicId" class="form-select">
                                <option value="0">-- Tất cả chủ đề --</option>
                                <%
                                    if (topics != null) {
                                        for (Topic t : topics) {
                                %>
                                <option value="<%= t.getTopicId() %>"
                                        <%= t.getTopicId() == selectedTopicId ? "selected" : "" %>>
                                    <%= t.getName() %>
                                </option>
                                <% } } %>
                            </select>
                        </div>
                        <div class="col-md-3">
                            <label class="form-label">Loại câu hỏi</label>
                            <select name="questionType" class="form-select">
                                <option value="" <%= (selectedType == null || selectedType.isEmpty()) ? "selected" : "" %>>Tất cả</option>
                                <option value="MCQ" <%= "MCQ".equals(selectedType) ? "selected" : "" %>>Trắc nghiệm</option>
                                <option value="ESSAY" <%= "ESSAY".equals(selectedType) ? "selected" : "" %>>Tự luận</option>
                            </select>
                        </div>
                        <div class="col-md-2">
                            <label class="form-label">Số câu hiện</label>
                            <select name="limit" class="form-select">
                                <option value="20"  <%= selectedLimit == 20  ? "selected" : "" %>>20</option>
                                <option value="50"  <%= selectedLimit == 50  ? "selected" : "" %>>50</option>
                                <option value="100" <%= selectedLimit == 100 ? "selected" : "" %>>100</option>
                                <option value="200" <%= selectedLimit == 200 ? "selected" : "" %>>200</option>
                            </select>
                        </div>
                        <div class="col-md-3">
                            <label class="form-label">&nbsp;</label>
                            <div class="form-check">
                                <input class="form-check-input" type="checkbox" name="showAll" value="1"
                                       id="showAll" <%= onlyWrong ? "" : "checked" %>>
                                <label class="form-check-label" for="showAll">
                                    Hiện cả câu làm đúng
                                </label>
                            </div>
                        </div>
                        <div class="col-12 d-flex gap-2">
                            <button type="submit" class="btn btn-primary">
                                <i class="bi bi-funnel"></i> Lọc thống kê
                            </button>
                            <a href="<%= statsUrl %>" class="btn btn-outline-secondary">Đặt lại</a>
                        </div>
                    </form>
                </div>
            </div>

            <div class="row g-3">
                <!-- ===== Bảng thống kê câu hỏi ===== -->
                <div class="col-lg-8">
                    <div class="card">
                        <div class="card-head">
                            <h5 class="card-title">
                                <i class="bi bi-list-stats"></i>
                                Câu hỏi học sinh làm sai nhiều nhất
                            </h5>
                        </div>
                        <%
                            if (stats == null || stats.isEmpty()) {
                        %>
                        <div class="card-body">
                            <div class="empty-state">
                                <i class="bi bi-clipboard-data"></i>
                                <h5>Chưa có dữ liệu luyện tập</h5>
                                <p class="mb-0">Thống kê sẽ hiển thị khi học sinh đã làm bài và có câu sai.</p>
                            </div>
                        </div>
                        <% } else { %>
                        <div class="table-wrap">
                            <table class="table">
                                <thead>
                                <tr>
                                    <th style="width:44px;">#</th>
                                    <th>Nội dung câu hỏi</th>
                                    <th style="width:110px;">Chủ đề</th>
                                    <th style="width:100px;">Loại</th>
                                    <th style="width:150px;">Lượt làm sai</th>
                                    <th style="width:80px;">Điểm TB</th>
                                    <th style="width:110px;">Thao tác</th>
                                </tr>
                                </thead>
                                <tbody>
                                <%
                                    int i = 1;
                                    for (QuestionStat s : stats) {
                                        String risk = s.getRiskLevel();
                                        String riskBadge = "NONE".equals(risk) ? "badge-on"
                                                : ("HIGH".equals(risk) ? "badge-off"
                                                : ("MEDIUM".equals(risk) ? "badge-medium" : "badge-easy"));
                                        String riskText = "NONE".equals(risk) ? "Bình thường"
                                                : ("HIGH".equals(risk) ? "Rất cao"
                                                : ("MEDIUM".equals(risk) ? "Cao" : "Thấp"));
                                        double wrongRate = s.getWrongRate();
                                %>
                                <tr>
                                    <td class="text-muted" style="font-size:.83rem;"><%= i %></td>
                                    <td>
                                        <div style="font-size:.88rem;max-width:420px;line-height:1.5;">
                                            <%= s.getContent() == null ? "" : (s.getContent().length() > 170 ? s.getContent().substring(0, 170) + "..." : s.getContent()) %>
                                        </div>
                                        <div class="mt-1">
                                            <span class="badge-soft <%= riskBadge %>">
                                                <i class="bi bi-exclamation-triangle"></i> Mức độ sai: <%= riskText %>
                                            </span>
                                        </div>
                                    </td>
                                    <td class="text-muted" style="font-size:.8rem;"><%= s.getTopicName() == null ? "" : s.getTopicName() %></td>
                                    <td>
                                        <span class="badge-soft <%= s.isMcq() ? "badge-mcq" : "badge-essay" %>">
                                            <%= s.isMcq() ? "Trắc nghiệm" : "Tự luận" %>
                                        </span>
                                    </td>
                                    <td>
                                        <div class="d-flex align-items-center gap-2">
                                            <strong style="font-size:.95rem;color:var(--err);"><%= s.getWrongCount() %></strong>
                                            <div class="progress-thin flex-grow-1" style="min-width:52px;">
                                                <div style="width:<%= Math.min(100, wrongRate) %>%;background:var(--err);"></div>
                                            </div>
                                            <span class="text-muted" style="font-size:.75rem;white-space:nowrap;">
                                                <%= String.format("%.0f", wrongRate) %>%
                                            </span>
                                        </div>
                                        <div class="text-muted" style="font-size:.73rem;margin-top:3px;">
                                            sai / <%= s.getTotalAttempts() %> lượt
                                        </div>
                                    </td>
                                    <td>
                                        <span style="font-weight:700;font-size:.86rem;">
                                            <%= String.format("%.1f", s.getAvgScore()) %>
                                        </span>
                                    </td>
                                    <td class="text-end">
                                        <a href="${pageContext.request.contextPath}/teacher/question-form?id=<%= s.getQuestionId() %>"
                                           class="btn btn-sm btn-outline-primary" title="Sửa câu hỏi">
                                            <i class="bi bi-pencil"></i>
                                        </a>
                                    </td>
                                </tr>
                                <% i++; } %>
                                </tbody>
                            </table>
                        </div>
                        <% } %>
                    </div>
                </div>

                <!-- ===== Thống kê theo chủ đề ===== -->
                <div class="col-lg-4">
                    <div class="card">
                        <div class="card-head">
                            <h5 class="card-title"><i class="bi bi-folder2"></i> Chủ đề hay bị sai</h5>
                        </div>
                        <%
                            if (topicStats == null || topicStats.isEmpty()) {
                        %>
                        <div class="card-body">
                            <div class="empty-state py-4">
                                <i class="bi bi-folder"></i>
                                <p class="mb-0">Chưa có dữ liệu.</p>
                            </div>
                        </div>
                        <% } else { %>
                        <div class="card-body">
                            <%
                                for (TopicStat ts : topicStats) {
                                    double rate = ts.getWrongRate();
                            %>
                            <div class="mb-3">
                                <div class="d-flex justify-content-between align-items-center mb-1">
                                    <a href="<%= statsUrl %>?topicId=<%= ts.getTopicId() %>"
                                       class="topic-name" style="font-size:.86rem;">
                                        <%= ts.getTopicName() %>
                                    </a>
                                    <span class="text-muted" style="font-size:.76rem;white-space:nowrap;">
                                        <%= ts.getWrongCount() %> sai · <%= String.format("%.0f", rate) %>%
                                    </span>
                                </div>
                                <div class="progress-thin">
                                    <div style="width:<%= Math.min(100, rate) %>%;background:var(--err);"></div>
                                </div>
                            </div>
                            <% } %>
                        </div>
                        <% } %>
                    </div>

                    <div class="card mt-3">
                        <div class="card-head">
                            <h5 class="card-title"><i class="bi bi-lightbulb"></i> Gợi ý cho giáo viên</h5>
                        </div>
                        <div class="card-body">
                            <ul class="mb-0" style="font-size:.85rem;line-height:1.8;color:var(--muted);">
                                <li>Câu có <strong>tỉ lệ sai cao</strong> thường do đề hoặc đáp án chưa rõ ràng — nên xem lại và sửa.</li>
                                <li>Câu <strong>bị sai nhiều lượt</strong> là kiến thức học sinh chưa vững — nên ôn lại trong giờ học.</li>
                                <li>Chủ đề có tỉ lệ sai cao cần <strong>bổ sung bài tập</strong> hoặc video giải thích.</li>
                                <li>Dùng nút <i class="bi bi-pencil"></i> để sửa trực tiếp câu hỏi gây nhầm lẫn.</li>
                            </ul>
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
