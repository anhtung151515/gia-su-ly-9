<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.giasuly9.model.Question" %>
<%@ page import="com.giasuly9.model.PracticeAnswer" %>
<%@ page import="java.util.Set" %>
<%
    Question question = (Question) request.getAttribute("question");
    pageContext.setAttribute("question", question);
    Integer indexAttr = (Integer) request.getAttribute("index");
    Integer totalAttr = (Integer) request.getAttribute("total");
    int index = indexAttr != null ? indexAttr : 0;
    int total = totalAttr != null ? totalAttr : 0;
    String practiceType = (String) request.getAttribute("practiceType");
    PracticeAnswer savedAnswer = (PracticeAnswer) request.getAttribute("savedAnswer");
    pageContext.setAttribute("savedAnswer", savedAnswer);
    Set<Integer> answeredSet = (Set<Integer>) request.getAttribute("answeredSet");
    pageContext.setAttribute("answeredSet", answeredSet);
    boolean isEssay = "ESSAY".equals(practiceType);
%>
<!doctype html>
<html lang="vi">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Câu hỏi <%= index + 1 %> | Gia Sư Lý 9</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Manrope:wght@300;400;600;700;800&display=swap" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/main.css" rel="stylesheet">
</head>
<body>
<%@ include file="/views/common/_header.jspf" %>

<section class="page">
    <div class="container practice-shell">
        <!-- Thanh tiến trình -->
        <div class="mb-3">
            <div class="d-flex justify-content-between align-items-center mb-2">
                <div class="fw-bold">Câu <%= index + 1 %> / <%= total %></div>
                <div class="d-flex gap-2">
                    <span class="badge-soft <%= isEssay ? "badge-essay" : "badge-mcq" %>">
                        <%= isEssay ? "Tự luận" : "Trắc nghiệm" %>
                    </span>
                    <% if (question.getDifficulty() != null) {
                            pageContext.setAttribute("diff", question.getDifficulty());
                       %>
                        <span class="badge-soft badge-${diff.toLowerCase()}">
                            <%= "EASY".equals(question.getDifficulty()) ? "Dễ" : ("HARD".equals(question.getDifficulty()) ? "Khó" : "Trung bình") %>
                        </span>
                    <% } %>
                </div>
            </div>
            <div class="progress-thin">
                <div style="width:<%= (index + 1) * 100 / total %>%"></div>
            </div>
        </div>

        <!-- ================= THANH ĐIỀU HƯỚNG CÂU ================= -->
        <div class="card mb-3">
            <div class="card-body py-2 px-3">
                <div class="d-flex align-items-center justify-content-between gap-2 flex-wrap">
                    <a class="btn btn-sm btn-outline-secondary <%= (index <= 0) ? "disabled" : "" %>"
                       href="${pageContext.request.contextPath}/practice/navigate?dir=prev&index=<%= index %>">
                        <i class="bi bi-chevron-left"></i> Câu trước
                    </a>

                    <div class="q-nav flex-grow-1">
                        <% if (total > 0) {
                            for (int i = 0; i < total; i++) {
                                boolean done = answeredSet != null && answeredSet.contains(i);
                                boolean cur = (i == index);
                        %>
                        <a class="q-nav-item<%= cur ? " current" : "" %><%= done ? " done" : "" %>"
                           href="${pageContext.request.contextPath}/practice/navigate?to=<%= i %>&index=<%= index %>"
                           title="Câu <%= i + 1 %><%= done ? " (da lam)" : "" %>"><%= i + 1 %></a>
                        <% } } %>
                    </div>

                    <% if (index + 1 < total) { %>
                        <a class="btn btn-sm btn-primary"
                           href="${pageContext.request.contextPath}/practice/navigate?dir=next&index=<%= index %>">
                            Câu sau <i class="bi bi-chevron-right"></i>
                        </a>
                    <% } else { %>
                        <a class="btn btn-sm btn-success"
                           href="${pageContext.request.contextPath}/practice/finish">
                            <i class="bi bi-flag"></i> Nộp bài
                        </a>
                    <% } %>
                </div>
            </div>
        </div>

        <% if (request.getAttribute("error") != null) { %>
            <div class="alert alert-danger"><i class="bi bi-exclamation-triangle"></i> <%= request.getAttribute("error") %></div>
        <% } %>

        <% if (savedAnswer != null) { %>
            <div class="alert alert-info d-flex align-items-center gap-2 mb-3" style="font-size:.9rem;">
                <i class="bi bi-info-circle"></i>
                <span>Bạn đã trả lời câu này. Xem lại đáp án bạn đã chọn, điểm và nhận xét AI
                    sẽ hiển thị ở trang kết quả sau khi nộp bài.</span>
            </div>
        <% } %>

        <div class="card mb-3">
            <div class="card-body p-4">
                <h5 class="fw-bold mb-3" style="line-height:1.6;">${question.content}</h5>

                <% if (isEssay) { %>
                    <!-- ==================== TỰ LUẬN ==================== -->
                    <form method="post" enctype="multipart/form-data"
                          action="${pageContext.request.contextPath}/practice/answer-essay">
                        <input type="hidden" name="qIndex" value="<%= index %>">
                        <div class="mb-3">
                            <label class="form-label">Bài làm của bạn</label>
                            <textarea class="form-control" name="studentAnswer" rows="5"
                                      placeholder="Nhập lời giải của bạn ở đây..."><%= savedAnswer != null && savedAnswer.getUserAnswer() != null ? savedAnswer.getUserAnswer() : "" %></textarea>
                            <div class="form-hint">Bạn có thể viết tay, chụp ảnh bài làm, hoặc làm cả hai.</div>
                        </div>

                        <div class="mb-3">
                            <label class="form-label">Hoặc upload ảnh bài làm (tối đa 5MB)</label>
                            <div class="img-upload-box" id="uploadBox">
                                <i class="bi bi-cloud-arrow-up" style="font-size:2rem;opacity:.45;"></i>
                                <div class="fw-bold mt-2">Chọn hoặc kéo thả ảnh vào đây</div>
                                <div class="form-hint">AI sẽ đọc ảnh để nhận xét bài làm của bạn</div>
                                <input type="file" name="answerImage" id="answerImage" accept="image/*"
                                       class="d-none">
                            </div>
                            <img id="imgPreview" class="img-preview d-none" alt="Xem trước">
                        </div>

                        <div class="d-flex gap-2 flex-wrap">
                            <button type="submit" class="btn btn-primary">
                                <i class="bi bi-send-check"></i> Nộp bài và sang câu tiếp
                            </button>
                            <a href="${pageContext.request.contextPath}/tutor/open?questionId=${question.questionId}"
                               class="btn btn-outline-primary">
                                <i class="bi bi-signpost-split"></i> AI giải từng bước
                            </a>
                        </div>
                    </form>

                <% } else { %>
                    <!-- ==================== TRẮC NGIỆM ==================== -->
                    <form method="post" action="${pageContext.request.contextPath}/practice/answer-mcq" id="mcqForm">
                        <input type="hidden" name="qIndex" value="<%= index %>">
                        <%
                            String[] letters = {"A", "B", "C", "D"};
                            String[] opts = {question.getOptionA(), question.getOptionB(), question.getOptionC(), question.getOptionD()};
                            String preselected = savedAnswer != null ? savedAnswer.getSelectedAnswer() : null;
                        %>
                        <%
                            for (int i = 0; i < opts.length; i++) {
                                if (opts[i] == null || opts[i].trim().isEmpty()) { continue; }
                                boolean checked = preselected != null && preselected.equalsIgnoreCase(letters[i]);
                        %>
                        <label class="option-btn<%= checked ? " selected" : "" %>" data-letter="<%= letters[i] %>">
                            <span class="option-letter"><%= letters[i] %></span>
                            <span><%= opts[i] %></span>
                            <input type="radio" name="answer" value="<%= letters[i] %>" class="d-none"<%= checked ? " checked" : "" %>>
                        </label>
                        <% } %>

                        <div class="d-flex gap-2 flex-wrap mt-3">
                            <button type="submit" class="btn btn-primary" id="submitMcq"<%= preselected == null ? " disabled" : "" %>>
                                <i class="bi bi-check-lg"></i> Xác nhận đáp án
                            </button>
                            <a href="${pageContext.request.contextPath}/tutor/open?questionId=${question.questionId}"
                               class="btn btn-outline-primary">
                                <i class="bi bi-signpost-split"></i> AI giải từng bước
                            </a>
                        </div>
                    </form>
                <% } %>

                <!-- Điều hướng + nộp bài -->
                <div class="d-flex gap-2 flex-wrap mt-3 pt-3 align-items-center"
                     style="border-top:1px dashed var(--border);">
                    <a href="${pageContext.request.contextPath}/practice/navigate?dir=prev&index=<%= index %>"
                       class="btn btn-outline-secondary <%= (index <= 0) ? "disabled" : "" %>">
                        <i class="bi bi-arrow-left"></i> Câu trước
                    </a>
                    <% if (index + 1 < total) { %>
                        <a href="${pageContext.request.contextPath}/practice/navigate?dir=next&index=<%= index %>"
                           class="btn btn-outline-primary">
                            Câu sau <i class="bi bi-arrow-right"></i>
                        </a>
                    <% } %>
                    <a href="${pageContext.request.contextPath}/practice/finish"
                       class="btn <%= (index + 1 >= total) ? "btn-success" : "btn-outline-secondary" %>">
                        <i class="bi bi-flag"></i>
                        <%= (index + 1 >= total) ? "Nộp bài và xem điểm" : "Nộp bài xem điểm (dở dang)" %>
                    </a>
                    <% if (index + 1 < total) { %>
                        <span class="align-self-center text-muted" style="font-size:.85rem;">
                            Bạn mới làm <%= index + 1 %>/<%= total %> câu — câu chưa làm sẽ tính 0 điểm.
                        </span>
                    <% } %>
                </div>
            </div>
        </div>

        <% if (question.getExplanation() != null && !question.getExplanation().trim().isEmpty()) { %>
            <div class="feedback-box feedback-info">
                <h6><i class="bi bi-lightbulb"></i> Gợi ý</h6>
                <%= question.getExplanation() %>
            </div>
        <% } %>
    </div>
</section>

<%@ include file="/views/common/_footer.jspf" %>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
<script>
    // Chọn đáp án trắc nghiệm
    const form = document.getElementById('mcqForm');
    if (form) {
        const submitBtn = document.getElementById('submitMcq');
        document.querySelectorAll('.option-btn').forEach(btn => {
            btn.addEventListener('click', () => {
                document.querySelectorAll('.option-btn').forEach(b => b.classList.remove('selected'));
                btn.classList.add('selected');
                btn.querySelector('input').checked = true;
                submitBtn.disabled = false;
            });
        });
        // Chống double-click: vô hiệu hoá nút sau khi bấm lần đầu
        form.addEventListener('submit', () => {
            submitBtn.disabled = true;
            submitBtn.innerHTML = '<i class="bi bi-hourglass-split"></i> Đang gửi...';
        });
    }

    // Upload ảnh bài làm
    const uploadBox = document.getElementById('uploadBox');
    const fileInput = document.getElementById('answerImage');
    const preview = document.getElementById('imgPreview');
    if (uploadBox && fileInput) {
        uploadBox.addEventListener('click', () => fileInput.click());
        uploadBox.addEventListener('dragover', e => {
            e.preventDefault();
            uploadBox.style.borderColor = '#1e78ff';
            uploadBox.style.background = '#eaf2ff';
        });
        uploadBox.addEventListener('dragleave', () => {
            uploadBox.style.borderColor = '';
            uploadBox.style.background = '';
        });
        uploadBox.addEventListener('drop', e => {
            e.preventDefault();
            uploadBox.style.borderColor = '';
            uploadBox.style.background = '';
            if (e.dataTransfer.files.length) {
                fileInput.files = e.dataTransfer.files;
                showPreview(e.dataTransfer.files[0]);
            }
        });
        fileInput.addEventListener('change', () => {
            if (fileInput.files.length) showPreview(fileInput.files[0]);
        });

        function showPreview(file) {
            const reader = new FileReader();
            reader.onload = e => {
                preview.src = e.target.result;
                preview.classList.remove('d-none');
            };
            reader.readAsDataURL(file);
        }
    }
</script>
</body>
</html>
