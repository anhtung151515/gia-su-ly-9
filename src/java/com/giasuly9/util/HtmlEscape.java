package com.giasuly9.util;

/**
 * Tiện ích escape HTML khi render nội dung do người dùng/AI tạo ra.
 */
public final class HtmlEscape {

    private HtmlEscape() {
    }

    /**
     * Chuyển ký tự đặc biệt thành entity để hiển thị an toàn trong HTML.
     */
    public static String escape(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder(text.length() + 32);
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '&':
                    sb.append("&amp;");
                    break;
                case '<':
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                case '"':
                    sb.append("&quot;");
                    break;
                case '\'':
                    sb.append("&#39;");
                    break;
                default:
                    sb.append(c);
            }
        }
        return sb.toString();
    }
}
