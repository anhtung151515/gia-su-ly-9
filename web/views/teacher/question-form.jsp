<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.giasuly9.model.Subject" %>
<%@ page import="com.giasuly9.model.Topic" %>
<!doctype html>
<html lang="vi">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Thêm câu hỏi | Gia Sư Lý 9</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Manrope:wght@300;400;600;700;800&display=swap" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/main.css" rel="stylesheet">
</head>
<body>
<div class="app-shell">
    <%@ include file="/views/common/_sidebar.jspf" %>
    <div class="app-main">
        <% request.setAttribute("pageTitle", "Ngân hàng câu hỏi"); %>
        <%@ include file="/views/common/_topbar.jspf" %>

        <div class="app-content" style="max-width:1000px;">
            <%
                com.giasuly9.model.Question q = (com.giasuly9.model.Question) request.getAttribute("question");
                boolean isEdit = (q != null);
            %>
            <div class="page-head">
                <h2 class="page-title"><%= isEdit ? "Cập nhật câu hỏi" : "Thêm câu hỏi mới" %></h2>
                <div class="page-sub">Tạo câu hỏi trắc nghiệm (4 đáp án) hoặc câu hỏi tự luận</div>
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
                    <form method="post" action="${pageContext.request.contextPath}/teacher/question-save">
                        <% if (isEdit) { %>
                            <input type="hidden" name="id" value="${q.questionId}">
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
                                        <%= isEdit && q.getSubjectId() == s.getSubjectId() ? "selected" : "" %>>
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
                                        <%= isEdit && q.getTopicId() == t.getTopicId() ? "selected" : "" %>>
                                        ${t.subjectName} — ${t.name}
                                    </option>
                                    <% } } %>
                                </select>
                            </div>

                            <div class="col-12">
                                <label class="form-label">Nội dung câu hỏi *</label>
                                <textarea name="content" class="form-control" rows="3" required
                                          placeholder="VD: Một vật nặng 10 kg được nâng lên cao 2 m. Công của trọng lực là bao nhiêu?"><%= isEdit ? q.getContent() : "" %></textarea>
                            </div>

                            <div class="col-md-4">
                                <label class="form-label">Loại câu hỏi *</label>
                                <select name="questionType" class="form-select" id="questionType">
                                    <option value="MCQ" <%= isEdit && q.isMcq() ? "selected" : "" %>>Trắc nghiệm</option>
                                    <option value="ESSAY" <%= isEdit && q.isEssay() ? "selected" : "" %>>Tự luận</option>
                                </select>
                            </div>
                            <div class="col-md-4">
                                <label class="form-label">Mức độ</label>
                                <select name="difficulty" class="form-select">
                                    <option value="EASY" <%= isEdit && "EASY".equals(q.getDifficulty()) ? "selected" : "" %>>Dễ</option>
                                    <option value="MEDIUM" <%= !isEdit || "MEDIUM".equals(q.getDifficulty()) ? "selected" : "" %>>Trung bình</option>
                                    <option value="HARD" <%= isEdit && "HARD".equals(q.getDifficulty()) ? "selected" : "" %>>Khó</option>
                                </select>
                            </div>
                            <div class="col-md-4" id="correctAnswerBox">
                                <label class="form-label">Đáp án đúng *</label>
                                <select name="correctAnswer" class="form-select">
                                    <option value="">-- Chọn --</option>
                                    <%
                                        String[] letters = {"A", "B", "C", "D"};
                                        for (String L : letters) {
                                            pageContext.setAttribute("L", L);

                                    %>
                                    <option value="<%= L %>" <%= isEdit && L.equals(q.getCorrectAnswer()) ? "selected" : "" %>>
                                        <%= L %>
                                    </option>
                                    <% } %>
                                </select>
                            </div>

                            <div class="col-12" id="optionsBox">
                                <div class="row g-3">
                                    <%
                                        String[] letters2 = {"A", "B", "C", "D"};
                                        String[] vals = {q == null ? null : q.getOptionA(), q == null ? null : q.getOptionB(),
                                            q == null ? null : q.getOptionC(), q == null ? null : q.getOptionD()};
                                        for (int i = 0; i < 4; i++) {
                                    %>
                                    <div class="col-md-6">
                                        <label class="form-label">Phương án <%= letters2[i] %></label>
                                        <input type="text" name="option<%= letters2[i] %>" class="form-control"
                                               value="<%= vals[i] != null ? vals[i] : "" %>"
                                               placeholder="Nội dung phương án <%= letters2[i] %>">
                                    </div>
                                    <% } %>
                                </div>
                            </div>

                            <div class="col-12">
                                <label class="form-label">Lời giải / gợi ý</label>
                                <textarea name="explanation" class="form-control" rows="3"
                                          placeholder="Giải thích ngắn gọn đáp án và lý do"><%= isEdit && q.getExplanation() != null ? q.getExplanation() : "" %></textarea>
                                <div class="form-hint">Hiển thị cho học sinh sau khi trả lời, và dùng làm gợi ý cho AI giải từng bước.</div>
                            </div>
                        </div>

                        <div class="d-flex gap-2 mt-4">
                            <button type="submit" class="btn btn-primary">
                                <i class="bi bi-check-lg"></i> <%= isEdit ? "Cập nhật" : "Thêm câu hỏi" %>
                            </button>
                            <a href="${pageContext.request.contextPath}/teacher/questions" class="btn btn-outline-secondary">Hủy</a>
                        </div>
                    </form>
                </div>
            </div>
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

    const questionType = document.getElementById('questionType');
    function toggleType() {
        const mcq = questionType.value === 'MCQ';
        document.getElementById('optionsBox').style.display = mcq ? '' : 'none';
        document.getElementById('correctAnswerBox').style.display = mcq ? '' : 'none';
    }
    questionType.addEventListener('change', toggleType);
    toggleType();
</script>
</body>
</html>
