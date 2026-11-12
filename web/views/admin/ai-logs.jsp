<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.giasuly9.model.AiLog" %>
<!doctype html>
<html lang="vi">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Nhật ký AI | Gia Sư Lý 9</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Manrope:wght@300;400;600;700;800&display=swap" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/main.css" rel="stylesheet">
</head>
<body>
<div class="app-shell">
    <%@ include file="/views/common/_sidebar.jspf" %>
    <div class="app-main">
        <% request.setAttribute("pageTitle", "Nhật ký AI"); %>
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
                <h2 class="page-title">Nhật ký sử dụng AI</h2>
                <div class="page-sub">100 lượt gọi AI gần nhất, gồm prompt, phản hồi và số token</div>
            </div>

            <%
                List<AiLog> logs = (List<AiLog>) request.getAttribute("logs");
                if (logs == null || logs.isEmpty()) {
            %>
                <div class="card">
                    <div class="empty-state">
                        <i class="bi bi-journal-text"></i>
                        <h5>Chưa có lượt gọi AI nào</h5>
                    </div>
                </div>
            <% } else { %>
                <div class="card">
                    <div class="table-wrap">
                        <table class="table">
                            <thead>
                            <tr>
                                <th style="width:50px;">#</th>
                                <th>Người dùng</th>
                                <th>Tính năng</th>
                                <th>Token</th>
                                <th>Kết quả</th>
                                <th>Thời gian</th>
                                <th style="width:70px;"></th>
                            </tr>
                            </thead>
                            <tbody>
                            <%
                                int i = 1;
                                for (AiLog l : logs) {
                                    pageContext.setAttribute("l", l);

                            %>
                            <tr>
                                <td class="text-muted" style="font-size:.83rem;"><%= i %></td>
                                <td class="text-muted" style="font-size:.85rem;">
                                    <%= l.getFullName() == null ? "—" : l.getFullName() %>
                                </td>
                                <td>
                                    <span class="badge-soft badge-ai">${l.feature}</span>
                                </td>
                                <td class="text-muted" style="font-size:.83rem;"><%= l.getTokensUsed() %></td>
                                <td>
                                    <span class="badge-soft <%= l.isSuccess() ? "badge-on" : "badge-off" %>">
                                        <%= l.isSuccess() ? "Thành công" : "Lỗi" %>
                                    </span>
                                </td>
                                <td class="text-muted" style="font-size:.8rem;">
                                    <%= l.getCreatedAt() == null ? "—" : l.getCreatedAt() %>
                                </td>
                                <td class="text-end">
                                    <button type="button" class="btn btn-sm btn-outline-primary"
                                            data-bs-toggle="modal" data-bs-target="#log<%= l.getLogId() %>"
                                            title="Xem chi tiết"><i class="bi bi-eye"></i></button>
                                </td>
                            </tr>

                            <div class="modal fade" id="log<%= l.getLogId() %>" tabindex="-1">
                                <div class="modal-dialog modal-lg modal-dialog-scrollable">
                                    <div class="modal-content">
                                        <div class="modal-header">
                                            <h6 class="modal-title fw-bold">
                                                ${l.feature} — <%= l.getFullName() == null ? "—" : l.getFullName() %>
                                            </h6>
                                            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                                        </div>
                                        <div class="modal-body">
                                            <div class="mb-3">
                                                <div class="stat-label">Prompt gửi đi</div>
                                                <div class="p-3 rounded" style="background:#f8fafc;white-space:pre-wrap;font-size:.85rem;">
                                                    <%= l.getPromptText() == null ? "—" : l.getPromptText() %>
                                                </div>
                                            </div>
                                            <div>
                                                <div class="stat-label">Phản hồi của AI</div>
                                                <div class="p-3 rounded" style="background:#f8fafc;white-space:pre-wrap;font-size:.85rem;">
                                                    <%= l.getResponseText() == null ? (l.getErrorMessage() == null ? "—" : l.getErrorMessage()) : l.getResponseText() %>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <% i++; } %>
                            </tbody>
                        </table>
                    </div>
                </div>
            <% } %>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
