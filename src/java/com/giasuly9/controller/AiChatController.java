package com.giasuly9.controller;

import com.giasuly9.filter.SessionUtil;
import com.giasuly9.model.User;
import com.giasuly9.util.AiService;
import com.giasuly9.util.FileUploadUtil;
import com.giasuly9.util.JsonUtil;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * AI Chat: giải bất kỳ bài nào thuộc kiến thức phổ thông.
 * Hỗ trợ cả request thường (form) và AJAX (JSON).
 */
public class AiChatController extends HttpServlet {

    private static final String SESSION_CHAT = "aiChatHistory";
    private static final int MAX_HISTORY = 30;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        showChat(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String path = request.getServletPath();

        if ("/ai-chat/save-key".equals(path)) {
            saveKey(request, response);
            return;
        }

        String message = request.getParameter("message");
        if (message == null) {
            message = "";
        }
        message = message.trim();

        // ===== Ảnh đề bài do học sinh gửi kèm (AI đọc ảnh để trả lời) =====
        // Đọc và chuẩn hóa ảnh 1 lần: nhận diện định dạng thật + thu nhỏ nếu quá lớn.
        FileUploadUtil.ImageData image = FileUploadUtil.readImage(request, "questionImage");
        byte[] imageBytes = (image != null) ? image.getBytes() : null;
        String imageMime = (image != null) ? image.getMime() : null;
        String imagePath = null;
        if (imageBytes != null) {
            imagePath = FileUploadUtil.saveImageBytes(imageBytes, imageMime,
                    FileUploadUtil.getUploadBasePath(request), "questions");
        }

        if (message.isEmpty() && imageBytes == null) {
            if (isAjax(request)) {
                sendJson(response, Map.of("success", false, "error", "Vui lòng nhập câu hỏi hoặc chụp ảnh đề bài."));
            } else {
                showChat(request, response);
            }
            return;
        }

        HttpSession sess = request.getSession();
        List<Map<String, String>> history = getHistory(sess);
        history.add(msg("user", message.isEmpty() ? "[Ảnh đề bài]" : message));

        int userId = SessionUtil.currentUserId(request);
        AiService ai = new AiService();

        String reply;
        if (!ai.isConfigured()) {
            reply = "Hệ thống AI chưa được cấu hình. Vui lòng liên hệ quản trị viên để thiết lập API key.";
        } else if (imageBytes != null) {
            // Có ảnh -> AI đọc ảnh đề bài rồi trả lời
            reply = ai.chatWithImage(history, imageBytes, imageMime, userId);
        } else {
            reply = ai.chat(history, userId);
        }

        history.add(msg("assistant", reply));
        sess.setAttribute(SESSION_CHAT, trimHistory(history));

        if (isAjax(request)) {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("success", true);
            data.put("reply", reply);
            if (imagePath != null) {
                data.put("imagePath", imagePath);
            }
            sendJson(response, data);
        } else {
            request.setAttribute("chatHistory", history);
            request.setAttribute("activeMenu", "ai-chat");
            request.getRequestDispatcher("/views/common/ai-chat.jsp").forward(request, response);
        }
    }

    // ==================================================================
    // Lưu API key nhanh (chỉ ADMIN)
    // ==================================================================
    private void saveKey(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        User user = SessionUtil.currentUser(request);
        if (user == null || !user.isAdmin()) {
            if (isAjax(request)) {
                sendJson(response, Map.of("success", false, "error", "Chỉ quản trị viên mới cấu hình được AI."));
            } else {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
            }
            return;
        }

        String apiKey = request.getParameter("apiKey");
        apiKey = (apiKey == null ? "" : apiKey.trim());
        String model = request.getParameter("model");
        model = (model == null || model.trim().isEmpty()) ? "gemini-3.5-flash-lite" : model.trim();

        if (apiKey.isEmpty()) {
            if (isAjax(request)) {
                sendJson(response, Map.of("success", false, "error", "Chưa nhập API key."));
            } else {
                response.sendRedirect(request.getContextPath() + "/ai-chat");
            }
            return;
        }

        com.giasuly9.dao.AiConfigDAO dao = new com.giasuly9.dao.AiConfigDAO();
        com.giasuly9.util.AiConfig cfg = new com.giasuly9.util.AiConfig();
        cfg.setProvider("GOOGLE");
        cfg.setApiKey(apiKey);
        cfg.setBaseUrl("https://generativelanguage.googleapis.com/v1beta/openai");
        cfg.setModel(model);
        cfg.setTemperature(0.7);
        cfg.setMaxTokens(2048);

        boolean saved = dao.save(cfg);
        String message = saved
                ? "Đã lưu API key. AI sẽ hoạt động ngay từ câu hỏi tiếp theo."
                : "Không lưu được cấu hình AI. Vui lòng thử lại.";

        if (isAjax(request)) {
            sendJson(response, Map.of("success", saved, "message", message));
        } else {
            request.getSession().setAttribute("flash", message);
            response.sendRedirect(request.getContextPath() + "/ai-chat");
        }
    }

    private void showChat(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<Map<String, String>> history = getHistory(request.getSession());
        request.setAttribute("chatHistory", history);
        request.setAttribute("activeMenu", "ai-chat");
        request.getRequestDispatcher("/views/common/ai-chat.jsp").forward(request, response);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, String>> getHistory(HttpSession sess) {
        Object o = sess.getAttribute(SESSION_CHAT);
        if (o instanceof List) {
            return new ArrayList<>((List<Map<String, String>>) o);
        }
        return new ArrayList<>();
    }

    private List<Map<String, String>> trimHistory(List<Map<String, String>> history) {
        while (history.size() > MAX_HISTORY) {
            history.remove(0);
        }
        return history;
    }

    private Map<String, String> msg(String role, String content) {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("role", role);
        m.put("content", content);
        return m;
    }

    private boolean isAjax(HttpServletRequest request) {
        String requestedWith = request.getHeader("X-Requested-With");
        return "XMLHttpRequest".equalsIgnoreCase(requestedWith)
                || "application/json".equalsIgnoreCase(request.getContentType());
    }

    private void sendJson(HttpServletResponse response, Object payload) throws IOException {
        response.setContentType("application/json; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.print(JsonUtil.stringify(payload));
        }
    }
}
