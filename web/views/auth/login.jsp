<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!doctype html>
<html lang="vi">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Đăng nhập | Gia Sư Lý 9</title>
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
            <i class="bi bi-stars"></i> Nền tảng học Vật lý lớp 9
        </span>
        <h2 class="mt-3">Gia sư AI giải từng bước,<br>không đưa đáp án sẵn.</h2>
        <p class="mb-4">Học sinh tự tính, tự nhập số liệu — AI chỉ gợi ý đúng lúc cần, giống buổi học 1-1 với thầy giáo.</p>

        <div class="auth-feature">
            <i class="bi bi-play-btn"></i>
            <div>Bài giảng video YouTube, tài liệu text và link học tập — quản lý tập trung.</div>
        </div>
        <div class="auth-feature">
            <i class="bi bi-patch-question"></i>
            <div>Ngân hàng câu hỏi trắc nghiệm &amp; tự luận, AI sinh tự động theo chủ đề.</div>
        </div>
        <div class="auth-feature">
            <i class="bi bi-signpost-split"></i>
            <div>AI dừng lại chờ học sinh nhập số liệu, rồi mới giải tiếp từng bước.</div>
        </div>
        <div class="auth-feature">
            <i class="bi bi-camera"></i>
            <div>Upload ảnh bài làm để AI chấm điểm và chỉ ra lỗi sai cụ thể.</div>
        </div>
    </section>

    <!-- Form -->
    <section class="auth-form-side">
        <div class="auth-box" style="margin:0;width:100%;max-width:400px;">
            <h2 class="fw-bold mb-1" style="font-size:1.5rem;color:var(--brand);">Đăng Nhập</h2>
            <p class="text-muted mb-4" style="font-size:.88rem;">Chào mừng bạn quay trở lại Gia Sư Lý 9</p>

            <% if (request.getAttribute("error") != null) { %>
                <div class="alert alert-danger"><i class="bi bi-exclamation-circle me-1"></i> <%= request.getAttribute("error") %></div>
            <% } %>

            <form method="post" action="${pageContext.request.contextPath}/login">
                <div class="field">
                    <label for="username">Tên đăng nhập</label>
                    <input type="text" id="username" name="username" placeholder="VD: hocsinh" required autofocus>
                </div>
                <div class="field">
                    <label for="password">Mật khẩu</label>
                    <input type="password" id="password" name="password" placeholder="••••••••" required>
                </div>
                <button type="submit" class="btn btn-primary w-100" style="padding:.58rem;">
                    <i class="bi bi-box-arrow-in-right"></i> Đăng Nhập
                </button>
            </form>

            <p class="text-center text-muted mt-3 mb-0" style="font-size:.82rem;">
                Chưa có tài khoản? <a href="${pageContext.request.contextPath}/register" style="font-weight:700;">Đăng ký ngay</a>
            </p>
        </div>
    </section>
</main>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
