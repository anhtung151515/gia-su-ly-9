<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.giasuly9.model.User" %>
<!doctype html>
<html lang="vi">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Tài khoản | Gia Sư Lý 9</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Manrope:wght@300;400;600;700;800&display=swap" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/main.css" rel="stylesheet">
</head>
<body>
<div class="app-shell">
    <%@ include file="/views/common/_sidebar.jspf" %>
    <div class="app-main">
        <% request.setAttribute("pageTitle", "Tài khoản người dùng"); %>
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
                    <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                </div>
                <% session.removeAttribute("generatedPassword"); %>
            <% } %>

            <%
                User user = (User) request.getAttribute("user");
                boolean isEdit = user != null;
            %>

            <div class="page-head d-flex justify-content-between align-items-end flex-wrap gap-2">
                <div>
                    <h2 class="page-title"><%= isEdit ? "Sửa tài khoản" : "Thêm tài khoản" %></h2>
                    <div class="page-sub">
                        <%= isEdit ? "Cập nhật thông tin cho " + user.getUsername() : "Tạo tài khoản mới cho hệ thống" %>
                    </div>
                </div>
                <a href="${pageContext.request.contextPath}/admin/users" class="btn btn-sm btn-outline-primary">
                    <i class="bi bi-arrow-left"></i> Quay lại
                </a>
            </div>

            <form method="post" action="${pageContext.request.contextPath}/admin/user-save">
                <input type="hidden" name="id" value="<%= isEdit ? user.getUserId() : 0 %>">

                <div class="row g-3">
                    <div class="col-lg-8">
                        <div class="card">
                            <div class="card-head">
                                <h5 class="card-title">Thông tin tài khoản</h5>
                            </div>
                            <div class="card-body">
                                <div class="row g-3">
                                    <div class="col-md-6">
                                        <label class="form-label">Tên đăng nhập <span class="text-danger">*</span></label>
                                        <input type="text" name="username" class="form-control"
                                               value="<%= isEdit ? user.getUsername() : "" %>"
                                               <%= isEdit ? "readonly" : "" %> placeholder="vidu: hocsinh01">
                                        <% if (isEdit) { %>
                                            <div class="form-hint">Tên đăng nhập không thể đổi sau khi tạo.</div>
                                        <% } %>
                                    </div>
                                    <div class="col-md-6">
                                        <label class="form-label">Họ và tên <span class="text-danger">*</span></label>
                                        <input type="text" name="fullName" class="form-control"
                                               value="<%= isEdit ? user.getFullName() : "" %>" placeholder="Nguyễn Văn A">
                                    </div>
                                    <div class="col-md-6">
                                        <label class="form-label">Email</label>
                                        <input type="email" name="email" class="form-control"
                                               value="<%= isEdit && user.getEmail() != null ? user.getEmail() : "" %>"
                                               placeholder="email@example.com">
                                    </div>
                                    <div class="col-md-6">
                                        <label class="form-label">Số điện thoại</label>
                                        <input type="text" name="phone" class="form-control"
                                               value="<%= isEdit && user.getPhone() != null ? user.getPhone() : "" %>"
                                               placeholder="09xx xxx xxx">
                                    </div>
                                    <div class="col-md-6">
                                        <label class="form-label">Vai trò <span class="text-danger">*</span></label>
                                        <select name="role" class="form-select">
                                            <option value="STUDENT" <%= isEdit && "STUDENT".equals(user.getRole()) ? "selected" : "" %>>Học sinh</option>
                                            <option value="TEACHER" <%= isEdit && "TEACHER".equals(user.getRole()) ? "selected" : "" %>>Giáo viên</option>
                                            <option value="ADMIN" <%= isEdit && "ADMIN".equals(user.getRole()) ? "selected" : "" %>>Quản trị viên</option>
                                        </select>
                                    </div>
                                    <div class="col-md-6">
                                        <label class="form-label">Lớp</label>
                                        <input type="text" name="className" class="form-control"
                                               value="<%= isEdit && user.getClassName() != null ? user.getClassName() : "" %>"
                                               placeholder="VD: 9A1">
                                        <div class="form-hint">Chỉ dùng cho học sinh.</div>
                                    </div>
                                    <div class="col-12">
                                        <label class="form-label">
                                            Mật khẩu <%= isEdit ? "(để trống nếu không đổi)" : "(để trống để hệ thống tạo tự động)" %>
                                        </label>
                                        <input type="password" name="password" class="form-control"
                                               autocomplete="new-password" placeholder="Tối thiểu 6 ký tự">
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="col-lg-4">
                        <div class="card">
                            <div class="card-head">
                                <h5 class="card-title">Thông tin thêm</h5>
                            </div>
                            <div class="card-body">
                                <% if (isEdit) { %>
                                    <div class="mb-3">
                                        <div class="stat-label">Trạng thái</div>
                                        <span class="badge-soft <%= user.isActive() ? "badge-on" : "badge-off" %>">
                                            <%= user.isActive() ? "Hoạt động" : "Đã khoá" %>
                                        </span>
                                    </div>
                                    <div class="mb-3">
                                        <div class="stat-label">Ngày tạo</div>
                                        <div style="font-size:.88rem;">
                                            <%= user.getCreatedAt() == null ? "—" : user.getCreatedAt() %>
                                        </div>
                                    </div>
                                <% } %>
                                <button type="submit" class="btn btn-primary w-100">
                                    <i class="bi bi-check-lg"></i>
                                    <%= isEdit ? "Lưu thay đổi" : "Tạo tài khoản" %>
                                </button>
                                <a href="${pageContext.request.contextPath}/admin/users"
                                   class="btn btn-outline-secondary w-100 mt-2">Huỷ</a>
                            </div>
                        </div>
                    </div>
                </div>
            </form>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
