<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.giasuly9.model.Subject" %>
<%@ page import="com.giasuly9.model.Topic" %>
<%@ page import="com.giasuly9.model.Question" %>
<!doctype html>
<html lang="vi">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>AI tạo câu hỏi | Gia Sư Lý 9</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Manrope:wght@300;400;600;700;800&display=swap" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/main.css" rel="stylesheet">
</head>
<body>
<div class="app-shell">
    <%@ include file="/views/common/_sidebar.jspf" %>
    <div class="app-main">
        <% request.setAttribute("pageTitle", "AI tạo câu hỏi"); %>
        <%@ include file="/views/common/_topbar.jspf" %>

        <div class="app-content">
            <% if (session.getAttribute("flash") != null) { %>
                <div class="alert alert-success alert-dismissible fade show">
                    <i class="bi bi-check-circle"></i> <%= session.getAttribute("flash") %>
                    <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                </div>
                <% session.removeAttribute("flash"); %>
            <% } %>

            <div class="page-head">
                <h2 class="page-title"><i class="bi bi-stars text-primary"></i> AI tạo câu hỏi</h2>
                <div class="page-sub">
                    Chọn môn học, chủ đề và tham số — AI sẽ sinh câu hỏi trắc nghiệm hoặc tự luận theo đúng kiến thức Vật lý 9
                </div>
            </div>

            <div class="card mb-3">
                <div class="card-body p-4">
                    <form method="post" action="${pageContext.request.contextPath}/teacher/ai-generate">
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
                                    <option value="${s.subjectId}">${s.name}</option>
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
                                            for (Topic tp : topics) {
                                                pageContext.setAttribute("tp", tp);

                                    %>
                                    <option value="${tp.topicId}" data-subject="${tp.subjectId}">
                                        ${tp.subjectName} — ${tp.name}
                                    </option>
                                    <% } } %>
                                </select>
                            </div>
                            <div class="col-md-4">
                                <label class="form-label">Loại câu hỏi *</label>
                                <select name="questionType" class="form-select">
                                    <option value="MCQ">Trắc nghiệm (A-B-C-D)</option>
                                    <option value="ESSAY">Tự luận (mở đầu)</option>
                                </select>
                            </div>
                            <div class="col-md-4">
                                <label class="form-label">Mức độ</label>
                                <select name="difficulty" class="form-select">
                                    <option value="EASY">Dễ</option>
                                    <option value="MEDIUM" selected>Trung bình</option>
                                    <option value="HARD">Khó</option>
                                </select>
                            </div>
                            <div class="col-md-4">
                                <label class="form-label">Số lượng câu hỏi</label>
                                <select name="count" class="form-select">
                                    <option value="5">5 câu</option>
                                    <option value="10" selected>10 câu</option>
                                    <option value="15">15 câu</option>
                                    <option value="20">20 câu</option>
                                    <option value="30">30 câu</option>
                                </select>
                            </div>
                            <div class="col-12">
                                <div class="form-check">
                                    <input class="form-check-input" type="checkbox" name="autoSave" value="1" id="autoSave">
                                    <label class="form-check-label" for="autoSave">
                                        <strong>Tự động lưu</strong> vào ngân hàng câu hỏi ngay sau khi AI sinh
                                    </label>
                                </div>
                            </div>
                        </div>
                        <div class="d-flex gap-2 mt-4">
                            <button type="submit" class="btn btn-primary">
                                <i class="bi bi-stars"></i> Sinh câu hỏi bằng AI
                            </button>
                        </div>
                    </form>
                </div>
            </div>

            <%
                List<Question> generated = (List<Question>) request.getAttribute("generated");
                if (generated != null && !generated.isEmpty()) {
            %>
            <div class="card">
                <div class="card-head">
                    <h5 class="card-title">AI đã sinh <%= generated.size() %> câu hỏi</h5>
                    <div class="d-flex gap-2">
                        <a href="${pageContext.request.contextPath}/teacher/questions" class="btn btn-sm btn-outline-primary">
                            Xem ngân hàng câu hỏi
                        </a>
                        <form method="post" class="d-inline"
                              action="${pageContext.request.contextPath}/teacher/ai-questions-delete-all"
                              onsubmit="return confirm('Xoá toàn bộ câu hỏi AI vừa sinh?');">
                            <button type="submit" class="btn btn-sm btn-outline-danger" id="deleteAllBtn">
                                <i class="bi bi-trash"></i> Xoá tất cả
                            </button>
                        </form>
                    </div>
                </div>
                <div class="card-body">
                    <%
                        int i = 1;
                        for (Question q : generated) {
                            pageContext.setAttribute("q", q);
                            // Câu hỏi đã lưu DB (questionId > 0) sẽ bị xoá thật;
                            // câu hỏi chưa lưu chỉ bị loại khỏi danh sách chờ duyệt.
                            boolean saved = q.getQuestionId() > 0;
                    %>
                    <div class="card mb-3" style="box-shadow:none;">
                        <div class="card-body">
                            <div class="d-flex align-items-start gap-2 mb-2">
                                <span class="topic-index"><%= i %></span>
                                <div class="flex-grow-1">
                                    <div class="fw-bold" style="line-height:1.6;">${q.content}</div>
                                    <% if (q.isMcq()) { %>
                                        <div class="row g-2 mt-2">
                                            <%
                                                String[] L = {"A", "B", "C", "D"};
                                                String[] V = {q.getOptionA(), q.getOptionB(), q.getOptionC(), q.getOptionD()};
                                                for (int k = 0; k < 4; k++) {
                                                    if (V[k] == null) continue;
                                            %>
                                            <div class="col-md-6">
                                                <div class="p-2 rounded" style="font-size:.85rem;
                                                    <%= L[k].equals(q.getCorrectAnswer())
                                                        ? "background:#f0fdf4;border:1px solid #86efac;font-weight:700;"
                                                        : "background:#f8fafc;border:1px solid #e4e9ef;" %>">
                                                    <strong><%= L[k] %>.</strong> ${V[k]}
                                                    <% if (L[k].equals(q.getCorrectAnswer())) { %>
                                                        <i class="bi bi-check-circle-fill text-success"></i>
                                                    <% } %>
                                                </div>
                                            </div>
                                            <% } %>
                                        </div>
                                    <% } %>
                                    <% if (q.getExplanation() != null && !q.getExplanation().trim().isEmpty()) { %>
                                        <div class="feedback-box feedback-info mt-2" style="font-size:.86rem;">
                                            <strong>Giải thích:</strong> ${q.explanation}
                                        </div>
                                    <% } %>
                                </div>
                                <div class="d-flex flex-column align-items-end gap-1 flex-shrink-0">
                                    <span class="badge-soft badge-ai">AI</span>
                                    <% if (saved) { %>
                                        <span class="badge-soft badge-mcq" title="Đã lưu vào ngân hàng câu hỏi">
                                            <i class="bi bi-check2-circle"></i> Đã lưu
                                        </span>
                                    <% } else { %>
                                        <span class="badge-soft" title="Chưa lưu vào ngân hàng câu hỏi">
                                            <i class="bi bi-hourglass-split"></i> Chờ duyệt
                                        </span>
                                    <% } %>
                                    <form method="post" class="d-inline"
                                          action="${pageContext.request.contextPath}/teacher/ai-question-delete"
                                          onsubmit="return confirm('Xoá câu hỏi này?');">
                                        <% if (saved) { %>
                                            <input type="hidden" name="id" value="${q.questionId}">
                                        <% } else { %>
                                            <input type="hidden" name="index" value="<%= i - 1 %>">
                                        <% } %>
                                        <button type="submit" class="btn btn-sm btn-outline-danger"
                                                title="<%= saved ? "Xoá vĩnh viễn khỏi ngân hàng câu hỏi" : "Bỏ câu hỏi này khỏi danh sách" %>">
                                            <i class="bi bi-trash"></i>
                                        </button>
                                    </form>
                                </div>
                            </div>
                        </div>
                    </div>
                    <% i++; } %>
                </div>
            </div>
            <% } %>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
<script>
    const subjectSelect = document.getElementById('subjectSelect');
    const topicSelect = document.getElementById('topicSelect');
    const allTopicOptions = Array.from(topicSelect.querySelectorAll('option[data-subject]'));
    function filterTopics() {
        const sv = subjectSelect.value;
        allTopicOptions.forEach(opt => {
            opt.style.display = (!sv || opt.dataset.subject === sv) ? '' : 'none';
        });
    }
    subjectSelect.addEventListener('change', filterTopics);
    filterTopics();
</script>
</body>
</html>
