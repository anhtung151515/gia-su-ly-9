<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.giasuly9.model.Question" %>
<%@ page import="com.giasuly9.model.TutorStep" %>
<!doctype html>
<html lang="vi">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>AI giải từng bước | Gia Sư Lý 9</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Manrope:wght@300;400;600;700;800&display=swap" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/main.css" rel="stylesheet">
</head>
<body>
<%@ include file="/views/common/_header.jspf" %>

<section class="page">
    <div class="container tutor-wrap">

        <div class="page-head d-flex justify-content-between align-items-start flex-wrap gap-2">
            <div>
                <h2 class="page-title"><i class="bi bi-signpost-split text-primary"></i> AI giải từng bước</h2>
                <div class="page-sub">AI dừng lại chờ bạn nhập số liệu, rồi mới giải tiếp — giống học 1-1 với thầy</div>
            </div>
            <a href="${pageContext.request.contextPath}/student/practice-setup" class="btn btn-sm btn-outline-primary">
                <i class="bi bi-arrow-left"></i> Quay lại
            </a>
        </div>

        <!-- Đề bài -->
        <div class="card mb-3">
            <div class="card-body p-4">
                <div class="d-flex align-items-center gap-2 mb-2">
                    <span class="badge-soft badge-essay">Đề bài</span>
                    <span class="badge-soft badge-medium">${question.topicName}</span>
                </div>
                <h5 class="fw-bold mb-0" style="line-height:1.6;">${question.content}</h5>
            </div>
        </div>

        <div class="chat-box">
            <!-- Các bước hội thoại -->
            <div class="chat-scroll" id="chatScroll">
                <%
                    List<Map<String, String>> history = (List<Map<String, String>>) request.getAttribute("history");
                    boolean started = (history != null && !history.isEmpty());
                %>

                <% if (!started) { %>
                    <div class="text-center py-4">
                        <div class="stat-icon blue mx-auto mb-3" style="width:56px;height:56px;font-size:1.5rem;">
                            <i class="bi bi-signpost-split"></i>
                        </div>
                        <h5 class="fw-bold">Sẵn sàng bắt đầu?</h5>
                        <p class="text-muted mb-4" style="font-size:.92rem;max-width:520px;margin-left:auto;margin-right:auto;">
                            AI sẽ hướng dẫn bạn giải từng bước một. Mỗi khi cần bạn tự tính toán,
                            AI sẽ dừng lại và chờ bạn nhập kết quả.
                        </p>
                        <form method="post" action="${pageContext.request.contextPath}/tutor/start">
                            <button type="submit" class="btn btn-primary btn-lg">
                                <i class="bi bi-play-fill"></i> Bắt đầu hướng dẫn
                            </button>
                        </form>
                    </div>
                <% } else {
                    for (Map<String, String> m : history) {
                        boolean isUser = "user".equals(m.get("role"));
                %>
                <div class="chat-msg <%= isUser ? "me" : "" %>">
                    <div class="chat-avatar <%= isUser ? "user" : "ai" %>">
                        <i class="bi <%= isUser ? "bi-person" : "bi-stars" %>"></i>
                    </div>
                    <div class="chat-bubble"><%= m.get("content") %></div>
                </div>
                <% } } %>
            </div>

            <!-- Thanh nhập dữ liệu -->
            <div class="tutor-input-bar">
                <%
                    String mode = (String) request.getAttribute("mode");
                    boolean waiting = "waiting_user".equals(mode);
                    boolean finished = "finished".equals(mode);
                %>
                <% if (finished) { %>
                    <div class="alert alert-success mb-0">
                        <i class="bi bi-check-circle-fill"></i>
                        <strong>Đã hoàn thành!</strong> Bạn đã tự giải được bài này.
                        <a href="${pageContext.request.contextPath}/student/history" class="ms-2">Xem lịch sử</a>
                    </div>
                <% } else if (!started) { %>
                    <div class="text-center text-muted py-2" style="font-size:.88rem;">
                        <i class="bi bi-lightbulb"></i> Bấm "Bắt đầu hướng dẫn" để AI bắt đầu giải từng bước.
                    </div>
                <% } else { %>
                    <% if (waiting) { %>
                        <div class="alert alert-warning py-2 mb-2" style="font-size:.87rem;">
                            <i class="bi bi-hand-index-thumb"></i>
                            <strong>AI đang chờ bạn:</strong> Hãy nhập kết quả tính được của bạn ở ô bên dưới.
                        </div>
                    <% } %>
                    <form method="post" action="${pageContext.request.contextPath}/tutor/next">
                        <div class="d-flex gap-2 align-items-end">
                            <div class="flex-grow-1">
                                <label class="form-label" for="userInput">
                                    <%= waiting ? "Nhập số liệu / kết quả của bạn" : "Trả lời AI (nếu có)" %>
                                </label>
                                <textarea class="form-control" name="userInput" id="userInput" rows="2"
                                          placeholder="<%= waiting ? "Ví dụ: 100 N, 20 m, hoặc 5 m/s² ..." : "Nhập câu trả lời của bạn..." %>"></textarea>
                            </div>
                            <button type="submit" class="btn btn-primary px-4">
                                <i class="bi bi-send"></i> Gửi
                            </button>
                        </div>
                    </form>
                <% } %>
            </div>
        </div>

        <!-- Nhật ký các bước đã lưu -->
        <%
            List<TutorStep> steps = (List<TutorStep>) request.getAttribute("steps");
            if (steps != null && !steps.isEmpty()) {
        %>
        <div class="card mt-3">
            <div class="card-head">
                <h6 class="card-title"><i class="bi bi-clock-history"></i> Lịch sử các bước đã lưu</h6>
                <span class="badge-soft badge-mcq"><%= steps.size() %> bước</span>
            </div>
            <div class="card-body">
                <%
                    for (TutorStep s : steps) {
                        pageContext.setAttribute("s", s);

                %>
                <div class="d-flex gap-3 mb-3">
                    <div class="chat-avatar <%= "USER".equals(s.getSpeaker()) ? "user" : "ai" %>"
                         style="width:30px;height:30px;">
                        <i class="bi <%= "USER".equals(s.getSpeaker()) ? "bi-person" : "bi-stars" %>"></i>
                    </div>
                    <div class="flex-grow-1">
                        <div class="d-flex align-items-center gap-2 mb-1">
                            <span class="fw-bold" style="font-size:.85rem;">
                                <%= "USER".equals(s.getSpeaker()) ? "Bạn" : "AI gia sư" %>
                            </span>
                            <span class="step-badge step-${s.stepType}">${s.stepType}</span>
                        </div>
                        <div style="font-size:.89rem;line-height:1.6;white-space:pre-wrap;">${s.message}</div>
                    </div>
                </div>
                <% } %>
            </div>
        </div>
        <% } %>
    </div>
</section>

<%@ include file="/views/common/_footer.jspf" %>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
<script>
    const scrollBox = document.getElementById('chatScroll');
    if (scrollBox) { scrollBox.scrollTop = scrollBox.scrollHeight; }

    const input = document.getElementById('userInput');
    if (input) {
        input.focus();
        input.addEventListener('keydown', e => {
            if ((e.ctrlKey || e.metaKey) && e.key === 'Enter') {
                e.preventDefault();
                input.form.submit();
            }
        });
    }
</script>
</body>
</html>
