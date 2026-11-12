<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    String path = request.getServletPath();
    boolean isAbout = "/about".equals(path);
    String pageTitle = isAbout ? "Về chúng tôi" : "Liên hệ";
%>
<!doctype html>
<html lang="vi">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title><%= pageTitle %> | Gia Sư Lý 9</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Manrope:wght@300;400;600;700;800&display=swap" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/main.css" rel="stylesheet">
</head>
<body>
<%@ include file="/views/common/_header.jspf" %>

<section class="page">
    <div class="container" style="max-width:860px;">
        <div class="page-head">
            <h2 class="page-title"><%= pageTitle %></h2>
            <div class="page-sub">Gia Sư Lý 9 — nền tảng học Vật lý lớp 9 cùng trợ lý AI</div>
        </div>

        <% if (isAbout) { %>
            <div class="card">
                <div class="card-body p-4">
                    <h5 class="fw-bold mb-3"><i class="bi bi-info-circle"></i> Về Gia Sư Lý 9</h5>
                    <p class="text-muted" style="line-height:1.8;">
                        Gia Sư Lý 9 là nền tảng hỗ trợ học sinh lớp 9 học môn Vật lý.
                        Toàn bộ bài giảng, ngân hàng câu hỏi trắc nghiệm và bài tự luận được
                        sắp xếp theo môn học và chủ đề, giúp học sinh học có hệ thống.
                    </p>
                    <p class="text-muted" style="line-height:1.8;">
                        Điểm nổi bật là trợ lý AI đồng hành cùng học sinh: AI giải từng bước
                        trong bài tự luận, nhận xét bài làm viết tay hoặc ảnh chụp, giải thích
                        khi trả lời trắc nghiệm sai, và giúp giáo viên sinh câu hỏi nhanh theo chủ đề.
                    </p>

                    <h6 class="fw-bold mt-4 mb-3">Tính năng chính</h6>
                    <ul class="text-muted" style="line-height:1.9;">
                        <li>Bài giảng dạng video YouTube, bài đọc và liên kết học tập.</li>
                        <li>Luyện tập trắc nghiệm 4 đáp án với nhận xét tức thì khi chọn sai.</li>
                        <li>Luyện tập tự luận: viết tay, upload ảnh bài làm, AI chấm và chỉ ra lỗi sai.</li>
                        <li>Chế độ "AI giải từng bước" dừng lại để học sinh nhập số liệu từng bước một.</li>
                        <li>Quản trị viên quản lý người dùng, nội dung và cấu hình API AI.</li>
                    </ul>
                </div>
            </div>
        <% } else { %>
            <div class="card">
                <div class="card-body p-4">
                    <h5 class="fw-bold mb-3"><i class="bi bi-envelope"></i> Liên hệ với chúng tôi</h5>
                    <div class="row g-3">
                        <div class="col-md-6">
                            <div class="topic-row">
                                <div class="d-flex align-items-center gap-3">
                                    <div class="topic-index"><i class="bi bi-envelope"></i></div>
                                    <div>
                                        <div class="topic-name">Email hỗ trợ</div>
                                        <div class="text-muted" style="font-size:.85rem;">support@giasuly9.vn</div>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div class="col-md-6">
                            <div class="topic-row">
                                <div class="d-flex align-items-center gap-3">
                                    <div class="topic-index"><i class="bi bi-telephone"></i></div>
                                    <div>
                                        <div class="topic-name">Hotline</div>
                                        <div class="text-muted" style="font-size:.85rem;">1900 2029</div>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div class="col-md-6">
                            <div class="topic-row">
                                <div class="d-flex align-items-center gap-3">
                                    <div class="topic-index"><i class="bi bi-geo-alt"></i></div>
                                    <div>
                                        <div class="topic-name">Địa chỉ</div>
                                        <div class="text-muted" style="font-size:.85rem;">Số 1 Đường Nguyễn Huệ, Hà Nội</div>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div class="col-md-6">
                            <div class="topic-row">
                                <div class="d-flex align-items-center gap-3">
                                    <div class="topic-index"><i class="bi bi-clock"></i></div>
                                    <div>
                                        <div class="topic-name">Giờ làm việc</div>
                                        <div class="text-muted" style="font-size:.85rem;">8:00 - 22:00, cả 7 ngày</div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        <% } %>

        <div class="text-center mt-4">
            <a href="${pageContext.request.contextPath}/subjects" class="btn btn-primary">
                <i class="bi bi-atom"></i> Xem các môn học
            </a>
        </div>
    </div>
</section>

<%@ include file="/views/common/_footer.jspf" %>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
