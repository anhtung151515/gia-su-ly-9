<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.giasuly9.model.PracticeSession" %>
<%@ page import="com.giasuly9.model.PracticeAnswer" %>
<%@ page import="com.giasuly9.util.AutoScoreService" %>
<%
    PracticeSession psess = (PracticeSession) request.getAttribute("session");
    pageContext.setAttribute("psess", psess);
    List<PracticeAnswer> answers = (List<PracticeAnswer>) request.getAttribute("answers");
    if (answers == null) { answers = java.util.Collections.emptyList(); }

    // Điểm phiên lưu theo thang 10
    double score10 = psess != null ? psess.getScore() : 0;
    double percent = AutoScoreService.toPercent(score10);
    String grade = AutoScoreService.grade(score10);
    String advice = AutoScoreService.advice(score10);
    String bannerClass = score10 >= 8 ? "good" : (score10 >= 5 ? "ok" : "bad");
%>
<!doctype html>
<html lang="vi">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Kết quả luyện tập | Gia Sư Lý 9</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Manrope:wght@300;400;600;700;800&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/katex@0.16.9/dist/katex.min.css">
    <link href="${pageContext.request.contextPath}/assets/css/main.css" rel="stylesheet">
    <script src="https://cdn.jsdelivr.net/npm/marked@12.0.2/marked.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/katex@0.16.9/dist/katex.min.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/md-render.js"></script>
</head>
<body>
<%@ include file="/views/common/_header.jspf" %>

<section class="page">
    <div class="container practice-shell">
        <div class="page-head d-flex justify-content-between align-items-end flex-wrap gap-2">
            <div>
                <h2 class="page-title">Kết quả luyện tập</h2>
                <div class="page-sub">${psess.topicName} · Vật lý 9</div>
            </div>
            <a href="${pageContext.request.contextPath}/student/history" class="btn btn-sm btn-outline-primary">
                <i class="bi bi-arrow-left"></i> Lịch sử
            </a>
        </div>

        <!-- ==================== TỰ CHẤM ĐIỂM ==================== -->
        <div class="score-banner <%= bannerClass %> mb-4">
            <div class="score-ring">
                <div>
                    <b><%= String.format("%.1f", score10) %></b>
                    <span>điểm / 10</span>
                </div>
            </div>
            <div class="score-banner-info">
                <h3>
                    Xếp loại: <%= grade %>
                    <span class="score-badge-out"><%= String.format("%.0f", percent) %>%</span>
                </h3>
                <p><%= advice %></p>
                <span class="score-badge-out">
                    <i class="bi bi-stars"></i>
                    Điểm do hệ thống tự chấm: trắc nghiệm đúng 10đ/sai 0đ, tự luận AI chấm thang 0-10
                </span>
            </div>
        </div>

        <!-- Tổng kết -->
        <div class="row g-3 mb-4">
            <div class="col-6 col-lg-3">
                <div class="stat-card">
                    <div class="stat-icon blue"><i class="bi bi-list-check"></i></div>
                    <div>
                        <div class="stat-value"><%= psess.getTotalQuestions() %></div>
                        <div class="stat-label">Tổng câu</div>
                    </div>
                </div>
            </div>
            <div class="col-6 col-lg-3">
                <div class="stat-card">
                    <div class="stat-icon green"><i class="bi bi-check-circle"></i></div>
                    <div>
                        <div class="stat-value"><%= psess.getCorrectCount() %></div>
                        <div class="stat-label">Đúng</div>
                    </div>
                </div>
            </div>
            <div class="col-6 col-lg-3">
                <div class="stat-card">
                    <div class="stat-icon red"><i class="bi bi-x-circle"></i></div>
                    <div>
                        <div class="stat-value"><%= psess.getWrongCount() %></div>
                        <div class="stat-label">Sai</div>
                    </div>
                </div>
            </div>
            <div class="col-6 col-lg-3">
                <div class="stat-card">
                    <div class="stat-icon orange"><i class="bi bi-award"></i></div>
                    <div>
                        <div class="stat-value"><%= String.format("%.1f", score10) %>/10</div>
                        <div class="stat-label">Điểm tự chấm</div>
                    </div>
                </div>
            </div>
        </div>

        <%
            if (answers.isEmpty()) {
        %>
            <div class="card">
                <div class="empty-state">
                    <i class="bi bi-inbox"></i>
                    <p class="mb-0">Không có dữ liệu câu trả lời.</p>
                </div>
            </div>
        <% } else { %>
            <div class="d-flex justify-content-between align-items-center flex-wrap gap-2 mb-3">
                <h5 class="card-title mb-0">
                    <i class="bi bi-list-check"></i> Chi tiết từng câu
                    <span class="badge-soft badge-mcq ms-1"><%= answers.size() %> câu</span>
                </h5>
                <div class="d-flex gap-2">
                    <button class="btn btn-sm btn-outline-primary" type="button" data-bs-toggle="collapse"
                            data-bs-target="#detailAll" aria-expanded="false" aria-controls="detailAll">
                        <i class="bi bi-chevron-down"></i> Mở tất cả
                    </button>
                    <button class="btn btn-sm btn-outline-secondary" type="button" id="expandWrong">
                        <i class="bi bi-x-circle"></i> Xem câu sai
                    </button>
                </div>
            </div>

            <div class="collapse" id="detailAll">
            <%
            int n = 0;
            for (PracticeAnswer a : answers) {
                n++;
                pageContext.setAttribute("a", a);
                double aScore = a.getScore();
                String scoreClass = aScore >= 8 ? "good" : (aScore >= 5 ? "mid" : "bad");
                String accId = "ans" + n;
        %>
        <div class="card mb-3 answer-item" data-correct="<%= a.isCorrect() %>">
            <div class="card-header bg-white p-0">
                <button class="btn btn-link w-100 text-start text-decoration-none p-3 d-flex align-items-center gap-3"
                        type="button" data-bs-toggle="collapse" data-bs-target="#<%= accId %>"
                        aria-expanded="false" aria-controls="<%= accId %>">
                    <span class="stat-icon <%= a.isCorrect() ? "green" : "red" %> flex-shrink-0">
                        <i class="bi <%= a.isCorrect() ? "bi-check-lg" : "bi-x-lg" %>"></i>
                    </span>
                    <span class="flex-grow-1">
                        <span class="d-block fw-bold" style="line-height:1.6;color:var(--text);">
                            Câu <%= n %>: ${a.questionContent}
                        </span>
                        <span class="d-flex gap-2 mt-2 flex-wrap">
                            <% if (a.getSelectedAnswer() != null) { %>
                                <span class="badge-soft <%= a.isCorrect() ? "badge-easy" : "badge-hard" %>">
                                    Bạn chọn: ${a.selectedAnswer}
                                </span>
                                <span class="badge-soft badge-mcq">Đáp án: ${a.correctAnswer}</span>
                            <% } else if ("ESSAY".equals(psess.getPracticeType())) { %>
                                <span class="badge-soft badge-essay">Bài tự luận</span>
                            <% } %>
                        </span>
                    </span>
                    <span class="answer-score <%= scoreClass %> flex-shrink-0">
                        <i class="bi bi-award"></i> <%= String.format("%.1f", aScore) %>/10
                    </span>
                    <i class="bi bi-chevron-down text-muted flex-shrink-0"></i>
                </button>
            </div>
            <div class="collapse" id="<%= accId %>">
                <div class="card-body p-4 pt-0">
                    <% if (a.getUserAnswer() != null && !a.getUserAnswer().trim().isEmpty()) { %>
                        <div class="p-3 rounded mb-2" style="background:#f8fafc;font-size:.9rem;white-space:pre-wrap;">
                            <strong>Bài làm của bạn:</strong><br>
                            <%= a.getUserAnswer() %>
                        </div>
                    <% } %>

                    <% if (a.getImagePath() != null) { %>
                        <div class="mb-2">
                            <img src="${pageContext.request.contextPath}${a.imagePath}" class="answer-image" alt="Bài làm">
                        </div>
                    <% } %>

                    <% if (a.getAiFeedback() != null && !a.getAiFeedback().trim().isEmpty()) { %>
                        <div class="feedback-box <%= a.isCorrect() ? "feedback-correct" : "feedback-wrong" %>">
                            <h6><i class="bi bi-stars"></i> Nhận xét của AI gia sư</h6>
                            <div class="md-body"><%= com.giasuly9.util.HtmlEscape.escape(a.getAiFeedback()) %></div>
                        </div>
                    <% } %>

                    <% if (a.getAiFeedback() == null || a.getAiFeedback().trim().isEmpty()) { %>
                        <div class="feedback-box feedback-info">
                            <h6><i class="bi bi-lightbulb"></i> Gợi ý</h6>
                            <div class="md-body"><%= com.giasuly9.util.HtmlEscape.escape(
                                    a.getQuestionExplanation() != null && !a.getQuestionExplanation().trim().isEmpty()
                                    ? a.getQuestionExplanation() : "Hãy xem lại bài giảng chủ đề này để nắm rõ hơn.") %></div>
                        </div>
                    <% } %>
                </div>
            </div>
        </div>
        <% } } %>
            </div>

        <div class="text-center mt-4 d-flex gap-2 justify-content-center flex-wrap">
            <a href="${pageContext.request.contextPath}/student/practice-setup?topicId=${psess.topicId}&type=${psess.practiceType}"
               class="btn btn-primary">
                <i class="bi bi-arrow-repeat"></i> Luyện lại chủ đề này
            </a>
            <a href="${pageContext.request.contextPath}/ai-chat" class="btn btn-outline-primary">
                <i class="bi bi-stars"></i> Hỏi AI thêm
            </a>
        </div>
    </div>
</section>

<%@ include file="/views/common/_footer.jspf" %>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
<script>
    // Render nội dung AI (markdown + công thức LaTeX)
    document.querySelectorAll('.md-body').forEach(el => {
        const raw = el.textContent;
        if (window.MdRender) {
            el.innerHTML = window.MdRender.render(raw);
        }
    });

    // Mở/đóng chi tiết từng câu
    document.querySelectorAll('.answer-item [data-bs-toggle="collapse"]').forEach(btn => {
        const target = document.querySelector(btn.getAttribute('data-bs-target'));
        if (target) {
            target.addEventListener('show.bs.collapse', () => {
                const icon = btn.querySelector('.bi-chevron-down');
                if (icon) icon.classList.replace('bi-chevron-down', 'bi-chevron-up');
            });
            target.addEventListener('hide.bs.collapse', () => {
                const icon = btn.querySelector('.bi-chevron-down, .bi-chevron-up');
                if (icon) icon.classList.replace('bi-chevron-up', 'bi-chevron-down');
            });
        }
    });

    // Nút "Xem câu sai" -> mở toàn bộ các câu sai
    const btnWrong = document.getElementById('expandWrong');
    if (btnWrong) {
        btnWrong.addEventListener('click', () => {
            const all = document.getElementById('detailAll');
            if (all && !all.classList.contains('show')) {
                all.classList.add('show');
            }
            let opened = 0;
            document.querySelectorAll('.answer-item[data-correct="false"]').forEach(item => {
                const btn = item.querySelector('[data-bs-toggle="collapse"]');
                const target = btn ? document.querySelector(btn.getAttribute('data-bs-target')) : null;
                if (target && !target.classList.contains('show')) {
                    bootstrap.Collapse.getOrCreateInstance(target).show();
                    opened++;
                }
            });
            if (opened === 0) {
                btnWrong.innerHTML = '<i class="bi bi-check-circle"></i> Không có câu sai';
            }
        });
    }
</script>
</body>
</html>
