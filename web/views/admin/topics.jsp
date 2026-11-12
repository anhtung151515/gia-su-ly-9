<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.giasuly9.model.Subject" %>
<%@ page import="com.giasuly9.model.Topic" %>
<!doctype html>
<html lang="vi">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Chủ đề | Gia Sư Lý 9</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Manrope:wght@300;400;600;700;800&display=swap" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/main.css" rel="stylesheet">
</head>
<body>
<div class="app-shell">
    <%@ include file="/views/common/_sidebar.jspf" %>
    <div class="app-main">
        <% request.setAttribute("pageTitle", "Chủ đề"); %>
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
                <h2 class="page-title">Quản lý chủ đề</h2>
                <div class="page-sub">Chủ đề nhóm bài giảng và câu hỏi theo từng môn học</div>
            </div>

            <div class="row g-3">
                <div class="col-lg-5">
                    <div class="card">
                        <div class="card-head">
                            <h5 class="card-title"><i class="bi bi-plus-circle"></i> Thêm chủ đề</h5>
                        </div>
                        <div class="card-body">
                            <form method="post" action="${pageContext.request.contextPath}/admin/topic-save">
                                <input type="hidden" name="id" value="0">
                                <div class="mb-3">
                                    <label class="form-label">Môn học <span class="text-danger">*</span></label>
                                    <select name="subjectId" class="form-select" required>
                                        <option value="">— Chọn môn học —</option>
                                        <%
                                            List<Subject> subjects = (List<Subject>) request.getAttribute("subjects");
                                            if (subjects != null) {
                                                for (Subject s : subjects) {
                                                    pageContext.setAttribute("s", s);

                                        %>
                                        <option value="<%= s.getSubjectId() %>"><%= s.getName() %></option>
                                        <% } } %>
                                    </select>
                                </div>
                                <div class="mb-3">
                                    <label class="form-label">Tên chủ đề <span class="text-danger">*</span></label>
                                    <input type="text" name="name" class="form-control" placeholder="VD: Điện và mạch điện">
                                </div>
                                <div class="mb-3">
                                    <label class="form-label">Mô tả</label>
                                    <textarea name="description" class="form-control" rows="3"
                                              placeholder="Nội dung chủ đề bao gồm..."></textarea>
                                </div>
                                <div class="mb-3">
                                    <label class="form-label">Thứ tự hiển thị</label>
                                    <input type="number" name="orderIndex" class="form-control" value="0" min="0">
                                </div>
                                <button type="submit" class="btn btn-primary w-100">
                                    <i class="bi bi-check-lg"></i> Thêm chủ đề
                                </button>
                            </form>
                        </div>
                    </div>
                </div>

                <div class="col-lg-7">
                    <%
                        List<Topic> topics = (List<Topic>) request.getAttribute("topics");
                        if (topics == null || topics.isEmpty()) {
                    %>
                        <div class="card">
                            <div class="empty-state">
                                <i class="bi bi-folder2-open"></i>
                                <h5>Chưa có chủ đề nào</h5>
                            </div>
                        </div>
                    <% } else { %>
                        <div class="card">
                            <div class="table-wrap">
                                <table class="table">
                                    <thead>
                                    <tr>
                                        <th style="width:50px;">TT</th>
                                        <th>Tên chủ đề</th>
                                        <th>Môn học</th>
                                        <th class="text-end">Thao tác</th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    <%
                                        int i = 1;
                                        for (Topic t : topics) {
                                            pageContext.setAttribute("t", t);

                                    %>
                                    <tr>
                                        <td class="text-muted" style="font-size:.83rem;"><%= i %></td>
                                        <td>
                                            <div class="fw-bold" style="font-size:.9rem;">${t.name}</div>
                                            <% if (t.getDescription() != null && !t.getDescription().trim().isEmpty()) { %>
                                                <div class="text-muted" style="font-size:.78rem;">
                                                    <%= t.getDescription().length() > 70 ? t.getDescription().substring(0, 70) + "..." : t.getDescription() %>
                                                </div>
                                            <% } %>
                                        </td>
                                        <td class="text-muted" style="font-size:.85rem;">${t.subjectName}</td>
                                        <td class="text-end">
                                            <a href="${pageContext.request.contextPath}/admin/topic-delete?id=${t.topicId}"
                                               class="btn btn-sm btn-outline-danger"
                                               onclick="return confirm('Xoá chủ đề này?');"><i class="bi bi-trash"></i></a>
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
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
