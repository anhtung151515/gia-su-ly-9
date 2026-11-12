<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.giasuly9.model.Subject" %>
<%@ page import="com.giasuly9.model.Topic" %>
<!doctype html>
<html lang="vi">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Thêm chủ đề | Gia Sư Lý 9</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Manrope:wght@300;400;600;700;800&display=swap" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/main.css" rel="stylesheet">
</head>
<body>
<div class="app-shell">
    <%@ include file="/views/common/_sidebar.jspf" %>
    <div class="app-main">
        <% request.setAttribute("pageTitle", "Chủ đề môn học"); %>
        <%@ include file="/views/common/_topbar.jspf" %>

        <div class="app-content" style="max-width:820px;">
            <%
                com.giasuly9.model.Topic t = (com.giasuly9.model.Topic) request.getAttribute("topic");
                boolean isEdit = (t != null);
            %>
            <div class="page-head">
                <h2 class="page-title"><%= isEdit ? "Cập nhật chủ đề" : "Thêm chủ đề mới" %></h2>
                <div class="page-sub">Chủ đề thuộc một môn học, dùng để nhóm bài giảng và câu hỏi</div>
            </div>

            <div class="card">
                <div class="card-body p-4">
                    <form method="post" action="${pageContext.request.contextPath}/teacher/topic-save">
                        <% if (isEdit) { %>
                            <input type="hidden" name="id" value="${t.topicId}">
                        <% } %>

                        <div class="row g-3">
                            <div class="col-md-8">
                                <label class="form-label">Môn học *</label>
                                <select name="subjectId" class="form-select" required>
                                    <option value="">-- Chọn môn học --</option>
                                    <%
                                        List<Subject> subjects = (List<Subject>) request.getAttribute("subjects");
                                        if (subjects != null) {
                                            for (Subject s : subjects) {
                                                pageContext.setAttribute("s", s);

                                    %>
                                    <option value="${s.subjectId}" <%= isEdit && t.getSubjectId() == s.getSubjectId() ? "selected" : "" %>>
                                        ${s.name}
                                    </option>
                                    <% } } %>
                                </select>
                            </div>
                            <div class="col-md-4">
                                <label class="form-label">Thứ tự</label>
                                <input type="number" name="orderIndex" class="form-control" min="0"
                                       value="<%= isEdit ? t.getOrderIndex() : 0 %>">
                            </div>
                            <div class="col-12">
                                <label class="form-label">Tên chủ đề *</label>
                                <input type="text" name="name" class="form-control" required
                                       placeholder="VD: Điện và mạch điện"
                                       value="<%= isEdit ? t.getName() : "" %>">
                            </div>
                            <div class="col-12">
                                <label class="form-label">Mô tả</label>
                                <textarea name="description" class="form-control" rows="3"
                                          placeholder="Nội dung chính của chủ đề..."><%= isEdit && t.getDescription() != null ? t.getDescription() : "" %></textarea>
                            </div>
                        </div>

                        <div class="d-flex gap-2 mt-4">
                            <button type="submit" class="btn btn-primary">
                                <i class="bi bi-check-lg"></i> <%= isEdit ? "Cập nhật" : "Thêm chủ đề" %>
                            </button>
                            <a href="${pageContext.request.contextPath}/teacher/topics" class="btn btn-outline-secondary">Hủy</a>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
