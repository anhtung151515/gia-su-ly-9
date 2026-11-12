<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!doctype html>
<html lang="vi">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Đăng ký | Gia Sư Lý</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Manrope:wght@400;600;700;800&display=swap" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/main.css" rel="stylesheet">
</head>
<body>
<nav class="navbar navbar-expand-lg site-navbar">
    <div class="container">
        <a class="navbar-brand brand" href="${pageContext.request.contextPath}/home">
            <img src="${pageContext.request.contextPath}/assets/img/logo.svg" alt="Gia Sư Lý">
            Gia&nbsp;Sư&nbsp;<em>Lý</em>
        </a>
        <div class="d-flex gap-2 align-items-center ms-auto">
            <a class="nav-phone d-none d-md-inline-flex" href="tel:19001234">
                <i class="bi bi-telephone-fill"></i> 1900 1234
            </a>
            <a class="btn btn-nav btn-nav-cta" href="${pageContext.request.contextPath}/home">Về trang chủ</a>
        </div>
    </div>
</nav>

<main class="auth-split">
    <!-- Visual -->
    <section class="auth-side">
        <span class="tag" style="background:rgba(255,255,255,.18);color:#fff;">
            <i class="bi bi-stars"></i> Tham gia cùng hàng nghìn học sinh lớp 9
        </span>
        <h2 class="mt-3">Tạo tài khoản miễn phí,<br>học Vật lý không còn sợ.</h2>
        <p class="mb-4">Chỉ mất 30 giây — bạn có ngay phòng luyện trắc nghiệm, tự luận và gia sư AI giải từng bước.</p>

        <div class="auth-feature">
            <i class="bi bi-play-btn"></i>
            <div>Bài giảng video YouTube, tài liệu text và link học tập theo từng chủ đề.</div>
        </div>
        <div class="auth-feature">
            <i class="bi bi-patch-question"></i>
            <div>Ngân hàng câu hỏi trắc nghiệm &amp; tự luận bám sát chương trình Vật lý 9.</div>
        </div>
        <div class="auth-feature">
            <i class="bi bi-signpost-split"></i>
            <div>AI dừng lại chờ bạn nhập số liệu, rồi mới giải tiếp từng bước.</div>
        </div>
        <div class="auth-feature">
            <i class="bi bi-camera"></i>
            <div>Upload ảnh bài làm để AI chấm điểm và chỉ ra lỗi sai cụ thể.</div>
        </div>
    </section>

    <!-- Form -->
    <section class="auth-form-side">
        <div class="auth-box" style="margin:0;width:100%;max-width:430px;">
            <h2 class="fw-bold mb-1" style="font-size:1.5rem;color:var(--brand);">Đăng Ký</h2>
            <p class="text-muted mb-4" style="font-size:.88rem;">Tạo tài khoản học sinh miễn phí</p>

            <% if (request.getAttribute("error") != null) { %>
                <div class="alert alert-danger"><i class="bi bi-exclamation-circle me-1"></i> <%= request.getAttribute("error") %></div>
            <% } %>

            <form method="post" action="${pageContext.request.contextPath}/register">
                <div class="field">
                    <label for="fullName">Họ và tên *</label>
                    <input type="text" id="fullName" name="fullName" placeholder="VD: Nguyễn Văn An"
                           value="<%= request.getAttribute("fullNameVal") != null ? request.getAttribute("fullNameVal") : "" %>" required autofocus>
                </div>
                <div class="field">
                    <label for="username">Tên đăng nhập *</label>
                    <input type="text" id="username" name="username" placeholder="VD: an.nguyen9 (từ 4 ký tự)"
                           value="<%= request.getAttribute("usernameVal") != null ? request.getAttribute("usernameVal") : "" %>" required>
                    <div class="form-hint">Chỉ dùng chữ cái, số, dấu gạch dưới và dấu chấm.</div>
                </div>
                <div class="row g-2">
                    <div class="col-md-6">
                        <div class="field">
                            <label for="password">Mật khẩu *</label>
                            <input type="password" id="password" name="password" placeholder="Ít nhất 6 ký tự" required>
                        </div>
                    </div>
                    <div class="col-md-6">
                        <div class="field">
                            <label for="confirmPassword">Nhập lại mật khẩu *</label>
                            <input type="password" id="confirmPassword" name="confirmPassword" placeholder="Nhập lại mật khẩu" required>
                        </div>
                    </div>
                </div>
                <div class="row g-2">
                    <div class="col-md-6">
                        <div class="field">
                            <label for="className">Lớp</label>
                            <input type="text" id="className" name="className" placeholder="VD: 9A1"
                                   value="<%= request.getAttribute("classNameVal") != null ? request.getAttribute("classNameVal") : "" %>">
                        </div>
                    </div>
                    <div class="col-md-6">
                        <div class="field">
                            <label for="phone">Số điện thoại</label>
                            <input type="tel" id="phone" name="phone" placeholder="VD: 0912345678"
                                   value="<%= request.getAttribute("phoneVal") != null ? request.getAttribute("phoneVal") : "" %>">
                        </div>
                    </div>
                </div>
                <div class="field">
                    <label for="email">Email</label>
                    <input type="email" id="email" name="email" placeholder="VD: an.nguyen@gmail.com"
                           value="<%= request.getAttribute("emailVal") != null ? request.getAttribute("emailVal") : "" %>">
                </div>
                <button type="submit" class="btn btn-cta w-100" style="padding:.58rem;">
                    <i class="bi bi-person-plus"></i> Tạo tài khoản
                </button>
            </form>

            <p class="text-center text-muted mt-3 mb-0" style="font-size:.82rem;">
                Đã có tài khoản? <a href="${pageContext.request.contextPath}/login" style="font-weight:700;">Đăng nhập ngay</a>
            </p>
        </div>
    </section>
</main>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
