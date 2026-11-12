package com.giasuly9.util;

import com.giasuly9.model.Question;

/**
 * Tự chấm điểm bài luyện tập.
 *
 * <p>Quy ước chấm điểm:
 * <ul>
 *   <li>Trắc nghiệm: đúng = 10đ/câu, sai = 0đ (không trừ điểm âm).</li>
 *   <li>Tự luận: AI chấm theo thang 0-10 dựa trên mức độ đúng của lời giải.</li>
 * </ul>
 * Điểm cuối phiên = điểm trung bình các câu, quy đổi thành thang 10.
 */
public final class AutoScoreService {

    /** Điểm tối đa cho một câu trắc nghiệm. */
    public static final double MCQ_MAX_SCORE = 10.0;

    private AutoScoreService() {
    }

    /**
     * Chấm một câu trắc nghiệm.
     *
     * @param selected đáp án học sinh chọn (có thể null/rỗng)
     * @param correct  đáp án đúng
     * @return điểm của câu (0 hoặc 10)
     */
    public static double scoreMcq(String selected, String correct) {
        if (selected == null || correct == null) {
            return 0;
        }
        return selected.trim().equalsIgnoreCase(correct.trim()) ? MCQ_MAX_SCORE : 0;
    }

    /**
     * Chấm một câu tự luận dựa trên phần nhận xét của AI.
     * <p>AI trả về văn bản; hệ thống dò số điểm "x/10" nếu có, nếu không thì
     * suy ra từ các từ khóa đánh giá (đúng/sai/hoàn toàn đúng...).
     *
     * <p><b>Quan trọng:</b> khi AI không đưa ra điểm số và cũng không có nhận xét
     * rõ ràng, câu đó được chấm <b>0 điểm</b> — không cho điểm "mặc định" để
     * tránh trường hợp bài sai/ảnh không liên quan vẫn được điểm.
     *
     * @param feedback nhận xét AI
     * @return điểm 0-10
     */
    public static double scoreEssay(String feedback) {
        if (feedback == null || feedback.trim().isEmpty()) {
            return 0;
        }
        String f = feedback.toLowerCase();

        // 1) AI có ghi rõ điểm số dạng "8/10", "8,5 điểm", "điểm: 8"
        Double parsed = extractNumericScore(f);
        if (parsed != null) {
            return clamp(parsed);
        }

        // 2) Trường hợp "không liên quan" (ảnh không liên quan đề bài) -> 0 điểm.
        //    Kiểm tra TRƯỚC vì nhận xét loại này thường kèm chữ "sai", "lỗi".
        if (f.contains("không liên quan") || f.contains("không đúng đề")
                || f.contains("lạc đề") || f.contains("sai đề")
                || f.contains("không đọc được") || f.contains("không xác định")) {
            return 0.0;
        }

        // 3) Trường hợp "không ... đúng" / "chưa đúng" -> sai, phải kiểm tra TRƯỚC
        //    vì chuỗi "không đúng" cũng chứa "đúng".
        if (f.contains("không đúng") || f.contains("không chính xác")
                || f.contains("chưa đúng") || f.contains("chưa chính xác")
                || f.contains("sai") || f.contains("thiếu")
                || f.contains("cần chỉnh") || f.contains("cần sửa")
                || f.contains("lỗi")) {
            return 3.0;
        }

        // 4) Suy ra từ từ khóa đánh giá tích cực
        if (f.contains("hoàn toàn đúng") || f.contains("làm đúng hoàn toàn")
                || f.contains("chính xác hoàn toàn") || f.contains("rất tốt")) {
            return 9.0;
        }
        if (f.contains("đúng")) {
            return 8.0;
        }
        if (f.contains("gần đúng")) {
            return 6.0;
        }

        // 5) AI không nói rõ -> không tự ý cho điểm
        return 0.0;
    }

    /**
     * Tìm số điểm dạng "x/10" hoặc "x điểm" trong văn bản.
     */
    private static Double extractNumericScore(String text) {
        // Dạng "8/10", "8,5/10"
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("(\\d+(?:[,.]\\d+)?)\\s*/\\s*10")
                .matcher(text);
        if (m.find()) {
            try {
                return Double.parseDouble(m.group(1).replace(',', '.'));
            } catch (NumberFormatException ignored) {
            }
        }
        // Dạng "điểm: 8" hoặc "8 điểm"
        m = java.util.regex.Pattern
                .compile("(?:điểm|điem)\\s*[:=]?\\s*(\\d+(?:[,.]\\d+)?)")
                .matcher(text);
        if (m.find()) {
            try {
                return Double.parseDouble(m.group(1).replace(',', '.'));
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    private static double clamp(double v) {
        if (v < 0) {
            return 0;
        }
        if (v > 10) {
            return 10;
        }
        return v;
    }

    /**
     * Quy đổi điểm trung bình (thang 10) sang phần trăm.
     */
    public static double toPercent(double scoreOutOf10) {
        return clamp(scoreOutOf10) * 10.0;
    }

    /**
     * Xếp loại theo thang 10.
     *
     * @return nhãn: Xuất sắc / Giỏi / Khá / Trung bình / Cần cải thiện
     */
    public static String grade(double scoreOutOf10) {
        double s = clamp(scoreOutOf10);
        if (s >= 9) {
            return "Xuất sắc";
        }
        if (s >= 8) {
            return "Giỏi";
        }
        if (s >= 6.5) {
            return "Khá";
        }
        if (s >= 5) {
            return "Trung bình";
        }
        return "Cần cải thiện";
    }

    /**
     * Lời khuyên dựa trên điểm.
     */
    public static String advice(double scoreOutOf10) {
        double s = clamp(scoreOutOf10);
        if (s >= 9) {
            return "Bạn đã nắm rất chắc kiến thức. Hãy tiếp tục luyện chủ đề mới để mở rộng phạm vi.";
        }
        if (s >= 8) {
            return "Kết quả tốt! Hãy xem lại các câu sai để khắc sâu phần kiến thức còn yếu.";
        }
        if (s >= 6.5) {
            return "Bạn đã nắm phần lớn bài. Hãy xem lại phần giải thích của các câu sai rồi luyện lại.";
        }
        if (s >= 5) {
            return "Bạn còn hổng một phần kiến thức. Hãy đọc lại bài giảng chủ đề này rồi luyện lại nhé.";
        }
        return "Hãy xem lại lý thuyết trong bài giảng và dùng gia sư AI hỏi từng bước để nắm vững kiến thức nền.";
    }
}
