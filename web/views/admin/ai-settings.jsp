<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.giasuly9.util.AiConfig" %>
<!doctype html>
<html lang="vi">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Cấu hình AI | Gia Sư Lý 9</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Manrope:wght@300;400;600;700;800&display=swap" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/main.css" rel="stylesheet">
</head>
<body>
<div class="app-shell">
    <%@ include file="/views/common/_sidebar.jspf" %>
    <div class="app-main">
        <% request.setAttribute("pageTitle", "Cấu hình API AI"); %>
        <%@ include file="/views/common/_topbar.jspf" %>

        <div class="app-content">
            <% if (session.getAttribute("flash") != null) { %>
                <div class="alert alert-success alert-dismissible fade show">
                    <i class="bi bi-check-circle"></i> <%= session.getAttribute("flash") %>
                    <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                </div>
                <% session.removeAttribute("flash"); %>
            <% } %>

            <%
                AiConfig cfg = (AiConfig) request.getAttribute("aiConfig");
                if (cfg == null) { cfg = new AiConfig(); }
            %>

            <div class="page-head">
                <h2 class="page-title">Cấu hình API AI</h2>
                <div class="page-sub">Dùng cho giải bài, chấm tự luận, hướng dẫn từng bước và sinh câu hỏi</div>
            </div>

            <div class="alert alert-info">
                <i class="bi bi-info-circle"></i>
                Hệ thống dùng API tương thích OpenAI. Nếu dùng provider khác (OpenRouter, Gemini OpenAI-compat, DeepSeek...)
                chỉ cần đổi <strong>Base URL</strong> và <strong>Model</strong> cho phù hợp.
            </div>

            <form method="post" action="${pageContext.request.contextPath}/admin/ai-settings-save">
                <div class="row g-3">
                    <div class="col-lg-6">
                        <div class="card h-100">
                            <div class="card-head">
                                <h5 class="card-title">Kết nối</h5>
                            </div>
                            <div class="card-body">
                                <div class="mb-3">
                                    <label class="form-label">Provider</label>
                                    <select name="provider" class="form-select">
                                        <option value="OPENAI" <%= "OPENAI".equals(cfg.getProvider()) ? "selected" : "" %>>OpenAI</option>
                                        <option value="OPENROUTER" <%= "OPENROUTER".equals(cfg.getProvider()) ? "selected" : "" %>>OpenRouter</option>
                                        <option value="DEEPSEEK" <%= "DEEPSEEK".equals(cfg.getProvider()) ? "selected" : "" %>>DeepSeek</option>
                                        <option value="GOOGLE" <%= "GOOGLE".equals(cfg.getProvider()) ? "selected" : "" %>>Google (compat)</option>
                                        <option value="CUSTOM" <%= "CUSTOM".equals(cfg.getProvider()) ? "selected" : "" %>>Tuỳ chỉnh</option>
                                    </select>
                                </div>
                                <div class="mb-3">
                                    <label class="form-label">Base URL</label>
                                    <input type="text" name="baseUrl" class="form-control"
                                           value="<%= cfg.getBaseUrl() != null ? cfg.getBaseUrl() : "" %>"
                                           placeholder="https://api.openai.com/v1">
                                    <div class="form-hint">Không cần gõ <code>/chat/completions</code>, hệ thống tự thêm.</div>
                                </div>
                                <div class="mb-3">
                                    <label class="form-label">API Key</label>
                                    <input type="password" name="apiKey" class="form-control"
                                           value="<%= cfg.getApiKey() != null ? cfg.getApiKey() : "" %>"
                                           placeholder="sk-...">
                                    <div class="form-hint">Lưu ý: key được hiển thị lại dạng chữ thường khi mở trang này.</div>
                                </div>
                                <div class="mb-3">
                                    <label class="form-label">Model</label>
                                    <input type="text" name="model" class="form-control"
                                           value="<%= cfg.getModel() != null ? cfg.getModel() : "" %>"
                                           placeholder="gpt-4o-mini">
                                </div>
                                <div class="row g-3">
                                    <div class="col-6">
                                        <label class="form-label">Temperature</label>
                                        <input type="number" name="temperature" class="form-control" step="0.1"
                                               min="0" max="2" value="<%= cfg.getTemperature() %>">
                                    </div>
                                    <div class="col-6">
                                        <label class="form-label">Max tokens</label>
                                        <input type="number" name="maxTokens" class="form-control" min="256"
                                               step="256" value="<%= cfg.getMaxTokens() %>">
                                    </div>
                                </div>
                                <button type="submit" class="btn btn-primary w-100 mt-3">
                                    <i class="bi bi-check-lg"></i> Lưu cấu hình
                                </button>
                            </div>
                        </div>
                    </div>

                    <div class="col-lg-6">
                        <div class="card h-100">
                            <div class="card-head">
                                <h5 class="card-title">Prompt hệ thống</h5>
                            </div>
                            <div class="card-body">
                                <div class="mb-3">
                                    <label class="form-label">Prompt chung</label>
                                    <textarea name="systemPrompt" class="form-control" rows="5"
                                              placeholder="Bạn là gia sư Vật lý lớp 9..."><%= cfg.getSystemPrompt() != null ? cfg.getSystemPrompt() : "" %></textarea>
                                    <div class="form-hint">Để trống sẽ dùng prompt mặc định của hệ thống.</div>
                                </div>
                                <div class="mb-3">
                                    <label class="form-label">Prompt chế độ giải từng bước (tutor)</label>
                                    <textarea name="tutorPrompt" class="form-control" rows="4"
                                              placeholder="Hướng dẫn từng bước, dừng lại để học sinh nhập dữ liệu..."><%= cfg.getTutorPrompt() != null ? cfg.getTutorPrompt() : "" %></textarea>
                                </div>
                                <div class="mb-3">
                                    <label class="form-label">Prompt sinh câu hỏi</label>
                                    <textarea name="questionPrompt" class="form-control" rows="4"
                                              placeholder="Sinh câu hỏi theo chủ đề, trả về JSON..."><%= cfg.getQuestionPrompt() != null ? cfg.getQuestionPrompt() : "" %></textarea>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </form>

            <div class="card mt-3">
                <div class="card-head">
                    <h5 class="card-title">Kiểm tra kết nối</h5>
                </div>
                <div class="card-body d-flex align-items-center gap-3 flex-wrap">
                    <div class="text-muted" style="font-size:.88rem;">
                        Gửi một yêu cầu thử để kiểm tra API key, base URL và model có hợp lệ không.
                    </div>
                    <form method="post" action="${pageContext.request.contextPath}/admin/ai-test" class="ms-auto">
                        <button type="submit" class="btn btn-outline-primary">
                            <i class="bi bi-plug"></i> Kiểm tra kết nối
                        </button>
                    </form>
                </div>
            </div>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
