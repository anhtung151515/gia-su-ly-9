package com.giasuly9.util;

/**
 * Cấu hình kết nối AI (OpenAI-compatible API).
 * Được nạp từ bảng AI_SETTINGS trong DB, có fallback từ file ai.properties.
 */
public class AiConfig {

    private String provider = "OPENAI";
    private String apiKey = "";
    private String baseUrl = "https://api.openai.com/v1";
    private String model = "gpt-4o-mini";
    private double temperature = 0.7;
    private int maxTokens = 2048;

    /** System prompt dùng chung. */
    private String systemPrompt = "";

    /** System prompt riêng cho chế độ giải từng bước (tutor). */
    private String tutorPrompt = "";

    /** System prompt cho việc sinh câu hỏi. */
    private String questionPrompt = "";

    public AiConfig() {
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public int getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
    }

    public String getSystemPrompt() {
        return systemPrompt;
    }

    public void setSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }

    public String getTutorPrompt() {
        return tutorPrompt;
    }

    public void setTutorPrompt(String tutorPrompt) {
        this.tutorPrompt = tutorPrompt;
    }

    public String getQuestionPrompt() {
        return questionPrompt;
    }

    public void setQuestionPrompt(String questionPrompt) {
        this.questionPrompt = questionPrompt;
    }

    public String chatEndpoint() {
        String b = baseUrl == null ? "" : baseUrl.trim();
        while (b.endsWith("/")) {
            b = b.substring(0, b.length() - 1);
        }
        if (b.endsWith("/chat/completions")) {
            return b;
        }
        return b + "/chat/completions";
    }

    public boolean isConfigured() {
        return apiKey != null && !apiKey.trim().isEmpty() && model != null && !model.trim().isEmpty();
    }
}
