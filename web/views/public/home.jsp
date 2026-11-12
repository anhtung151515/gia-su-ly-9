<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.giasuly9.model.Topic" %>
<%@ page import="com.giasuly9.model.TeacherProfile" %>
<!doctype html>
<html lang="vi">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Trang chủ | Gia Sư Lý 9</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Manrope:wght@400;600;700;800&display=swap" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/main.css" rel="stylesheet">
</head>
<body>
<%@ include file="/views/common/_header.jspf" %>

<!-- ================= BANNER ================= -->
<section class="hero-banner">
    <div class="container py-5">
        <div class="row align-items-center g-5">
            <div class="col-lg-7">
                <h1>Học Vật lý lớp 9 rõ ràng, từng bước, có người dẫn</h1>
                <p>
                    Bài giảng, video, ngân hàng câu hỏi và gia sư AI dừng lại để bạn tự tính số liệu
                    trước khi giải tiếp — giống như đang học 1-1 với thầy giáo.
                </p>
                <div class="d-flex gap-2 mt-4 flex-wrap">
                    <a class="btn btn-white" href="${pageContext.request.contextPath}/topics">
                        <i class="bi bi-collection-play"></i> Chọn bài học
                    </a>
                    <a class="btn btn-yellow" href="${pageContext.request.contextPath}/teachers">
                        <i class="bi bi-person-video3"></i> Xem giáo viên
                    </a>
                </div>
                <div class="stats">
                    <div class="stat"><b>12</b><span>Chủ đề Vật lý 9</span></div>
                    <div class="stat"><b>400+</b><span>Câu hỏi luyện tập</span></div>
                    <div class="stat"><b>3</b><span>Giáo viên Vật lý</span></div>
                </div>
            </div>
            <div class="col-lg-5 d-none d-lg-block">
                <div class="hero-visual">
                    <img src="${pageContext.request.contextPath}/assets/img/hero/physics-hero.svg" alt="Minh hoa Vat ly lop 9">
                </div>
            </div>
        </div>
    </div>
</section>

<!-- ================= CHỦ ĐỀ VẬT LÝ 9 ================= -->
<section class="page">
    <div class="container">
        <%
            List<Topic> topics = (List<Topic>) request.getAttribute("topics");
        %>
        <div class="section-title">
            <h2>Chủ đề Vật lý 9</h2>
            <a href="${pageContext.request.contextPath}/topics">
                Xem tất cả <i class="bi bi-arrow-right"></i>
            </a>
        </div>

        <% if (topics == null || topics.isEmpty()) { %>
            <div class="card">
                <div class="empty-state">
                    <i class="bi bi-journal-x"></i>
                    <h5>Chưa có chủ đề nào</h5>
                    <p class="mb-0">Giáo viên sẽ thêm chủ đề và bài giảng cho Vật lý 9.</p>
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
                <div class="col-md-6 col-lg-3">
                    <a class="course-card" href="${pageContext.request.contextPath}/topics?topicId=${t.topicId}">
                        <div class="course-cover topic-cover">
                            <% if (idx == 1) { %>
                                <span class="cover-new">Mới</span>
                            <% } else if (idx == 2) { %>
                                <span class="cover-hot">Hot</span>
                            <% } %>
                            <span class="cover-num"><%= idx %></span>
                            <% if (t.getImageUrl() != null && !t.getImageUrl().trim().isEmpty()) { %>
                                <img class="course-cover-img" src="${pageContext.request.contextPath}${t.imageUrl}" alt="${t.name}">
                            <% } else { %>
                                <i class="bi bi-atom"></i>
                            <% } %>
                        </div>
                        <div class="course-body">
                            <h3 class="course-title">${t.name}</h3>
                            <p class="course-teacher">
                                <i class="bi bi-collection-play"></i>
                                ${t.lessonCount} bài giảng
                            </p>
                            <div class="course-meta">
                                <span><i class="bi bi-ui-checks-grid"></i> Luyện trắc nghiệm</span>
                                <span><i class="bi bi-pencil-square"></i> Luyện tự luận</span>
                            </div>
                            <span class="course-link">
                                Học chủ đề này <i class="bi bi-arrow-right"></i>
                            </span>
                        </div>
                    </a>
                </div>
                <% } %>
            </div>
        <% } %>

        <!-- ================= GIÁO VIÊN ================= -->
        <%
            List<TeacherProfile> teachers = (List<TeacherProfile>) request.getAttribute("teachers");
        %>
        <div class="section-title">
            <h2>Giáo viên Vật lý 9</h2>
            <a href="${pageContext.request.contextPath}/teachers">
                Xem tất cả <i class="bi bi-arrow-right"></i>
            </a>
        </div>

        <% if (teachers == null || teachers.isEmpty()) { %>
            <div class="card">
                <div class="empty-state">
                    <i class="bi bi-person-video3"></i>
                    <h5>Chưa có giáo viên</h5>
                    <p class="mb-0">Hồ sơ thầy cô sẽ được cập nhật sau.</p>
                </div>
            </div>
        <% } else { %>
            <div class="row g-3">
                <%
                    for (TeacherProfile t : teachers) {
                        pageContext.setAttribute("t", t);
                %>
                <div class="col-md-6 col-lg-4">
                    <a class="teacher-card" style="--tc: <%= t.getColor() != null ? t.getColor() : "#1d4ed8" %>;"
                       href="${pageContext.request.contextPath}/teachers?teacherId=${t.userId}">
                        <div class="teacher-head">
                            <% if (t.getAvatarUrl() != null && !t.getAvatarUrl().trim().isEmpty()) { %>
                                <img class="teacher-avatar" src="${t.avatarUrl}" alt="${t.fullName}">
                            <% } else { %>
                                <span class="teacher-avatar teacher-avatar-fallback">${t.initial}</span>
                            <% } %>
                            <div class="teacher-head-text">
                                <h5 class="teacher-name">${t.fullName}</h5>
                                <div class="teacher-degree">${t.degree}</div>
                            </div>
                        </div>
                        <div class="teacher-title">${t.title}</div>
                        <% if (t.getSpecialty() != null && !t.getSpecialty().trim().isEmpty()) { %>
                            <div class="teacher-tags">
                                <% for (String sp : t.getSpecialty().split(",")) { %>
                                    <% String s = sp.trim(); if (!s.isEmpty()) { %>
                                        <span class="badge-soft badge-teacher"><%= s %></span>
                                <% } } %>
                            </div>
                        <% } %>
                        <div class="teacher-stats">
                            <span><i class="bi bi-collection-play"></i> <b>${t.videoCount}</b> video</span>
                            <span><i class="bi bi-award"></i> <b>${t.yearsExp}</b> năm</span>
                        </div>
                        <span class="course-link">
                            Xem video bài giảng <i class="bi bi-arrow-right"></i>
                        </span>
                    </a>
                </div>
                <% } %>
            </div>
        <% } %>

        <!-- ================= PHÒNG LUYỆN ================= -->
        <div class="section-title">
            <h2>Phòng luyện</h2>
        </div>
        <div class="row g-3">
            <div class="col-md-4">
                <div class="card h-100">
                    <div class="card-body">
                        <div class="stat-icon blue mb-3"><i class="bi bi-ui-checks-grid"></i></div>
                        <h3 class="course-title mb-2">Luyện trắc nghiệm</h3>
                        <p class="text-muted mb-3" style="font-size:.85rem;line-height:1.65;">
                            Làm bài theo chủ đề, tự chấm điểm ngay và nhận xét AI khi trả lời sai.
                        </p>
                        <a class="btn btn-primary btn-sm" href="${pageContext.request.contextPath}/student/practice-setup?type=MCQ">
                            Bắt đầu <i class="bi bi-arrow-right"></i>
                        </a>
                    </div>
                </div>
            </div>
            <div class="col-md-4">
                <div class="card h-100">
                    <div class="card-body">
                        <div class="stat-icon orange mb-3"><i class="bi bi-pencil-square"></i></div>
                        <h3 class="course-title mb-2">Luyện tự luận</h3>
                        <p class="text-muted mb-3" style="font-size:.85rem;line-height:1.65;">
                            Chụp ảnh bài giải viết tay, AI chấm điểm, chỉ ra lỗi sai và cho lời giải đúng.
                        </p>
                        <a class="btn btn-primary btn-sm" href="${pageContext.request.contextPath}/student/practice-setup?type=ESSAY">
                            Nộp bài <i class="bi bi-arrow-right"></i>
                        </a>
                    </div>
                </div>
            </div>
            <div class="col-md-4">
                <div class="card h-100">
                    <div class="card-body">
                        <div class="stat-icon purple mb-3"><i class="bi bi-signpost-split"></i></div>
                        <h3 class="course-title mb-2">Gia sư từng bước</h3>
                        <p class="text-muted mb-3" style="font-size:.85rem;line-height:1.65;">
                            AI hỏi số liệu, bạn nhập rồi mới giải tiếp từng bước.
                        </p>
                        <a class="btn btn-cta btn-sm" href="${pageContext.request.contextPath}/ai-chat">
                            Mở gia sư <i class="bi bi-arrow-right"></i>
                        </a>
                    </div>
                </div>
            </div>
        </div>

        <!-- ================= TÍNH NĂNG ================= -->
        <div class="section-title">
            <h2>Tính năng nổi bật</h2>
        </div>
        <div class="row g-3">
            <div class="col-md-4">
                <div class="card h-100">
                    <div class="card-body">
                        <div class="stat-icon cyan mb-3"><i class="bi bi-play-btn"></i></div>
                        <h3 class="course-title mb-2">Bài giảng đa dạng</h3>
                        <p class="text-muted mb-0" style="font-size:.85rem;line-height:1.65;">
                            Video YouTube, bài đọc chi tiết, tài liệu — kèm thanh tiến độ xem video.
                        </p>
                    </div>
                </div>
            </div>
            <div class="col-md-4">
                <div class="card h-100">
                    <div class="card-body">
                        <div class="stat-icon green mb-3"><i class="bi bi-patch-question"></i></div>
                        <h3 class="course-title mb-2">Ngân hàng câu hỏi</h3>
                        <p class="text-muted mb-0" style="font-size:.85rem;line-height:1.65;">
                            Trắc nghiệm 4 đáp án và tự luận, AI sinh tự động theo chủ đề và mức độ.
                        </p>
                    </div>
                </div>
            </div>
            <div class="col-md-4">
                <div class="card h-100">
                    <div class="card-body">
                        <div class="stat-icon blue mb-3"><i class="bi bi-stars"></i></div>
                        <h3 class="course-title mb-2">iChat — Hỏi đáp với AI</h3>
                        <p class="text-muted mb-0" style="font-size:.85rem;line-height:1.65;">
                            Trò chuyện với AI về bất kỳ kiến thức Vật lý nào, không giới hạn.
                        </p>
                    </div>
                </div>
            </div>
        </div>
    </div>
</section>

<%@ include file="/views/common/_footer.jspf" %>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
