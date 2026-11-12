<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.giasuly9.model.Topic" %>
<!doctype html>
<html lang="vi">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Chủ đề Vật lý 9 | Gia Sư Lý</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Manrope:wght@300;400;600;700;800&display=swap" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/main.css" rel="stylesheet">
</head>
<body>
<%@ include file="/views/common/_header.jspf" %>

<section class="page">
    <div class="container">
        <nav aria-label="breadcrumb">
            <ol class="breadcrumb" style="font-size:.86rem;">
                <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/home">Trang chủ</a></li>
                <li class="breadcrumb-item active">Chủ đề Vật lý 9</li>
            </ol>
        </nav>

        <div class="page-head d-flex justify-content-between align-items-end flex-wrap gap-3">
            <div>
                <h2 class="page-title">Chủ đề Vật lý 9</h2>
                <div class="page-sub">Toàn bộ chương trình Vật lý lớp 9 — chọn chủ đề để xem bài giảng và luyện tập</div>
            </div>
            <a href="${pageContext.request.contextPath}/teachers" class="btn btn-sm btn-outline-primary">
                <i class="bi bi-person-video3"></i> Xem giáo viên
            </a>
        </div>

        <%
            List<Topic> topics = (List<Topic>) request.getAttribute("topics");
            String keyword = (String) request.getAttribute("keyword");
            if (keyword == null) { keyword = ""; }
        %>

        <div class="card mb-4">
            <div class="card-body">
                <form method="get" action="${pageContext.request.contextPath}/topics" class="d-flex gap-2">
                    <input type="text" name="q" class="form-control" value="<%= keyword %>"
                           placeholder="Tìm chủ đề (cơ năng, ánh sáng, điện từ...)">
                    <button type="submit" class="btn btn-primary">
                        <i class="bi bi-search"></i> Tìm
                    </button>
                    <% if (!keyword.isEmpty()) { %>
                        <a href="${pageContext.request.contextPath}/topics" class="btn btn-outline-secondary">Xoá</a>
                    <% } %>
                </form>
            </div>
        </div>

        <% if (topics == null || topics.isEmpty()) { %>
            <div class="card">
                <div class="empty-state">
                    <i class="bi bi-folder2-open"></i>
                    <h5>Không tìm thấy chủ đề</h5>
                    <p class="mb-0">
                        <% if (!keyword.isEmpty()) { %>
                            Không có chủ đề nào khớp với "<%= keyword %>".
                        <% } else { %>
                            Giáo viên sẽ thêm chủ đề và bài giảng cho môn Vật lý 9.
                        <% } %>
                    </p>
                </div>
            </div>
        <% } else { %>
            <div class="row g-3">
                <%
                    int idx = 0;
                    for (Topic t : topics) {
                        idx++;
                        pageContext.setAttribute("t", t);
                %>
                <div class="col-md-6 col-lg-4">
                    <div class="topic-card">
                        <div class="topic-card-cover">
                            <% if (t.getImageUrl() != null && !t.getImageUrl().trim().isEmpty()) { %>
                                <img src="${pageContext.request.contextPath}${t.imageUrl}" alt="${t.name}">
                            <% } else { %>
                                <i class="bi bi-atom"></i>
                            <% } %>
                            <span class="topic-card-num"><%= idx %></span>
                        </div>                        <div class="topic-card-body">
                            <div class="topic-card-head">
                                <span class="badge-soft badge-mcq"><i class="bi bi-collection-play"></i> ${t.lessonCount} bài</span>
                            </div>
                            <h5 class="topic-card-title">${t.name}</h5>
                            <% if (t.getDescription() != null && !t.getDescription().trim().isEmpty()) { %>
                                <p class="topic-card-desc">
                                    <%= t.getDescription().length() > 130 ? t.getDescription().substring(0, 130) + "..." : t.getDescription() %>
                                </p>
                            <% } %>
                            <div class="topic-card-actions">
                                <a class="btn btn-sm btn-primary"
                                   href="${pageContext.request.contextPath}/student/lessons?topicId=${t.topicId}">
                                    <i class="bi bi-play-btn"></i> Bài giảng
                                </a>
                                <a class="btn btn-sm btn-outline-primary"
                                   href="${pageContext.request.contextPath}/student/practice-setup?topicId=${t.topicId}&type=MCQ">
                                    <i class="bi bi-pencil-square"></i> Luyện tập
                                </a>
                            </div>
                        </div>
                    </div>
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
