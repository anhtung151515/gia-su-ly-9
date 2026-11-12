<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.giasuly9.model.Subject" %>
<%@ page import="com.giasuly9.model.User" %>
<!doctype html>
<html lang="vi">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Môn học | Gia Sư Lý 9</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Manrope:wght@300;400;600;700;800&display=swap" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/main.css" rel="stylesheet">
</head>
<body>
<div class="app-shell">
    <%@ include file="/views/common/_sidebar.jspf" %>
    <div class="app-main">
        <% request.setAttribute("pageTitle", "Môn học"); %>
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
                <h2 class="page-title">Quản lý môn học</h2>
                <div class="page-sub">Môn học được giao cho giáo viên phụ trách</div>
            </div>

            <%
                Subject editSubject = (Subject) request.getAttribute("editSubject");
                boolean isEdit = editSubject != null;
            %>

            <div class="row g-3">
                <div class="col-lg-5">
                    <div class="card">
                        <div class="card-head">
                            <h5 class="card-title">
                                <i class="bi bi-plus-circle"></i>
                                <%= isEdit ? "Cập nhật môn học" : "Thêm môn học" %>
                            </h5>
                        </div>
                        <div class="card-body">
                            <form method="post" action="${pageContext.request.contextPath}/admin/subject-save">
                                <input type="hidden" name="id" value="<%= isEdit ? editSubject.getSubjectId() : 0 %>">
                                <div class="mb-3">
                                    <label class="form-label">Tên môn học <span class="text-danger">*</span></label>
                                    <input type="text" name="name" class="form-control"
                                           value="<%= isEdit ? editSubject.getName() : "" %>"
                                           placeholder="VD: Vật lý lớp 9">
                                </div>
                                <div class="row g-3">
                                    <div class="col-6">
                                        <label class="form-label">Mã môn</label>
                                        <input type="text" name="code" class="form-control"
                                               value="<%= isEdit && editSubject.getCode() != null ? editSubject.getCode() : "" %>"
                                               placeholder="LY9">
                                    </div>
                                    <div class="col-6">
                                        <label class="form-label">Ảnh bìa (URL)</label>
                                        <input type="text" name="imageUrl" class="form-control"
                                               value="<%= isEdit && editSubject.getImageUrl() != null ? editSubject.getImageUrl() : "" %>"
                                               placeholder="https://...">
                                    </div>
                                    <div class="col-12">
                                        <label class="form-label">Mô tả</label>
                                        <textarea name="description" class="form-control" rows="3"
                                                  placeholder="Giới thiệu ngắn về môn học..."><%= isEdit && editSubject.getDescription() != null ? editSubject.getDescription() : "" %></textarea>
                                    </div>
                                    <div class="col-12">
                                        <label class="form-label">Giáo viên phụ trách</label>
                                        <select name="teacherId" class="form-select">
                                            <option value="0">— Chưa phân công —</option>
                                            <%
                                                List<User> teachers = (List<User>) request.getAttribute("teachers");
                                                if (teachers != null) {
                                                    for (User t : teachers) {
                                                        pageContext.setAttribute("t", t);

                                            %>
                                            <option value="<%= t.getUserId() %>"
                                                <%= isEdit && editSubject.getTeacherId() == t.getUserId() ? "selected" : "" %>>
                                                <%= t.getFullName() %>
                                            </option>
                                            <% } } %>
                                        </select>
                                    </div>
                                </div>
                                <button type="submit" class="btn btn-primary w-100 mt-3">
                                    <i class="bi bi-check-lg"></i>
                                    <%= isEdit ? "Lưu thay đổi" : "Thêm môn học" %>
                                </button>
                                <% if (isEdit) { %>
                                    <a href="${pageContext.request.contextPath}/admin/subjects"
                                       class="btn btn-outline-secondary w-100 mt-2">Huỷ sửa</a>
                                <% } %>
                            </form>
                        </div>
                    </div>
                </div>

                <div class="col-lg-7">
                    <%
                        List<Subject> subjects = (List<Subject>) request.getAttribute("subjects");
                        if (subjects == null || subjects.isEmpty()) {
                    %>
                        <div class="card">
                            <div class="empty-state">
                                <i class="bi bi-atom"></i>
                                <h5>Chưa có môn học nào</h5>
                            </div>
                        </div>
                    <% } else { %>
                        <div class="card">
                            <div class="table-wrap">
                                <table class="table">
                                    <thead>
                                    <tr>
                                        <th>Môn học</th>
                                        <th>Giáo viên</th>
                                        <th class="text-end">Thao tác</th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    <%
                                        for (Subject s : subjects) {
                                            pageContext.setAttribute("s", s);

                                    %>
                                    <tr>
                                        <td>
                                            <div class="fw-bold" style="font-size:.9rem;">${s.name}</div>
                                            <% if (s.getCode() != null && !s.getCode().trim().isEmpty()) { %>
                                                <span class="badge-soft badge-mcq">${s.code}</span>
                                            <% } %>
                                        </td>
                                        <td class="text-muted" style="font-size:.85rem;">
                                            <%= s.getTeacherName() == null ? "Chưa phân công" : s.getTeacherName() %>
                                        </td>
                                        <td class="text-end">
                                            <div class="d-flex gap-1 justify-content-end">
                                                <a href="${pageContext.request.contextPath}/admin/subjects?edit=${s.subjectId}"
                                                   class="btn btn-sm btn-outline-primary" title="Sửa"><i class="bi bi-pencil"></i></a>
                                                <a href="${pageContext.request.contextPath}/admin/subject-delete?id=${s.subjectId}"
                                                   class="btn btn-sm btn-outline-danger"
                                                   onclick="return confirm('Xoá môn học này? Các chủ đề bên trong cũng bị ảnh hưởng.');">
                                                    <i class="bi bi-trash"></i>
                                                </a>
                                            </div>
                                        </td>
                                    </tr>
                                    <% } %>
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
