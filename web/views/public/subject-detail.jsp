<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.giasuly9.model.Subject" %>
<%@ page import="com.giasuly9.model.Topic" %>
<!doctype html>
<html lang="vi">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>${subject.name} | Gia Sư Lý 9</title>
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
                <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/subjects">Môn học</a></li>
                <li class="breadcrumb-item active">${subject.name}</li>
            </ol>
        </nav>

        <div class="card mb-4 overflow-hidden">
            <div class="row g-0 align-items-center">
                <div class="col-md-3">
                    <div class="subject-cover" style="height:100%;min-height:160px;border-radius:0;">
                        <% if (subject.imageUrl != null && !subject.imageUrl.trim().isEmpty()) { %>
                            <img src="${subject.imageUrl}" alt="${subject.name}" style="height:100%;min-height:160px;">
                        <% } else { %>
                            <i class="bi bi-atom"></i>
                        <% } %>
                    </div>
                </div>
                <div class="col-md-9">
                    <div class="card-body p-4">
                        <h1 class="page-title mb-2">${subject.name}</h1>
                        <p class="text-muted mb-3">${subject.description}</p>
                        <div class="d-flex gap-3 flex-wrap" style="font-size:.87rem;color:var(--muted);">
                            <span><i class="bi bi-person-badge"></i> ${subject.teacherName == null ? "Chưa phân công giáo viên" : subject.teacherName}</span>
                            <span><i class="bi bi-list-task"></i> <%= ((List<Topic>) request.getAttribute("topics")).size() %> chủ đề</span>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div class="page-head">
            <h2 class="page-title" style="font-size:1.3rem;">Danh sách chủ đề</h2>
            <div class="page-sub">Chọn chủ đề để xem bài giảng và luyện tập</div>
        </div>

        <%
            List<Topic> topics = (List<Topic>) request.getAttribute("topics");
            if (topics == null || topics.isEmpty()) {
        %>
            <div class="card">
                <div class="empty-state">
                    <i class="bi bi-folder2-open"></i>
                    <h5>Chưa có chủ đề</h5>
                    <p class="mb-0">Giáo viên sẽ thêm chủ đề và bài giảng cho môn học này.</p>
                </div>
            </div>
        <% } else { %>
            <%
                int idx = 1;
                for (Topic t : topics) {
                    pageContext.setAttribute("t", t);

            %>
            <div class="topic-row">
                <div class="d-flex align-items-center gap-3 flex-grow-1">
                            <div class="topic-thumb">
                                <% if (t.getImageUrl() != null && !t.getImageUrl().trim().isEmpty()) { %>
                                    <img src="${pageContext.request.contextPath}${t.imageUrl}" alt="${t.name}">
                                <% } else { %>
                                    <i class="bi bi-atom"></i>
                                <% } %>
                            </div>
                    <div class="topic-index"><%= idx %></div>
                    <div>
                        <div class="topic-name">${t.name}</div>
                        <% if (t.getDescription() != null && !t.getDescription().trim().isEmpty()) { %>
                            <div class="text-muted" style="font-size:.83rem;">${t.description}</div>
                        <% } %>
                    </div>
                </div>
                <div class="d-flex align-items-center gap-2">
                    <span class="badge-soft badge-mcq"><i class="bi bi-collection-play"></i> ${t.lessonCount} bài</span>
                    <a class="btn btn-sm btn-outline-primary"
                       href="${pageContext.request.contextPath}/student/lessons?topicId=${t.topicId}&subjectId=${subject.subjectId}">
                        Xem bài <i class="bi bi-arrow-right"></i>
                    </a>
                    <a class="btn btn-sm btn-primary"
                       href="${pageContext.request.contextPath}/student/practice-setup?topicId=${t.topicId}&type=MCQ">
                        <i class="bi bi-pencil-square"></i> Luyện tập
                    </a>
                </div>
            </div>
            <% idx++; } %>
        <% } %>
    </div>
</section>

<%@ include file="/views/common/_footer.jspf" %>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
