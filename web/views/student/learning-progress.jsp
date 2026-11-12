<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.giasuly9.model.Lesson" %>
<%@ page import="com.giasuly9.model.LessonProgress" %>
<%@ page import="com.giasuly9.model.Topic" %>
<%@ page import="com.giasuly9.model.TopicLearning" %>
<%
    List<TopicLearning> topicLearnings = (List<TopicLearning>) request.getAttribute("topicLearnings");
    List<Lesson> lessons = (List<Lesson>) request.getAttribute("lessons");
    Map<Integer, LessonProgress> progressMap = (Map<Integer, LessonProgress>) request.getAttribute("progressMap");
    if (progressMap == null) { progressMap = java.util.Collections.emptyMap(); }
    if (topicLearnings == null) { topicLearnings = java.util.Collections.emptyList(); }
    int totalLessons = (Integer) request.getAttribute("totalLessons");
    int doneLessons = (Integer) request.getAttribute("doneLessons");
    int doingLessons = (Integer) request.getAttribute("doingLessons");
    int notStartedLessons = (Integer) request.getAttribute("notStartedLessons");
    int overallPercent = (Integer) request.getAttribute("overallPercent");
%>
<!doctype html>
<html lang="vi">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Tiến độ học | Gia Sư Lý 9</title>
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
            <h2 class="page-title"><i class="bi bi-journal-check text-primary"></i> Tiến độ học của tôi</h2>
            <div class="page-sub">
                Xem rõ bài nào đã học xong, bài nào đang học và bài nào chưa bắt đầu
            </div>
        </div>

        <!-- ===== Tổng quan ===== -->
        <div class="card mb-4">
            <div class="card-body">
                <div class="d-flex align-items-center gap-4 flex-wrap">
                    <div style="min-width:190px;">
                        <div class="text-muted" style="font-size:.78rem;font-weight:700;">TIẾN ĐỘ TỔNG THỂ</div>
                        <div style="font-size:2.4rem;font-weight:800;line-height:1.1;color:var(--brand-dark);">
                            <%= overallPercent %>%
                        </div>
                        <div class="text-muted" style="font-size:.8rem;">
                            <%= doneLessons %>/<%= totalLessons %> bài giảng đã học
                        </div>
                    </div>
                    <div class="flex-grow-1" style="min-width:220px;">
                        <div class="lesson-progress-bar <%= overallPercent >= 100 ? "done" : "" %>">
                            <span style="width:<%= overallPercent %>%"></span>
                        </div>
                        <div class="d-flex gap-3 flex-wrap mt-3">
                            <span class="badge-soft badge-on">
                                <i class="bi bi-check-circle"></i> Đã học: <%= doneLessons %>
                            </span>
                            <span class="badge-soft badge-medium">
                                <i class="bi bi-hourglass-split"></i> Đang học: <%= doingLessons %>
                            </span>
                            <span class="badge-soft">
                                <i class="bi bi-circle"></i> Chưa học: <%= notStartedLessons %>
                            </span>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- ===== Chủ đề nào đã học / chưa học ===== -->
        <div class="card mb-4">
            <div class="card-head">
                <h5 class="card-title"><i class="bi bi-folder2"></i> Tiến độ theo chủ đề</h5>
            </div>
            <div class="card-body">
                <%
                    if (topicLearnings.isEmpty()) {
                %>
                    <div class="empty-state py-4">
                        <i class="bi bi-folder"></i>
                        <p class="mb-0">Chưa có chủ đề nào.</p>
                    </div>
                <% } else {
                    for (TopicLearning tl : topicLearnings) {
                        pageContext.setAttribute("tl", tl);
                        String status = tl.getStatus();
                        String badge = "DONE".equals(status) ? "badge-on"
                                : ("DOING".equals(status) ? "badge-medium" : "badge-off");
                        String badgeText = "DONE".equals(status) ? "Đã học xong"
                                : ("DOING".equals(status) ? "Đang học" : "Chưa học");
                %>
                <a class="topic-row" href="${pageContext.request.contextPath}/student/lessons?topicId=<%= tl.getTopicId() %>">
                    <div class="d-flex align-items-center gap-3 flex-grow-1 flex-wrap">
                        <div class="topic-thumb" style="width:52px;height:52px;">
                            <% if (tl.getTopicImage() != null && !tl.getTopicImage().trim().isEmpty()) { %>
                                <img src="${pageContext.request.contextPath}${tl.topicImage}" alt="${tl.topicName}">
                            <% } else { %>
                                <i class="bi bi-atom"></i>
                            <% } %>
                        </div>
                        <div style="min-width:180px;">
                            <div class="topic-name">${tl.topicName}</div>
                            <div class="text-muted" style="font-size:.8rem;">
                                <%= tl.getDoneLessons() %>/<%= tl.getTotalLessons() %> bài đã học
                                <% if (tl.getInProgressLessons() > 0) { %>
                                    · <%= tl.getInProgressLessons() %> đang học
                                <% } %>
                            </div>
                        </div>
                        <div class="flex-grow-1" style="min-width:140px;">
                            <div class="lesson-progress-bar <%= tl.isFullyDone() ? "done" : "" %>">
                                <span style="width:<%= tl.getPercent() %>%"></span>
                            </div>
                        </div>
                        <span class="badge-soft <%= badge %>"><%= badgeText %></span>
                        <strong style="font-size:.9rem;min-width:44px;text-align:right;"><%= tl.getPercent() %>%</strong>
                    </div>
                </a>
                <% } } %>
            </div>
        </div>

        <!-- ===== Danh sách chi tiết bài giảng ===== -->
        <%
            if (lessons == null || lessons.isEmpty()) {
        %>
            <div class="card">
                <div class="empty-state">
                    <i class="bi bi-collection-play"></i>
                    <h5>Chưa có bài giảng nào</h5>
                    <p class="mb-0">Giáo viên sẽ thêm bài giảng video và tài liệu cho chủ đề này.</p>
                </div>
            </div>
        <% } else { %>
            <div class="card">
                <div class="card-head">
                    <h5 class="card-title"><i class="bi bi-list-check"></i> Chi tiết từng bài giảng</h5>
                    <div class="d-flex gap-2 flex-wrap">
                        <span class="badge-soft badge-on"><i class="bi bi-check-circle"></i> Đã học</span>
                        <span class="badge-soft badge-medium"><i class="bi bi-hourglass-split"></i> Đang học</span>
                        <span class="badge-soft badge-off"><i class="bi bi-circle"></i> Chưa học</span>
                    </div>
                </div>
                <div class="table-wrap">
                    <table class="table">
                        <thead>
                        <tr>
                            <th style="width:44px;">#</th>
                            <th>Bài giảng</th>
                            <th style="width:180px;">Chủ đề</th>
                            <th style="width:120px;">Loại</th>
                            <th style="width:170px;">Tiến độ</th>
                            <th style="width:120px;">Trạng thái</th>
                            <th style="width:110px;" class="text-end">Thao tác</th>
                        </tr>
                        </thead>
                        <tbody>
                        <%
                            int i = 1;
                            for (Lesson l : lessons) {
                                pageContext.setAttribute("l", l);
                                LessonProgress lp = progressMap.get(l.getLessonId());
                                int pct = lp != null ? lp.getPercent() : 0;
                                boolean done = lp != null && (lp.isCompleted() || pct >= 90);
                                boolean doing = !done && pct > 0;
                                String statusText = done ? "Đã học" : (doing ? "Đang học" : "Chưa học");
                                String statusBadge = done ? "badge-on" : (doing ? "badge-medium" : "badge-off");
                                String typeIcon = "VIDEO_YOUTUBE".equals(l.getLessonType()) ? "bi-youtube"
                                        : ("VIDEO_FILE".equals(l.getLessonType()) ? "bi-camera-video"
                                        : ("WEBSITE".equals(l.getLessonType()) ? "bi-globe" : "bi-file-text"));
                                String typeText = "VIDEO_YOUTUBE".equals(l.getLessonType()) ? "YouTube"
                                        : ("VIDEO_FILE".equals(l.getLessonType()) ? "Video"
                                        : ("WEBSITE".equals(l.getLessonType()) ? "Website" : "Bài đọc"));
                        %>
                        <tr>
                            <td class="text-muted" style="font-size:.83rem;"><%= i %></td>
                            <td>
                                <div style="font-size:.88rem;font-weight:600;line-height:1.45;">${l.title}</div>
                                <% if (l.getCreatedByName() != null) { %>
                                    <div class="text-muted" style="font-size:.77rem;">
                                        <i class="bi bi-person-video3"></i> ${l.createdByName}
                                        <% if (l.getDurationMinutes() > 0) { %>
                                            · <i class="bi bi-clock"></i> ${l.durationMinutes} phút
                                        <% } %>
                                    </div>
                                <% } %>
                            </td>
                            <td class="text-muted" style="font-size:.8rem;">${l.topicName}</td>
                            <td>
                                <span class="badge-soft">
                                    <i class="bi <%= typeIcon %>"></i> <%= typeText %>
                                </span>
                            </td>
                            <td>
                                <div class="lesson-progress-bar <%= done ? "done" : "" %>">
                                    <span style="width:<%= pct %>%"></span>
                                </div>
                                <div class="text-muted" style="font-size:.75rem;margin-top:4px;">
                                    <%= pct %>%<%= done ? " - Hoàn thành" : "" %>
                                </div>
                            </td>
                            <td>
                                <span class="badge-soft <%= statusBadge %>">
                                    <% if (done) { %><i class="bi bi-check-circle"></i>
                                    <% } else if (doing) { %><i class="bi bi-hourglass-split"></i>
                                    <% } else { %><i class="bi bi-circle"></i><% } %>
                                    <%= statusText %>
                                </span>
                            </td>
                            <td class="text-end">
                                <a href="${pageContext.request.contextPath}/student/lessons?topicId=${l.topicId}"
                                   class="btn btn-sm btn-outline-primary" title="Học bài này">
                                    <% if (done) { %>
                                        <i class="bi bi-arrow-repeat"></i> Xem lại
                                    <% } else { %>
                                        <i class="bi bi-play-fill"></i> Học
                                    <% } %>
                                </a>
                            </td>
                        </tr>
                        <% i++; } %>
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
