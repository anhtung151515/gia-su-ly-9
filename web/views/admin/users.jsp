<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.giasuly9.model.User" %>
<!doctype html>
<html lang="vi">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Người dùng | Gia Sư Lý 9</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Manrope:wght@300;400;600;700;800&display=swap" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/main.css" rel="stylesheet">
</head>
<body>
<div class="app-shell">
    <%@ include file="/views/common/_sidebar.jspf" %>
    <div class="app-main">
        <% request.setAttribute("pageTitle", "Người dùng"); %>
        <%@ include file="/views/common/_topbar.jspf" %>

        <div class="app-content">
            <% if (session.getAttribute("flash") != null) { %>
                <div class="alert alert-success alert-dismissible fade show">
                    <i class="bi bi-check-circle"></i> <%= session.getAttribute("flash") %>
                    <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                </div>
                <% session.removeAttribute("flash"); %>
            <% } %>

            <% if (session.getAttribute("generatedPassword") != null) { %>
                <div class="alert alert-warning alert-dismissible fade show">
                    <i class="bi bi-key"></i>
                    Mật khẩu được tạo tự động: <strong><%= session.getAttribute("generatedPassword") %></strong>
                    — hãy gửi cho người dùng và nhắc họ đổi sau lần đăng nhập đầu.
                    <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                </div>
                <% session.removeAttribute("generatedPassword"); %>
            <% } %>

            <div class="page-head d-flex justify-content-between align-items-end flex-wrap gap-2">
                <div>
                    <h2 class="page-title">Quản lý người dùng</h2>
                    <div class="page-sub">Tài khoản quản trị viên, giáo viên và học sinh</div>
                </div>
                <a href="${pageContext.request.contextPath}/admin/user-form" class="btn btn-primary btn-sm">
                    <i class="bi bi-plus-lg"></i> Thêm người dùng
                </a>
            </div>

            <%
                String selectedRole = (String) request.getAttribute("selectedRole");
            %>
            <div class="pill-nav mb-3">
                <a class="pill <%= (selectedRole == null || selectedRole.isEmpty()) ? "active" : "" %>"
                   href="${pageContext.request.contextPath}/admin/users">Tất cả</a>
                <a class="pill <%= "ADMIN".equals(selectedRole) ? "active" : "" %>"
                   href="${pageContext.request.contextPath}/admin/users?role=ADMIN">Quản trị</a>
                <a class="pill <%= "TEACHER".equals(selectedRole) ? "active" : "" %>"
                   href="${pageContext.request.contextPath}/admin/users?role=TEACHER">Giáo viên</a>
                <a class="pill <%= "STUDENT".equals(selectedRole) ? "active" : "" %>"
                   href="${pageContext.request.contextPath}/admin/users?role=STUDENT">Học sinh</a>
            </div>

            <%
                List<User> users = (List<User>) request.getAttribute("users");
                if (users == null || users.isEmpty()) {
            %>
                <div class="card">
                    <div class="empty-state">
                        <i class="bi bi-people"></i>
                        <h5>Không có người dùng nào</h5>
                        <a href="${pageContext.request.contextPath}/admin/user-form" class="btn btn-sm btn-primary mt-2">
                            Thêm người dùng
                        </a>
                    </div>
                </div>
            <% } else { %>
                <div class="card">
                    <div class="table-wrap">
                        <table class="table">
                            <thead>
                            <tr>
                                <th style="width:50px;">#</th>
                                <th>Họ tên</th>
                                <th>Tên đăng nhập</th>
                                <th>Vai trò</th>
                                <th>Liên hệ</th>
                                <th>Trạng thái</th>
                                <th class="text-end">Thao tác</th>
                            </tr>
                            </thead>
                            <tbody>
                            <%
                                int i = 1;
                                for (User u : users) {
                                    pageContext.setAttribute("u", u);

                            %>
                            <tr>
                                <td class="text-muted" style="font-size:.83rem;"><%= i %></td>
                                <td>
                                    <div class="fw-bold" style="font-size:.9rem;">${u.fullName}</div>
                                    <% if (u.getClassName() != null && !u.getClassName().trim().isEmpty()) { %>
                                        <div class="text-muted" style="font-size:.78rem;">Lớp ${u.className}</div>
                                    <% } %>
                                </td>
                                <td class="text-muted" style="font-size:.85rem;">${u.username}</td>
                                <td>
                                    <span class="badge-soft <%= "ADMIN".equals(u.getRole()) ? "badge-hard"
                                            : ("TEACHER".equals(u.getRole()) ? "badge-teacher" : "badge-mcq") %>">
                                        <%= "ADMIN".equals(u.getRole()) ? "Quản trị"
                                            : ("TEACHER".equals(u.getRole()) ? "Giáo viên" : "Học sinh") %>
                                    </span>
                                </td>
                                <td class="text-muted" style="font-size:.82rem;">
                                    <% if (u.getEmail() != null && !u.getEmail().trim().isEmpty()) { %>
                                        <div><i class="bi bi-envelope"></i> ${u.email}</div>
                                    <% } %>
                                    <% if (u.getPhone() != null && !u.getPhone().trim().isEmpty()) { %>
                                        <div><i class="bi bi-telephone"></i> ${u.phone}</div>
                                    <% } %>
                                </td>
                                <td>
                                    <span class="badge-soft <%= u.isActive() ? "badge-on" : "badge-off" %>">
                                        <%= u.isActive() ? "Hoạt động" : "Đã khoá" %>
                                    </span>
                                </td>
                                <td class="text-end">
                                    <div class="d-flex gap-1 justify-content-end">
                                        <a href="${pageContext.request.contextPath}/admin/user-form?id=${u.userId}"
                                           class="btn btn-sm btn-outline-primary" title="Sửa"><i class="bi bi-pencil"></i></a>
                                        <a href="${pageContext.request.contextPath}/admin/user-reset-password?id=${u.userId}"
                                           class="btn btn-sm btn-outline-warning" title="Đặt lại mật khẩu"
                                           onclick="return confirm('Đặt lại mật khẩu cho tài khoản này?');">
                                            <i class="bi bi-key"></i>
                                        </a>
                                        <a href="${pageContext.request.contextPath}/admin/user-toggle?id=${u.userId}"
                                           class="btn btn-sm btn-outline-secondary" title="Bật / khoá">
                                            <i class="bi <%= u.isActive() ? "bi-toggle-on" : "bi-toggle-off" %>"></i>
                                        </a>
                                        <a href="${pageContext.request.contextPath}/admin/user-delete?id=${u.userId}"
                                           class="btn btn-sm btn-outline-danger" title="Xoá"
                                           onclick="return confirm('Xoá tài khoản này?');"><i class="bi bi-trash"></i></a>
                                    </div>
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

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
