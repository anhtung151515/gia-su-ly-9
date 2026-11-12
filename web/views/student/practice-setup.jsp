<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.giasuly9.model.Topic" %>
<!doctype html>
<html lang="vi">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Luyện tập Vật lý 9 | Gia Sư Lý 9</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Manrope:wght@300;400;600;700;800&display=swap" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/main.css" rel="stylesheet">
</head>
<body>
<%@ include file="/views/common/_header.jspf" %>

<section class="page">
    <div class="container practice-shell">
        <div class="page-head">
            <h2 class="page-title">Luyện tập Vật lý 9</h2>
            <div class="page-sub">Chọn chủ đề → chọn kiểu luyện tập → làm bài và được chấm điểm ngay</div>
        </div>

        <% if (session.getAttribute("flashError") != null) { %>
            <div class="alert alert-warning alert-dismissible fade show">
                <i class="bi bi-exclamation-triangle"></i> <%= session.getAttribute("flashError") %>
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
            <% session.removeAttribute("flashError"); %>
        <% } %>

        <div class="card mb-3">
            <div class="card-head">
                <h5 class="card-title"><span class="topic-index me-2">1</span>Chọn chủ đề Vật lý 9</h5>
            </div>
            <div class="card-body">
                <%
                    List<Topic> topics = (List<Topic>) request.getAttribute("topics");
                    if (topics == null || topics.isEmpty()) {
                %>
                    <div class="empty-state py-3">
                        <i class="bi bi-folder2-open"></i>
                        <p class="mb-0">Chưa có chủ đề nào.</p>
                    </div>
                <% } else {
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
                                <div class="text-muted" style="font-size:.82rem;">
                                    ${t.lessonCount} bài giảng
                                    <% if (t.getDescription() != null && !t.getDescription().trim().isEmpty()) { %>
                                        — ${t.description}
                                    <% } %>
                                </div>
                            </div>
                        </div>
                        <a class="btn btn-sm btn-outline-primary"
                           href="?topicId=${t.topicId}&type=${practiceType}">
                            Chọn <i class="bi bi-arrow-right"></i>
                        </a>
                    </div>
                    <% idx++; } %>
                <% } %>
            </div>
        </div>

        <%
            Topic topic = (Topic) request.getAttribute("topic");
            pageContext.setAttribute("topic", topic);
            if (topic != null) {
        %>
        <div class="card">
            <div class="card-head">
                <h5 class="card-title"><span class="topic-index me-2">2</span>Chọn kiểu luyện tập — ${topic.name}</h5>
            </div>
            <div class="card-body">
                <div class="row g-3">
                    <div class="col-md-6">
                        <div class="card h-100" style="border-width:2px;border-color:${practiceType eq 'MCQ' ? '#1d4ed8' : '#e2e8f0'};">
                            <div class="card-body">
                                <div class="d-flex align-items-center gap-3 mb-2">
                                    <div class="stat-icon blue"><i class="bi bi-ui-checks-grid"></i></div>
                                    <h6 class="fw-bold mb-0">Trắc nghiệm</h6>
                                </div>
                                <p class="text-muted" style="font-size:.87rem;">
                                    Câu hỏi 4 đáp án A-B-C-D. Đúng 10 điểm, sai 0 điểm — hệ thống chấm ngay
                                    và AI nhận xét câu sai.
                                </p>
                                <div class="d-flex align-items-center justify-content-between mb-3">
                                    <span class="badge-soft badge-mcq">${mcqCount} câu trong ngân hàng</span>
                                </div>
                                <form method="post" action="${pageContext.request.contextPath}/practice/start">
                                    <input type="hidden" name="topicId" value="${topic.topicId}">
                                    <input type="hidden" name="type" value="MCQ">
                                    <div class="mb-2">
                                        <label class="form-label">Số câu muốn luyện</label>
                                        <select name="count" class="form-select form-select-sm">
                                            <option value="5">5 câu</option>
                                            <option value="10" selected>10 câu</option>
                                            <option value="15">15 câu</option>
                                            <option value="20">20 câu</option>
                                        </select>
                                    </div>
                                    <button type="submit" class="btn btn-primary w-100">
                                        <i class="bi bi-play-fill"></i> Bắt đầu luyện trắc nghiệm
                                    </button>
                                </form>
                            </div>
                        </div>
                    </div>

                    <div class="col-md-6">
                        <div class="card h-100" style="border-width:2px;border-color:${practiceType eq 'ESSAY' ? '#1d4ed8' : '#e2e8f0'};">
                            <div class="card-body">
                                <div class="d-flex align-items-center gap-3 mb-2">
                                    <div class="stat-icon cyan"><i class="bi bi-pencil-square"></i></div>
                                    <h6 class="fw-bold mb-0">Tự luận</h6>
                                </div>
                                <p class="text-muted" style="font-size:.87rem;">
                                    Viết bài hoặc upload ảnh bài làm. AI chấm điểm thang 10, chỉ ra lỗi sai
                                    và đưa lời giải đúng.
                                </p>
                                <div class="d-flex align-items-center justify-content-between mb-3">
                                    <span class="badge-soft badge-essay">${essayCount} câu trong ngân hàng</span>
                                </div>
                                <form method="post" action="${pageContext.request.contextPath}/practice/start">
                                    <input type="hidden" name="topicId" value="${topic.topicId}">
                                    <input type="hidden" name="type" value="ESSAY">
                                    <div class="mb-2">
                                        <label class="form-label">Số câu muốn luyện</label>
                                        <select name="count" class="form-select form-select-sm">
                                            <option value="3">3 câu</option>
                                            <option value="5" selected>5 câu</option>
                                            <option value="10">10 câu</option>
                                        </select>
                                    </div>
                                    <button type="submit" class="btn btn-primary w-100">
                                        <i class="bi bi-play-fill"></i> Bắt đầu luyện tự luận
                                    </button>
                                </form>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="alert alert-info mt-3">
                    <i class="bi bi-info-circle"></i>
                    <strong>Mẹo:</strong> Với bài tự luận, sau khi luyện bạn có thể dùng nút
                    <i class="bi bi-signpost-split"></i> <strong>AI giải từng bước</strong>
                    để được hướng dẫn chi tiết từng bước một.
                </div>
            </div>
        </div>
        <% } %>
    </div>
</section>

<%@ include file="/views/common/_footer.jspf" %>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
