package com.giasuly9.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Client gọi OpenAI-compatible Chat Completions API.
 * Không dùng thư viện ngoài: dựng HTTP request bằng java.net + parse bằng JsonUtil.
 */
public class AiClient {

    private static final Logger LOGGER = Logger.getLogger(AiClient.class.getName());
    /**
     * Timeout gọi AI. Trước đây là 120s khiến một câu hỏi có thể treo 2 phút nếu
     * model bị lỗi 429/timeout. 25s là mức hợp lý cho việc chấm 1 câu;
     * phần chờ lâu đã được chuyển sang luồng nền trong PracticeController.
     */
    private static final int TIMEOUT_MS = 25_000;

    /**
     * Timeout cho các request có kèm ảnh. Xử lý ảnh (đọc đề bài, OCR) mất lâu
     * hơn nhiều so với chat text thuần - thường 20-40 giây - nên cần timeout riêng.
     */
    private static final int TIMEOUT_IMAGE_MS = 120_000;

    /** Lỗi cuối cùng gặp phải, dùng để hiển thị thông báo rõ ràng cho người dùng. */
    private String lastError;

    /** Mã lỗi HTTP của lần gọi gần nhất (0 = chưa có lỗi). */
    private int lastErrorCode;

    private final AiConfig config;

    public AiClient(AiConfig config) {
        this.config = config;
    }

    public String getLastError() {
        return lastError;
    }

    public int getLastErrorCode() {
        return lastErrorCode;
    }

    /**
     * Danh sách model dùng để thử lại khi model chính gặp lỗi tạm thời
     * (503 quá tải, 429 vượt giới hạn, 404 model không còn khả dụng...).
     * Chỉ áp dụng với nhà cung cấp Google Gemini.
     */
    private List<String> candidateModels() {
        List<String> list = new ArrayList<>();
        String primary = config.getModel();
        if (primary != null && !primary.trim().isEmpty()) {
            list.add(primary.trim());
        }
        if ("GOOGLE".equalsIgnoreCase(config.getProvider())) {
            // Thứ tự này được kiểm chứng: gemini-3.5-flash hay bị 429 với key free,
            // còn gemini-3.5-flash-lite trả lời ổn định ~1.5-2.5s.
            for (String m : new String[]{"gemini-3.5-flash-lite", "gemini-3.1-flash-lite",
                    "gemini-3-flash-preview", "gemini-3.5-flash"}) {
                if (!list.contains(m)) {
                    list.add(m);
                }
            }
        }
        if (list.isEmpty()) {
            list.add("gemini-3.5-flash-lite");
        }
        return list;
    }

    /**
     * Lỗi có thể thử lại với model khác hay không (lỗi tạm thời của hạ tầng).
     */
    private boolean isRetryable() {
        return lastErrorCode == 429 || lastErrorCode == 500 || lastErrorCode == 502
                || lastErrorCode == 503 || lastErrorCode == 404;
    }

    /**
     * Gửi 1 lượt chat và trả về nội dung trả lời của assistant.
     *
     * @param systemPrompt system prompt (có thể null)
     * @param messages    lịch sử hội thoại dạng [{role, content}, ...]
     * @return nội dung trả lời, hoặc null nếu lỗi
     */
    public String chat(String systemPrompt, List<Map<String, String>> messages) {
        if (!config.isConfigured()) {
            lastError = "Chưa cấu hình API key / model cho AI.";
            lastErrorCode = 0;
            LOGGER.warning(lastError);
            return null;
        }
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("model", config.getModel());
            payload.put("temperature", config.getTemperature());
            payload.put("max_tokens", config.getMaxTokens());

            List<Map<String, Object>> msgs = new ArrayList<>();
            if (systemPrompt != null && !systemPrompt.trim().isEmpty()) {
                Map<String, Object> sys = new LinkedHashMap<>();
                sys.put("role", "system");
                sys.put("content", systemPrompt);
                msgs.add(sys);
            }
            for (Map<String, String> m : messages) {
                Map<String, Object> mm = new LinkedHashMap<>();
                mm.put("role", m.get("role"));
                mm.put("content", m.get("content"));
                msgs.add(mm);
            }
            payload.put("messages", msgs);

            String body = JsonUtil.stringify(payload);
            String raw = post(config.chatEndpoint(), body);
            if (raw == null && isRetryable()) {
                raw = retryWithOtherModels(body);
            }
            return extractContent(raw);

        } catch (Exception e) {
            lastError = e.getMessage();
            LOGGER.log(Level.SEVERE, "Lỗi gọi AI API", e);
            return null;
        }
    }

    /**
     * Thử lại cùng payload với các model dự phòng khi model chính gặp lỗi tạm thời.
     *
     * <p>Khi gặp 429 (vượt giới hạn tần suất) thì chờ một chút trước khi thử model
     * kế tiếp - nếu không, mọi lần thử đều nhận 429 và học sinh phải chờ rất lâu.
     */
    private String retryWithOtherModels(String body) {
        return retryWithOtherModels(body, TIMEOUT_MS);
    }

    private String retryWithOtherModels(String body, int timeoutMs) {
        List<String> models = candidateModels();
        for (int i = 1; i < models.size(); i++) {
            String alt = models.get(i);
            if (lastErrorCode == 429) {
                // Rate limit: nghỉ ngắn rồi thử model nhẹ hơn
                sleepQuietly(700);
            }
            try {
                Map<String, Object> payload = JsonUtil.parseObject(body);
                payload.put("model", alt);
                String raw = post(config.chatEndpoint(), JsonUtil.stringify(payload), timeoutMs);
                if (raw != null) {
                    LOGGER.info("Fallback sang model " + alt + " thành công");
                    return raw;
                }
            } catch (Exception e) {
                LOGGER.warning("Fallback model " + alt + " lỗi: " + e.getMessage());
            }
            if (!isRetryable()) {
                break;
            }
        }
        return null;
    }

    private void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Chat 1 lượt đơn giản với system + user prompt.
     */
    public String complete(String systemPrompt, String userPrompt) {
        List<Map<String, String>> msgs = new ArrayList<>();
        Map<String, String> u = new LinkedHashMap<>();
        u.put("role", "user");
        u.put("content", userPrompt);
        msgs.add(u);
        return chat(systemPrompt, msgs);
    }

    /**
     * Gửi payload dựng sẵn (dùng cho vision, response_format...).
     */
    public String send(Map<String, Object> payload) {
        if (!config.isConfigured()) {
            lastError = "Chưa cấu hình API key / model cho AI.";
            lastErrorCode = 0;
            return null;
        }
        try {
            Map<String, Object> data = payload;
            if (data == null || !data.containsKey("model")) {
                data = new LinkedHashMap<>();
                if (payload != null) {
                    data.putAll(payload);
                }
                data.put("model", config.getModel());
            }
            String body = JsonUtil.stringify(data);
            // Request có ảnh cần timeout dài hơn (OCR/đọc đề mất 20-40s)
            int timeout = containsImage(data) ? TIMEOUT_IMAGE_MS : TIMEOUT_MS;
            String raw = post(config.chatEndpoint(), body, timeout);
            if (raw == null && isRetryable()) {
                raw = retryWithOtherModels(body, timeout);
            }
            return extractContent(raw);
        } catch (Exception e) {
            lastError = e.getMessage();
            LOGGER.log(Level.SEVERE, "Lỗi gọi AI API", e);
            return null;
        }
    }

    /**
     * Kiểm tra payload có chứa ảnh (image_url) hay không.
     */
    private boolean containsImage(Map<String, Object> payload) {
        try {
            Object msgs = payload.get("messages");
            if (!(msgs instanceof List)) {
                return false;
            }
            for (Object m : (List<?>) msgs) {
                if (!(m instanceof Map)) {
                    continue;
                }
                Object content = ((Map<?, ?>) m).get("content");
                if (content instanceof List) {
                    for (Object part : (List<?>) content) {
                        if (part instanceof Map
                                && "image_url".equals(((Map<?, ?>) part).get("type"))) {
                            return true;
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    private String post(String endpoint, String body) throws IOException {
        return post(endpoint, body, TIMEOUT_MS);
    }

    private String post(String endpoint, String body, int timeoutMs) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(endpoint).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        conn.setRequestProperty("Authorization", "Bearer " + config.getApiKey());
        conn.setConnectTimeout(timeoutMs);
        conn.setReadTimeout(timeoutMs);
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
        }

        int code = conn.getResponseCode();
        InputStream is = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();
        String response = readAll(is);

        if (code < 200 || code >= 300) {
            lastErrorCode = code;
            lastError = friendlyError(code, response);
            LOGGER.severe("AI API trả về mã " + code + ": " + response);
            return null;
        }
        lastErrorCode = 0;
        lastError = null;
        return response;
    }

    /**
     * Dịch lỗi API sang thông báo tiếng Việt dễ hiểu.
     */
    private String friendlyError(int code, String response) {
        String detail = "";
        try {
            Map<String, Object> obj = JsonUtil.parseObject(response);
            Object err = obj.get("error");
            if (err instanceof Map) {
                Object msg = ((Map<?, ?>) err).get("message");
                if (msg != null) {
                    detail = String.valueOf(msg);
                }
            }
        } catch (Exception ignored) {
            detail = response == null ? "" : response;
        }
        if (detail.length() > 300) {
            detail = detail.substring(0, 300) + "...";
        }
        switch (code) {
            case 400:
                return "Yêu cầu không hợp lệ (400). Kiểm tra lại model hoặc base URL. " + detail;
            case 401:
            case 403:
                return "API key không hợp lệ hoặc chưa được cấp quyền (" + code + "). " + detail;
            case 404:
                return "Không tìm thấy endpoint/model (" + code + "). Kiểm tra Base URL và Model. " + detail;
            case 429:
                return "Đã vượt giới hạn tần suất (429). Hãy thử lại sau ít phút. " + detail;
            case 500:
            case 502:
            case 503:
                return "Máy chủ AI đang lỗi (" + code + "). Hãy thử lại sau. " + detail;
            default:
                return "Lỗi " + code + " khi gọi AI. " + detail;
        }
    }

    private String readAll(InputStream is) throws IOException {
        if (is == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }

    /**
     * Rút nội dung từ response OpenAI-compatible.
     * Hỗ trợ cả dạng đã trả về JSON trực tiếp và dạng bọc trong code block ```json
     */
    private String extractContent(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }
        String cleaned = raw.trim();
        // Bỏ code fence nếu có
        if (cleaned.startsWith("```")) {
            int firstNewline = cleaned.indexOf('\n');
            if (firstNewline > 0) {
                cleaned = cleaned.substring(firstNewline + 1);
            }
            int lastFence = cleaned.lastIndexOf("```");
            if (lastFence > 0) {
                cleaned = cleaned.substring(0, lastFence);
            }
            cleaned = cleaned.trim();
        }

        // Trường hợp 1: response là JSON chuẩn của chat/completions
        try {
            Map<String, Object> obj = JsonUtil.parseObject(cleaned);
            Object choices = obj.get("choices");
            if (choices instanceof List && !((List<?>) choices).isEmpty()) {
                Object first = ((List<?>) choices).get(0);
                if (first instanceof Map) {
                    Object msg = ((Map<?, ?>) first).get("message");
                    if (msg instanceof Map) {
                        Object content = ((Map<?, ?>) msg).get("content");
                        if (content != null) {
                            return String.valueOf(content);
                        }
                    }
                }
            }
        } catch (Exception ignored) {
            // Không phải JSON -> xử lý như text thuần
        }

        // Trường hợp 2: content trả về trực tiếp là text (một số provider)
        return cleaned;
    }
}
