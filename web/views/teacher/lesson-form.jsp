<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.giasuly9.model.Subject" %>
<%@ page import="com.giasuly9.model.Topic" %>
<!doctype html>
<html lang="vi">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Thêm bài giảng | Gia Sư Lý 9</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Manrope:wght@300;400;600;700;800&display=swap" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/main.css" rel="stylesheet">
</head>
<body>
<div class="app-shell">
    <%@ include file="/views/common/_sidebar.jspf" %>
    <div class="app-main">
        <% request.setAttribute("pageTitle", "Bài giảng"); %>
        <%@ include file="/views/common/_topbar.jspf" %>

        <div class="app-content" style="max-width:1000px;">
            <%
                com.giasuly9.model.Lesson lesson = (com.giasuly9.model.Lesson) request.getAttribute("lesson");
                boolean isEdit = (lesson != null);
            %>
            <div class="page-head">
                <h2 class="page-title"><%= isEdit ? "Cập nhật bài giảng" : "Thêm bài giảng mới" %></h2>
                <div class="page-sub">Bài giảng có thể là video YouTube, video tải lên, bài đọc text hoặc link website</div>
            </div>

            <% if (session.getAttribute("flash") != null) { %>
                <div class="alert alert-success alert-dismissible fade show">
                    <i class="bi bi-check-circle"></i> <%= session.getAttribute("flash") %>
                    <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                </div>
                <% session.removeAttribute("flash"); %>
            <% } %>

            <div class="card">
                <div class="card-body p-4">
                    <form method="post" action="${pageContext.request.contextPath}/teacher/lesson-save">
                        <% if (isEdit) { %>
                            <input type="hidden" name="id" value="${lesson.lessonId}">
                        <% } %>

                        <div class="row g-3">
                            <div class="col-md-6">
                                <label class="form-label">Môn học *</label>
                                <select name="subjectId" class="form-select" id="subjectSelect" required>
                                    <option value="">-- Chọn môn học --</option>
                                    <%
                                        List<Subject> subjects = (List<Subject>) request.getAttribute("subjects");
                                        if (subjects != null) {
                                            for (Subject s : subjects) {
                                                pageContext.setAttribute("s", s);

                                    %>
                                    <option value="${s.subjectId}"
                                        <%= isEdit && lesson.getSubjectId() == s.getSubjectId() ? "selected" : "" %>>
                                        ${s.name}
                                    </option>
                                    <% } } %>
                                </select>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label">Chủ đề *</label>
                                <select name="topicId" class="form-select" id="topicSelect" required>
                                    <option value="">-- Chọn chủ đề --</option>
                                    <%
                                        List<Topic> topics = (List<Topic>) request.getAttribute("topics");
                                        if (topics != null) {
                                            for (Topic t : topics) {
                                                pageContext.setAttribute("t", t);

                                    %>
                                    <option value="${t.topicId}" data-subject="${t.subjectId}"
                                        <%= isEdit && lesson.getTopicId() == t.getTopicId() ? "selected" : "" %>>
                                        ${t.subjectName} — ${t.name}
                                    </option>
                                    <% } } %>
                                </select>
                            </div>

                            <div class="col-12">
                                <label class="form-label">Tiêu đề bài giảng *</label>
                                <input type="text" name="title" class="form-control" required
                                       placeholder="VD: Bài 1: Chuyển động cơ"
                                       value="<%= isEdit ? lesson.getTitle() : "" %>">
                            </div>

                            <div class="col-12">
                                <label class="form-label">Mô tả ngắn</label>
                                <textarea name="description" class="form-control" rows="2"
                                          placeholder="Tóm tắt nội dung bài giảng..."><%= isEdit ? lesson.getDescription() : "" %></textarea>
                            </div>

                            <div class="col-md-6">
                                <label class="form-label">Loại bài giảng *</label>
                                <select name="lessonType" class="form-select" id="lessonType">
                                    <option value="VIDEO_YOUTUBE"
                                        <%= isEdit && "VIDEO_YOUTUBE".equals(lesson.getLessonType()) ? "selected" : "" %>>
                                        Video YouTube
                                    </option>
                                    <option value="VIDEO_FILE"
                                        <%= isEdit && "VIDEO_FILE".equals(lesson.getLessonType()) ? "selected" : "" %>>
                                        Video tải lên / link video
                                    </option>
                                    <option value="TEXT"
                                        <%= isEdit && "TEXT".equals(lesson.getLessonType()) ? "selected" : "" %>>
                                        Bài đọc (text)
                                    </option>
                                    <option value="WEBSITE"
                                        <%= isEdit && "WEBSITE".equals(lesson.getLessonType()) ? "selected" : "" %>>
                                        Link website
                                    </option>
                                </select>
                            </div>

                            <div class="col-md-6">
                                <label class="form-label">Thời lượng (phút)</label>
                                <input type="number" name="durationMinutes" class="form-control" min="0" max="600"
                                       value="<%= isEdit ? lesson.getDurationMinutes() : 45 %>">
                            </div>

                            <div class="col-12" id="videoUrlBox">
                                <label class="form-label">Đường dẫn video</label>
                                <input type="url" name="videoUrl" class="form-control"
                                       placeholder="https://www.youtube.com/watch?v=... hoặc link embed"
                                       value="<%= isEdit && lesson.getVideoUrl() != null ? lesson.getVideoUrl() : "" %>">
                                <div class="form-hint">
                                    Với YouTube, dùng định dạng: <code>https://www.youtube.com/embed/VIDEO_ID</code>
                                </div>
                            </div>

                            <div class="col-12" id="documentUrlBox">
                                <label class="form-label">Link tài liệu (PDF, Google Drive...)</label>
                                <input type="url" name="documentUrl" class="form-control"
                                       placeholder="https://drive.google.com/..."
                                       value="<%= isEdit && lesson.getDocumentUrl() != null ? lesson.getDocumentUrl() : "" %>">
                            </div>

                            <div class="col-12" id="contentBox">
                                <label class="form-label">Nội dung bài đọc</label>
                                <textarea name="content" class="form-control" rows="8"
                                          placeholder="Viết nội dung bài giảng ở đây...&#10;&#10;Có thể dùng công thức: W = P.h, s = v0.t + 1/2.a.t²"><%= isEdit && lesson.getContent() != null ? lesson.getContent() : "" %></textarea>
                                <div class="form-hint">Nội dung sẽ hiển thị cho học sinh khi mở bài giảng loại "Bài đọc".</div>
                            </div>
                        </div>

                        <div class="d-flex gap-2 mt-4">
                            <button type="submit" class="btn btn-primary">
                                <i class="bi bi-check-lg"></i> <%= isEdit ? "Cập nhật" : "Thêm bài giảng" %>
                            </button>
                            <a href="${pageContext.request.contextPath}/teacher/lessons" class="btn btn-outline-secondary">Hủy</a>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
<script>
    // Lọc chủ đề theo môn học
    const subjectSelect = document.getElementById('subjectSelect');
    const topicSelect = document.getElementById('topicSelect');
    const allTopicOptions = Array.from(topicSelect.querySelectorAll('option[data-subject]'));

    function filterTopics() {
        const sv = subjectSelect.value;
        allTopicOptions.forEach(opt => {
            const show = !sv || opt.dataset.subject === sv;
            opt.style.display = show ? '' : 'none';
        });
    }
    subjectSelect.addEventListener('change', filterTopics);
    filterTopics();

    // Hiện/ẩn các trường theo loại bài giảng
    const lessonType = document.getElementById('lessonType');
    function toggleType() {
        const t = lessonType.value;
        document.getElementById('videoUrlBox').style.display =
            (t === 'VIDEO_YOUTUBE' || t === 'VIDEO_FILE') ? '' : 'none';
        document.getElementById('contentBox').style.display = (t === 'TEXT') ? '' : 'none';
    }
    lessonType.addEventListener('change', toggleType);
    toggleType();
</script>
</body>
</html>
