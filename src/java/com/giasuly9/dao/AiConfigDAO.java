package com.giasuly9.dao;

import com.giasuly9.util.AiConfig;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Lưu cấu hình AI trong bảng AI_SETTINGS (quản lý bởi Admin).
 */
public class AiConfigDAO extends DBContext {

    public AiConfigDAO() {
        super();
    }

    public AiConfigDAO(java.sql.Connection conn) {
        super(conn);
    }

    /**
     * Lấy cấu hình AI đang active. Nếu chưa có trong DB thì đọc từ file ai.properties.
     */
    public AiConfig getActiveConfig() {
        String sql = "SELECT TOP 1 Provider, ApiKey, BaseUrl, Model, Temperature, MaxTokens, "
                + "SystemPrompt, TutorPrompt, QuestionPrompt FROM AI_SETTINGS WHERE IsActive = 1 "
                + "ORDER BY SettingID DESC";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    AiConfig cfg = new AiConfig();
                    cfg.setProvider(rs.getString("Provider"));
                    cfg.setApiKey(rs.getString("ApiKey"));
                    cfg.setBaseUrl(rs.getString("BaseUrl"));
                    cfg.setModel(rs.getString("Model"));
                    cfg.setTemperature(rs.getDouble("Temperature"));
                    cfg.setMaxTokens(rs.getInt("MaxTokens"));
                    cfg.setSystemPrompt(rs.getString("SystemPrompt"));
                    cfg.setTutorPrompt(rs.getString("TutorPrompt"));
                    cfg.setQuestionPrompt(rs.getString("QuestionPrompt"));
                    if (cfg.getApiKey() != null && !cfg.getApiKey().trim().isEmpty()) {
                        return cfg;
                    }
                    // DB có cấu hình nhưng chưa có key -> dùng key từ env/file
                    AiConfig fromFile = loadFromFile();
                    if (fromFile.getApiKey() != null && !fromFile.getApiKey().trim().isEmpty()) {
                        fromFile.setProvider(cfg.getProvider());
                        fromFile.setBaseUrl(cfg.getBaseUrl());
                        fromFile.setModel(cfg.getModel());
                        fromFile.setTemperature(cfg.getTemperature());
                        fromFile.setMaxTokens(cfg.getMaxTokens());
                        if (cfg.getSystemPrompt() != null && !cfg.getSystemPrompt().trim().isEmpty()) {
                            fromFile.setSystemPrompt(cfg.getSystemPrompt());
                        }
                        if (cfg.getTutorPrompt() != null && !cfg.getTutorPrompt().trim().isEmpty()) {
                            fromFile.setTutorPrompt(cfg.getTutorPrompt());
                        }
                        if (cfg.getQuestionPrompt() != null && !cfg.getQuestionPrompt().trim().isEmpty()) {
                            fromFile.setQuestionPrompt(cfg.getQuestionPrompt());
                        }
                        return fromFile;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return loadFromFile();
    }

    /**
     * Fallback: đọc file ai.properties trong WEB-INF/classes hoặc classpath.
     */
    public AiConfig loadFromFile() {
        AiConfig cfg = new AiConfig();
        java.util.Properties props = com.giasuly9.util.SecurityUtil.loadProperties("ai.properties");
        if (props.isEmpty()) {
            props = com.giasuly9.util.SecurityUtil.loadProperties("../ai.properties");
        }
        cfg.setProvider(props.getProperty("ai.provider", cfg.getProvider()));
        cfg.setApiKey(props.getProperty("ai.apiKey", cfg.getApiKey()));
        cfg.setBaseUrl(props.getProperty("ai.baseUrl", cfg.getBaseUrl()));
        cfg.setModel(props.getProperty("ai.model", cfg.getModel()));
        cfg.setTemperature(Double.parseDouble(props.getProperty("ai.temperature", "0.7")));
        cfg.setMaxTokens(Integer.parseInt(props.getProperty("ai.maxTokens", "2048")));
        cfg.setSystemPrompt(props.getProperty("ai.systemPrompt", cfg.getSystemPrompt()));
        cfg.setTutorPrompt(props.getProperty("ai.tutorPrompt", cfg.getTutorPrompt()));
        cfg.setQuestionPrompt(props.getProperty("ai.questionPrompt", cfg.getQuestionPrompt()));

        // Biến môi trường được ưu tiên cao hơn (tiện cho deploy, không cần sửa file)
        String envKey = firstNonEmpty(System.getenv("GEMINI_API_KEY"), System.getenv("OPENAI_API_KEY"),
                System.getenv("AI_API_KEY"));
        if (envKey != null) {
            cfg.setApiKey(envKey);
            if ("GOOGLE".equalsIgnoreCase(cfg.getProvider())) {
                cfg.setBaseUrl("https://generativelanguage.googleapis.com/v1beta/openai");
                // gemini-3.5-flash thường bị 429 với key miễn phí; flash-lite ổn định hơn
                cfg.setModel("gemini-3.5-flash-lite");
            }
        }
        return cfg;
    }

    private String firstNonEmpty(String... values) {
        for (String v : values) {
            if (v != null && !v.trim().isEmpty()) {
                return v.trim();
            }
        }
        return null;
    }

    /**
     * Lưu cấu hình mới, tắt các bản ghi cũ.
     */
    public boolean save(AiConfig cfg) {
        try {
            try (PreparedStatement off = getConnection().prepareStatement("UPDATE AI_SETTINGS SET IsActive = 0")) {
                off.executeUpdate();
            }
            String sql = "INSERT INTO AI_SETTINGS (Provider, ApiKey, BaseUrl, Model, Temperature, MaxTokens, "
                    + "SystemPrompt, TutorPrompt, QuestionPrompt, IsActive, CreatedBy, CreatedAt) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 1, NULL, GETDATE())";
            try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
                stmt.setString(1, cfg.getProvider());
                stmt.setString(2, cfg.getApiKey());
                stmt.setString(3, cfg.getBaseUrl());
                stmt.setString(4, cfg.getModel());
                stmt.setDouble(5, cfg.getTemperature());
                stmt.setInt(6, cfg.getMaxTokens());
                stmt.setString(7, cfg.getSystemPrompt());
                stmt.setString(8, cfg.getTutorPrompt());
                stmt.setString(9, cfg.getQuestionPrompt());
                stmt.executeUpdate();
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updatePrompts(String systemPrompt, String tutorPrompt, String questionPrompt) {
        String sql = "UPDATE AI_SETTINGS SET SystemPrompt = ?, TutorPrompt = ?, QuestionPrompt = ? "
                + "WHERE IsActive = 1";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, systemPrompt);
            stmt.setString(2, tutorPrompt);
            stmt.setString(3, questionPrompt);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean testConnection() {
        AiConfig cfg = getActiveConfig();
        if (!cfg.isConfigured()) {
            return false;
        }
        String response = new com.giasuly9.util.AiClient(cfg)
                .complete("Trả lời đúng một từ: OK", "Ping");
        return response != null && !response.trim().isEmpty();
    }

    /**
     * Kiểm tra kết nối và trả về thông báo chi tiết (dùng cho trang quản trị).
     */
    public String testConnectionDetail() {
        AiConfig cfg = getActiveConfig();
        if (!cfg.isConfigured()) {
            return "Chưa cấu hình API key. Hãy dán key Gemini (lấy tại https://aistudio.google.com/apikey) "
                    + "rồi bấm Lưu cấu hình.";
        }
        com.giasuly9.util.AiClient c = new com.giasuly9.util.AiClient(cfg);
        String response = c.complete("Trả lời đúng một từ: OK", "Ping");
        if (response == null || response.trim().isEmpty()) {
            String err = c.getLastError();
            return "Kết nối thất bại. " + (err != null ? err : "Không nhận được phản hồi từ AI.");
        }
        return "Kết nối thành công! Phản hồi mẫu: " + response.trim();
    }
}
