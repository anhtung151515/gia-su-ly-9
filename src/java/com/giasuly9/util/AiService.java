package com.giasuly9.util;

import com.giasuly9.dao.AiConfigDAO;
import com.giasuly9.dao.AiLogDAO;
import com.giasuly9.model.AiLog;
import com.giasuly9.model.Question;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Lớp dịch vụ AI: sinh câu hỏi, chấm bài, giải từng bước (tutor), giải bài bất kỳ.
 */
public class AiService {

    public static final String DEFAULT_SYSTEM_PROMPT = """
            Bạn là một gia sư môn Vật lý lớp 9 tên là Gia Sư Lý 9.
            Nhiệm vụ của bạn:
            - Giải thích bài tập theo từng bước một, rõ ràng, đúng ngữ lý phổ thông vừa đủ.
            - Nếu học sinh trả lời sai, đừng đưa đáp án ngay. Hãy chỉ ra sai ở đâu, gợi ý họn.
            - Luôn dừng lại ở những chỗ cần học sinh tự tính toán rồi hỏi học sinh nhập kết quả.
            - Dùng đơn vị SI và ký hiệu đúng quy ước Việt Nam: dấu "." phân cách phần thập phân,
              dấu "," phân cách phần nguyên. Ví dụ 9,8 m/s^2; 1,5 kg; 0,02 A.
            - Công thức vật lý viết dạng: F = m.a ; P = m.g ; W = P.h ; Q = I^2.R.t ; A = U.I ; ...
            - Trả lời bằng tiếng Việt.
            """;

    public static final String DEFAULT_TUTOR_PROMPT = """
            Bạn là gia sư Vật lý lớp 9, hỗ trợ học sinh giải bài TỪNG BƯỚC.
            NGUYÊN TẮC BẮT BUỘC:
            1. Mỗi lượt trả lời chỉ đưa ra MỘT bước duy nhất, ngắn gọn.
            2. Nếu bước tiếp theo cần học sinh tự tính (tính lực, tính đại lượng trung gian,
               thay số vào công thức...), bạn PHẢI dừng lại và hỏi:
               "Bây giờ bạn hãy nhập kết quả/số liệu bạn tính được: ...".
            3. Nếu học sinh trả lời sai, KHÔNG nói đáp án. Hãy nêu lỗi sai ở đâu và gợi ý.
            4. Chỉ kết thúc (kết luận đáp án) khi học sinh đã tự ra được kết quả cuối cùng
               hoặc khi bạn đã hướng dẫn đủ các bước cơ bản.
            5. Luôn ghi rõ đơn vị kèm theo kết quả.

            ĐỊNH DẠNG TRẢ VỀ (JSON duy nhất, không thêm chữ khác):
            {
              "step": "nội dung bước đang hướng dẫn",
              "needUserData": true hoặc false,
              "askQuestion": "câu hỏi cụ thể cần học sinh trả lời (để trống nếu không cần)",
              "expectedType": "number | text | choice",
              "hint": "gợi ý ngắn (có thể để trống)",
              "isFinal": false,
              "finalAnswer": "đáp án cuối cùng (chỉ khi isFinal = true)"
            }
            """;

    public static final String DEFAULT_QUESTION_PROMPT = """
            Bạn là chuyên gia tạo câu hỏi ôn tập môn Vật lý lớp 9.
            Hãy tạo câu hỏi theo yêu cầu và trả về đúng JSON, không thêm giải thích ngoài JSON.
            """;


    /**
     * Prompt yêu cầu AI chấm điểm bài tự luận theo thang 0-10.
     */
    public static final String DEFAULT_GRADING_PROMPT = """
            Bạn là giáo viên chấm bài môn Vật lý lớp 9.
            Hãy chấm bài của học sinh theo thang điểm 0-10 và trả về đúng định dạng sau,
            KHÔNG thêm chữ nào ngoài JSON:

            {
              "score": 7.5,
              "comment": "nhận xét ngắn gọn 2-4 câu về bài làm: đúng ở bước nào, sai ở bước nào, cần sửa gì",
              "correctSteps": ["bước 1 đã làm đúng", "bước 2 đã làm đúng"],
              "wrongSteps": ["bước sai và lý do sai"],
              "suggestion": "gợi ý cụ thể để làm đúng"
            }

            QUY TẮC:
            1. "score" là số thực từ 0 đến 10 (làm tròn 0,5 điểm).
            2. Chấm theo mức độ đúng của lời giải: đúng hoàn toàn 8-10, đúng một phần 4-7,
               sai cơ bản 1-3, không làm 0.
            3. "correctSteps" và "wrongSteps" là mảng, có thể rỗng.
            4. Dùng tiếng Việt, đơn vị SI, dấu "." phân cách thập phân, dấu "," phân cách phần nguyên.
            5. TUYỆT ĐỐI không chấm điểm cao nếu bài làm không trả lời đúng đề bài.
               Nếu học sinh làm sai đề hoặc lệch hẳn đề bài -> score = 0.
            6. Nếu bài làm đúng nhưng thiếu bước trung gian -> trừ 1-2 điểm, không hạ 0.
            """;

    /**
     * Prompt chấm bài tự luận GỬI KÈM ẢNH. Bắt buộc AI đánh giá mức độ liên quan
     * của ảnh với đề bài trước khi chấm điểm, để tránh trường hợp gửi ảnh không
     * liên quan (ảnh vô nghĩa, ảnh người, ảnh chụp màn hình khác...) vẫn được điểm.
     */
    public static final String DEFAULT_IMAGE_GRADING_PROMPT = """
            Bạn là giáo viên chấm bài môn Vật lý lớp 9. Học sinh nộp bài làm bằng ẢNH CHỤP.
            Nhiệm vụ của bạn là CHẤM ĐIỂM ĐÚNG cho bài làm trong ảnh.

            Trả về đúng định dạng JSON sau, KHÔNG thêm chữ nào ngoài JSON:

            {
              "relevant": true,
              "score": 7.5,
              "comment": "nhận xét ngắn gọn 2-4 câu về bài làm trong ảnh",
              "correctSteps": ["bước đã làm đúng"],
              "wrongSteps": ["bước sai và lý do sai"],
              "suggestion": "gợi ý cụ thể để làm đúng"
            }

            QUY TẮC BẮT BUỘC:
            1. "relevant": trước tiên hãy xem ảnh có chứa bài làm VẬT LÝ cho đề bài này không.
               - Ảnh không liên quan (ảnh vô nghĩa, ảnh người, ảnh chụp màn hình khác,
                 ảnh của đề bài khác, ảnh quá mờ không đọc được) -> "relevant": false, "score": 0.
               - Ảnh có bài làm của đề bài này -> "relevant": true.
            2. Nếu "relevant" là false thì "score" BẮT BUỘC bằng 0, không có ngoại lệ.
            3. Nếu "relevant" là true: chấm theo mức độ đúng của bài làm trong ảnh.
               đúng hoàn toàn 8-10, đúng một phần 4-7, sai cơ bản 1-3, không làm 0.
            4. TUYỆT ĐỐI không chấm điểm cao nếu bài làm không trả lời đúng đề bài -> 0 điểm.
            5. "correctSteps" và "wrongSteps" là mảng, có thể rỗng.
            6. Dùng tiếng Việt, đơn vị SI, dấu "." phân cách thập phân, dấu "," phân cách phần nguyên.
            """;

    private final AiConfig config;
    private final AiClient client;
    private final AiLogDAO logDAO;

    public AiService() {
        this(new AiConfigDAO().getActiveConfig());
    }

    public AiService(AiConfig config) {
        this.config = config;
        this.client = new AiClient(config);
        this.logDAO = new AiLogDAO();
    }

    public AiConfig getConfig() {
        return config;
    }

    public boolean isConfigured() {
        return config.isConfigured();
    }

    public String systemPrompt() {
        String p = config.getSystemPrompt();
        return (p == null || p.trim().isEmpty()) ? DEFAULT_SYSTEM_PROMPT : p;
    }

    public String tutorPrompt() {
        String p = config.getTutorPrompt();
        return (p == null || p.trim().isEmpty()) ? DEFAULT_TUTOR_PROMPT : p;
    }

    public String questionPrompt() {
        String p = config.getQuestionPrompt();
        return (p == null || p.trim().isEmpty()) ? DEFAULT_QUESTION_PROMPT : p;
    }

    public String gradingPrompt() {
        return DEFAULT_GRADING_PROMPT;
    }

    // ==================================================================
    // 1. Sinh câu hỏi từ AI
    // ==================================================================
    /**
     * Sinh danh sách câu hỏi (trắc nghiệm hoặc tự luận) cho một chủ đề.
     */
    public List<Question> generateQuestions(int subjectId, int topicId, String subjectName,
            String topicName, String type, int count, String difficulty, int userId) {
        List<Question> result = new ArrayList<>();
        if (!isConfigured() || count <= 0) {
            return result;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Môn học: ").append(subjectName).append("\n");
        sb.append("Chủ đề: ").append(topicName).append("\n");
        sb.append("Loại câu hỏi: ")
                .append("MCQ".equalsIgnoreCase(type) ? "TRẮC NGHIỆM" : "TỰ LUẬN").append("\n");
        sb.append("Mức độ: ").append(difficulty == null ? "MEDIUM" : difficulty).append("\n");
        sb.append("Số lượng: ").append(count).append("\n\n");

        if ("MCQ".equalsIgnoreCase(type)) {
            sb.append("Tạo ").append(count).append(" câu hỏi trắc nghiệm chọn 1 đáp án (A, B, C, D). "
                    + "Câu hỏi phải bao quát đúng kiến thức Vật lý lớp 9 của chủ đề trên.\n");
            sb.append("Trả về JSON đúng định dạng sau, là mảng:\n");
            sb.append("""
                    [
                      {
                        "content": "nội dung câu hỏi",
                        "optionA": "phương án A",
                        "optionB": "phương án B",
                        "optionC": "phương án C",
                        "optionD": "phương án D",
                        "correctAnswer": "A",
                        "explanation": "giải thích ngắn vì sao đáp án đúng",
                        "difficulty": "EASY|MEDIUM|HARD"
                      }
                    ]
                    """);
        } else {
            sb.append("Tạo ").append(count).append(" câu hỏi tự luận (mở đầu). "
                    + "Mỗi câu phải là một bài toán giải được, có đủ số liệu.\n");
            sb.append("Trả về JSON đúng định dạng sau, là mảng:\n");
            sb.append("""
                    [
                      {
                        "content": "nội dung câu hỏi tự luận",
                        "explanation": "hướng dẫn giải gợi ý ngắn",
                        "difficulty": "EASY|MEDIUM|HARD"
                      }
                    ]
                    """);
        }

        String prompt = sb.toString();
        String raw = client.complete(questionPrompt(), prompt);
        log(userId, "GENERATE_QUESTION", prompt, raw, "GENERATE_QUESTION");

        if (raw == null) {
            return result;
        }

        Object parsed = extractJsonArray(raw);
        if (!(parsed instanceof List)) {
            return result;
        }

        for (Object o : (List<?>) parsed) {
            if (!(o instanceof Map)) {
                continue;
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> m = (Map<String, Object>) o;
            Question q = new Question();
            q.setSubjectId(subjectId);
            q.setTopicId(topicId);
            q.setContent(str(m.get("content")));
            q.setType("MCQ".equalsIgnoreCase(type) ? "MCQ" : "ESSAY");
            q.setOptionA(str(m.get("optionA")));
            q.setOptionB(str(m.get("optionB")));
            q.setOptionC(str(m.get("optionC")));
            q.setOptionD(str(m.get("optionD")));
            q.setCorrectAnswer(str(m.get("correctAnswer")));
            q.setExplanation(str(m.get("explanation")));
            q.setDifficulty(difficulty == null ? "MEDIUM" : difficulty);
            q.setCreatedBy(userId);
            q.setSource("AI");
            q.setActive(true);
            if (q.getContent() == null || q.getContent().isEmpty()) {
                continue;
            }
            result.add(q);
        }
        return result;
    }

    // ==================================================================
    // 2. Nhận xét câu trắc nghiệm sai
    // ==================================================================
    /**
     * Nhận xét câu trắc nghiệm học sinh trả lời sai.
     */
    public String explainWrongMcq(Question q, String selected, int userId) {
        if (!isConfigured()) {
            return fallbackMcq(q, selected);
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Học sinh trả lời SAI câu hỏi sau:\n");
        sb.append("Câu hỏi: ").append(q.getContent()).append("\n");
        sb.append("A. ").append(nz(q.getOptionA())).append("\n");
        sb.append("B. ").append(nz(q.getOptionB())).append("\n");
        sb.append("C. ").append(nz(q.getOptionC())).append("\n");
        sb.append("D. ").append(nz(q.getOptionD())).append("\n");
        sb.append("Đáp án đúng: ").append(nz(q.getCorrectAnswer())).append("\n");
        sb.append("Học sinh chọn: ").append(nz(selected)).append("\n\n");
        sb.append("Hãy nhận xét cho học sinh: nói rõ học sinh sai ở đâu, giải thích lại kiến thức, ")
                .append("và gợi ý để học sinh hiểu lại. Dùng tiếng Việt, thân thiện, ngắn gọn (dưới 200 từ).");

        String raw = client.complete(systemPrompt(), sb.toString());
        log(userId, "EXPLAIN_MCQ", sb.toString(), raw, "EXPLAIN_MCQ");
        return (raw == null || raw.trim().isEmpty()) ? fallbackMcq(q, selected) : raw.trim();
    }

    private String fallbackMcq(Question q, String selected) {
        StringBuilder sb = new StringBuilder();
        sb.append("Đáp án đúng là ").append(nz(q.getCorrectAnswer())).append(".\n");
        if (q.getExplanation() != null && !q.getExplanation().trim().isEmpty()) {
            sb.append(q.getExplanation());
        } else {
            sb.append("Hãy xem lại lại định luật và công thức liên quan trong phần lý thuyết của chủ đề này.");
        }
        return sb.toString();
    }

    // ==================================================================
    // 3. Chấm bài tự luận (có ảnh chụp)
    // ==================================================================
    /**
     * Nhận xét bài tự luận. Nếu có ảnh, mô tả ảnh được truyền vào prompt.
     */
    public String reviewEssay(Question q, String studentAnswer, String imageDescription, int userId) {
        if (!isConfigured()) {
            return "Bài tự luận của bạn đã được ghi nhận. Hệ thống AI chưa được cấu hình nên chưa thể nhận xét chi tiết.";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Đây là bài tự luận môn Vật lý lớp 9.\n");
        sb.append("Đề bài: ").append(q.getContent()).append("\n");
        if (imageDescription != null && !imageDescription.trim().isEmpty()) {
            sb.append("Nội dung bài làm trong ảnh: ").append(imageDescription.trim()).append("\n");
        }
        if (studentAnswer != null && !studentAnswer.trim().isEmpty()) {
            sb.append("Bài làm viết tay thêm: ").append(studentAnswer.trim()).append("\n");
        }
        sb.append("\nHãy đánh giá bài làm của học sinh theo cấu trúc:\n");
        sb.append("1. Nhận xét: học sinh làm đúng ở bước nào, sai ở bước nào (nếu có).\n");
        sb.append("2. Chỉ ra lỗi sai cụ thể và nguyên nhân.\n");
        sb.append("3. Gợi ý cách sửa / cách làm đúng.\n");
        sb.append("4. Nếu bài sai, đưa ra lời giải đúng để học sinh tham khảo.\n");
        sb.append("Trả lời bằng tiếng Việt, rõ ràng, thân thiện.");

        String raw = client.complete(systemPrompt(), sb.toString());
        log(userId, "REVIEW_ESSAY", sb.toString(), raw, "REVIEW_ESSAY");
        return (raw == null || raw.trim().isEmpty())
                ? "AI chưa phản hồi được. Bạn hãy thử lại sau hoặc liên hệ giáo viên."
                : raw.trim();
    }

    /**
     * Mô tả ảnh bài làm (dùng cho multimodal API nếu provider hỗ trợ).
     * Mặc định gửi kèm ảnh dạng base64 theo chuẩn OpenAI vision.
     */
    public String reviewEssayWithImage(Question q, String studentAnswer, byte[] imageBytes,
            String imageMime, int userId) {
        return gradeEssayWithImage(q, studentAnswer, imageBytes, imageMime, userId).toDisplayText();
    }

    /**
     * Chấm điểm bài tự luận nộp kèm ảnh.
     * <p>AI đánh giá trước mức độ liên quan của ảnh với đề bài. Nếu ảnh không
     * liên quan (hoặc không đọc được) thì bài chắc chắn 0 điểm.
     */
    public EssayGrade gradeEssayWithImage(Question q, String studentAnswer, byte[] imageBytes,
            String imageMime, int userId) {
        EssayGrade fallback = new EssayGrade();
        if (!isConfigured()) {
            fallback.setScore(0);
            fallback.setComment("AI chưa được cấu hình nên chưa thể chấm điểm bài ảnh. "
                    + "Bài của bạn vẫn được lưu lại.");
            return fallback;
        }
        if (imageBytes == null || imageBytes.length == 0) {
            return gradeEssay(q, studentAnswer, userId);
        }
        if (!isImageData(imageBytes)) {
            fallback.setScore(0);
            fallback.setRelevant(false);
            fallback.setComment("Ảnh bài làm không đúng định dạng nên không thể chấm điểm. "
                    + "Bạn hãy chụp lại ảnh rồi nộp.");
            return fallback;
        }
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("Đề bài: ").append(q.getContent()).append("\n");
            if (q.getExplanation() != null && !q.getExplanation().trim().isEmpty()) {
                sb.append("Lời giải tham khảo: ").append(q.getExplanation()).append("\n");
            }
            if (studentAnswer != null && !studentAnswer.trim().isEmpty()) {
                sb.append("Bài làm viết tay thêm: ").append(studentAnswer.trim()).append("\n");
            }
            sb.append("\nHãy đọc ảnh bài làm và chấm điểm theo định dạng JSON đã quy định.");

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("model", config.getModel());
            payload.put("temperature", config.getTemperature());
            payload.put("max_tokens", config.getMaxTokens());

            List<Map<String, Object>> msgs = new ArrayList<>();
            Map<String, Object> sys = new LinkedHashMap<>();
            sys.put("role", "system");
            sys.put("content", DEFAULT_IMAGE_GRADING_PROMPT);
            msgs.add(sys);

            Map<String, Object> userMsg = new LinkedHashMap<>();
            userMsg.put("role", "user");
            List<Map<String, Object>> content = new ArrayList<>();
            Map<String, Object> textPart = new LinkedHashMap<>();
            textPart.put("type", "text");
            textPart.put("text", sb.toString());
            content.add(textPart);
            Map<String, Object> imgPart = new LinkedHashMap<>();
            imgPart.put("type", "image_url");
            Map<String, Object> imgUrl = new LinkedHashMap<>();
            imgUrl.put("url", "data:" + (imageMime == null ? "image/jpeg" : imageMime)
                    + ";base64," + java.util.Base64.getEncoder().encodeToString(imageBytes));
            imgPart.put("image_url", imgUrl);
            content.add(imgPart);
            userMsg.put("content", content);
            msgs.add(userMsg);
            payload.put("messages", msgs);

            String raw = postAndExtract(JsonUtil.stringify(payload));
            log(userId, "GRADE_ESSAY_IMAGE", sb.toString(), raw, "GRADE_ESSAY_IMAGE");

            if (raw == null || raw.trim().isEmpty()) {
                fallback.setScore(0);
                fallback.setComment("AI chưa phản hồi được. Bài của bạn đã được ghi nhận, "
                        + "hãy thử lại sau hoặc liên hệ giáo viên.");
                return fallback;
            }

            Object extracted = extractJsonObject(raw);
            if (!(extracted instanceof Map)) {
                // AI trả về text thuần -> không xác định được mức độ liên quan
                EssayGrade g = new EssayGrade();
                g.setRelevant(false);
                g.setScore(0);
                g.setComment(raw.trim());
                return g;
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> m = (Map<String, Object>) extracted;
            EssayGrade g = new EssayGrade();
            g.setRelevant(!Boolean.FALSE.equals(m.get("relevant")));
            Object scoreObj = m.get("score");
            double score = 0;
            if (scoreObj instanceof Number) {
                score = ((Number) scoreObj).doubleValue();
            } else if (scoreObj != null) {
                try {
                    score = Double.parseDouble(String.valueOf(scoreObj).replace(',', '.'));
                } catch (NumberFormatException ignored) {
                    score = 0;
                }
            }
            // Ảnh không liên quan -> chắc chắn 0 điểm
            if (!g.isRelevant()) {
                score = 0;
            }
            if (score < 0) {
                score = 0;
            }
            if (score > 10) {
                score = 10;
            }
            g.setScore(score);
            g.setComment(str(m.get("comment")));
            g.setSuggestion(str(m.get("suggestion")));
            g.setCorrectSteps(toStringList(m.get("correctSteps")));
            g.setWrongSteps(toStringList(m.get("wrongSteps")));

            if (!g.isRelevant() && (g.getComment() == null || g.getComment().trim().isEmpty())) {
                g.setComment("Ảnh bài làm không liên quan đến đề bài nên không được tính điểm. "
                        + "Bạn hãy chụp đúng phần bài làm của mình rồi nộp lại.");
            }
            if (g.getComment() == null && g.getSuggestion() == null
                    && g.getCorrectSteps().isEmpty() && g.getWrongSteps().isEmpty()) {
                g.setComment(raw.trim());
            }
            return g;

        } catch (Exception e) {
            e.printStackTrace();
            fallback.setScore(0);
            fallback.setComment("AI chưa phản hồi được. Bài của bạn đã được ghi nhận.");
            return fallback;
        }
    }


    // ==================================================================
    // 3b. Chấm điểm bài tự luận (có điểm số cấu trúc)
    // ==================================================================
    /**
     * Kết quả chấm bài tự luận do AI trả về.
     */
    public static class EssayGrade {

        private double score;
        private String comment;
        private java.util.List<String> correctSteps = new java.util.ArrayList<>();
        private java.util.List<String> wrongSteps = new java.util.ArrayList<>();
        private String suggestion;
        /** Ảnh bài làm có liên quan đến đề bài hay không (chỉ dùng cho bài gửi ảnh). */
        private boolean relevant = true;

        /**
         * Ảnh có liên quan tới đề bài không. Nếu false thì bài chắc chắn 0 điểm.
         */
        public boolean isRelevant() {
            return relevant;
        }

        public void setRelevant(boolean relevant) {
            this.relevant = relevant;
        }

        public double getScore() {
            return score;
        }

        public void setScore(double score) {
            this.score = score;
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }

        public java.util.List<String> getCorrectSteps() {
            return correctSteps;
        }

        public void setCorrectSteps(java.util.List<String> correctSteps) {
            this.correctSteps = correctSteps;
        }

        public java.util.List<String> getWrongSteps() {
            return wrongSteps;
        }

        public void setWrongSteps(java.util.List<String> wrongSteps) {
            this.wrongSteps = wrongSteps;
        }

        public String getSuggestion() {
            return suggestion;
        }

        public void setSuggestion(String suggestion) {
            this.suggestion = suggestion;
        }

        /**
         * Ghép kết quả thành văn bản hiển thị trên trang kết quả.
         */
        public String toDisplayText() {
            StringBuilder sb = new StringBuilder();
            if (comment != null && !comment.trim().isEmpty()) {
                sb.append(comment.trim());
            }
            if (!correctSteps.isEmpty()) {
                sb.append("\n\nBước làm đúng:").append("\n");
                for (String s : correctSteps) {
                    sb.append("  - ").append(s).append("\n");
                }
            }
            if (!wrongSteps.isEmpty()) {
                sb.append("\nBước làm sai / cần sửa:").append("\n");
                for (String s : wrongSteps) {
                    sb.append("  - ").append(s).append("\n");
                }
            }
            if (suggestion != null && !suggestion.trim().isEmpty()) {
                sb.append("\nGợi ý sửa: ").append(suggestion.trim());
            }
            return sb.toString().trim();
        }
    }

    /**
     * Chấm điểm bài tự luận, trả về kết quả có điểm số + nhận xét chi tiết.
     * Nếu AI lỗi, fallback sang scoreEssay dựa trên văn bản nhận xét.
     */
    public EssayGrade gradeEssay(Question q, String studentAnswer, int userId) {
        if (!isConfigured()) {
            EssayGrade g = new EssayGrade();
            g.setScore(0);
            g.setComment("AI chưa được cấu hình nên chưa thể chấm điểm chi tiết. "
                    + "Bài của bạn vẫn được lưu lại.");
            return g;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Đề bài: ").append(q.getContent()).append("\n");
        if (q.getExplanation() != null && !q.getExplanation().trim().isEmpty()) {
            sb.append("Lời giải tham khảo: ").append(q.getExplanation()).append("\n");
        }
        if (studentAnswer != null && !studentAnswer.trim().isEmpty()) {
            sb.append("\nBài làm của học sinh:\n").append(studentAnswer.trim()).append("\n");
        }
        sb.append("\nHãy chấm điểm bài trên theo thang 0-10 và trả về đúng định dạng JSON đã quy định.");

        String raw = client.complete(gradingPrompt(), sb.toString());
        log(userId, "GRADE_ESSAY", sb.toString(), raw, "GRADE_ESSAY");

        if (raw == null || raw.trim().isEmpty()) {
            EssayGrade g = new EssayGrade();
            g.setScore(0);
            g.setComment("AI chưa phản hồi được. Bài của bạn đã được ghi nhận, "
                    + "hãy thử lại sau hoặc liên hệ giáo viên.");
            return g;
        }

        Object extracted = extractJsonObject(raw);
        if (!(extracted instanceof Map)) {
            // AI trả về text thuần -> dùng bộ chấm điểm theo văn bản
            EssayGrade g = new EssayGrade();
            g.setScore(AutoScoreService.scoreEssay(raw));
            g.setComment(raw.trim());
            return g;
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> m = (Map<String, Object>) extracted;
        EssayGrade g = new EssayGrade();
        Object scoreObj = m.get("score");
        if (scoreObj instanceof Number) {
            g.setScore(((Number) scoreObj).doubleValue());
        } else if (scoreObj != null) {
            try {
                g.setScore(Double.parseDouble(String.valueOf(scoreObj).replace(',', '.')));
            } catch (NumberFormatException ignored) {
                g.setScore(0);
            }
        }
        g.setComment(str(m.get("comment")));
        g.setSuggestion(str(m.get("suggestion")));
        g.setCorrectSteps(toStringList(m.get("correctSteps")));
        g.setWrongSteps(toStringList(m.get("wrongSteps")));

        if (g.getComment() == null && g.getSuggestion() == null
                && g.getCorrectSteps().isEmpty() && g.getWrongSteps().isEmpty()) {
            g.setComment(raw.trim());
        }
        return g;
    }

    private java.util.List<String> toStringList(Object o) {
        java.util.List<String> list = new java.util.ArrayList<>();
        if (o instanceof List) {
            for (Object item : (List<?>) o) {
                String s = str(item);
                if (s != null) {
                    list.add(s);
                }
            }
        } else {
            String s = str(o);
            if (s != null) {
                list.add(s);
            }
        }
        return list;
    }

    // ==================================================================
    // 4. Tutor: giải từng bước
    // ==================================================================
    /**
     * Một bước trong quy trình giải từng bước.
     */
    public static class TutorTurn {

        private String step;
        private boolean needUserData;
        private String askQuestion;
        private String expectedType;
        private String hint;
        private boolean isFinal;
        private String finalAnswer;
        private boolean aiError;

        public String getStep() {
            return step;
        }

        public void setStep(String step) {
            this.step = step;
        }

        public boolean isNeedUserData() {
            return needUserData;
        }

        public void setNeedUserData(boolean needUserData) {
            this.needUserData = needUserData;
        }

        public String getAskQuestion() {
            return askQuestion;
        }

        public void setAskQuestion(String askQuestion) {
            this.askQuestion = askQuestion;
        }

        public String getExpectedType() {
            return expectedType;
        }

        public void setExpectedType(String expectedType) {
            this.expectedType = expectedType;
        }

        public String getHint() {
            return hint;
        }

        public void setHint(String hint) {
            this.hint = hint;
        }

        public boolean isFinal() {
            return isFinal;
        }

        public void setFinal(boolean isFinal) {
            this.isFinal = isFinal;
        }

        public String getFinalAnswer() {
            return finalAnswer;
        }

        public void setFinalAnswer(String finalAnswer) {
            this.finalAnswer = finalAnswer;
        }

        public boolean isAiError() {
            return aiError;
        }

        public void setAiError(boolean aiError) {
            this.aiError = aiError;
        }
    }

    /**
     * Bắt đầu quy trình giải từng bước cho một câu hỏi tự luận.
     * history chứa các lượt trước (role/content).
     */
    public TutorTurn startTutor(Question q, List<Map<String, String>> history, int userId) {
        String prompt = buildTutorPrompt(q, history, true);
        String raw = client.complete(tutorPrompt(), prompt);
        log(userId, "TUTOR_START", prompt, raw, "TUTOR_START");
        return parseTutorTurn(raw);
    }

    /**
     * Tiếp tục quy trình sau khi học sinh nhập dữ liệu.
     */
    public TutorTurn continueTutor(Question q, List<Map<String, String>> history, int userId) {
        String prompt = buildTutorPrompt(q, history, false);
        String raw = client.complete(tutorPrompt(), prompt);
        log(userId, "TUTOR_CONTINUE", prompt, raw, "TUTOR_CONTINUE");
        return parseTutorTurn(raw);
    }

    private String buildTutorPrompt(Question q, List<Map<String, String>> history, boolean isFirst) {
        StringBuilder sb = new StringBuilder();
        sb.append("Đề bài: ").append(q.getContent()).append("\n");
        if (q.getExplanation() != null && !q.getExplanation().trim().isEmpty()) {
            sb.append("Gợi ý tham khảo của giáo viên: ").append(q.getExplanation()).append("\n");
        }
        sb.append("\n");
        if (isFirst) {
            sb.append("Hãy bắt đầu hướng dẫn học sinh giải bài này từng bước. "
                    + "Nếu bước đầu tiên cần học sinh tự tính thì hãy dừng lại và hỏi học sinh.");
        } else {
            sb.append("Học sinh vừa trả lời. Hãy phản hồi và đưa ra bước tiếp theo. "
                    + "Nếu bước tiếp theo cần học sinh tự tính thì hãy dừng lại và hỏi học sinh.");
        }
        if (history != null && !history.isEmpty()) {
            sb.append("\nLịch sử hội thoại:\n");
            for (Map<String, String> m : history) {
                String role = "user".equals(m.get("role")) ? "Học sinh" : "Gia sư";
                sb.append(role).append(": ").append(m.get("content")).append("\n");
            }
        }
        return sb.toString();
    }

    private TutorTurn parseTutorTurn(String raw) {
        TutorTurn turn = new TutorTurn();
        if (raw == null || raw.trim().isEmpty()) {
            turn.setAiError(true);
            turn.setStep("AI chưa phản hồi được. Vui lòng thử lại.");
            return turn;
        }
        Object extracted = extractJsonObject(raw);
        if (extracted instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> m = (Map<String, Object>) extracted;
            turn.setStep(str(m.get("step")));
            turn.setNeedUserData(bool(m.get("needUserData")));
            turn.setAskQuestion(str(m.get("askQuestion")));
            turn.setExpectedType(str(m.get("expectedType")));
            turn.setHint(str(m.get("hint")));
            turn.setFinal(bool(m.get("isFinal")));
            turn.setFinalAnswer(str(m.get("finalAnswer")));
        }
        if (turn.getStep() == null || turn.getStep().trim().isEmpty()) {
            // AI không trả về JSON hợp lệ -> coi như text thuần
            turn.setStep(raw.trim());
        }
        return turn;
    }

    // ==================================================================
    // 5. Giải bất kỳ bài nào thuộc kiến thức phổ thông
    // ==================================================================
    /**
     * Giải bài bất kỳ - dùng cho AI chat tổng quát và các bài ngoài chương trình.
     */
    public String solveAnything(String question, int userId) {
        if (!isConfigured()) {
            return "Hệ thống AI chưa được cấu hình. Vui lòng liên hệ quản trị viên.";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Hãy giải bài toán sau một cách đầy đủ, từng bước:\n\n");
        sb.append(question).append("\n\n");
        sb.append("Yêu cầu: trình bày rõ ràng, dùng đơn vị SI, kết luận có đáp án.");

        String raw = client.complete(systemPrompt(), sb.toString());
        log(userId, "SOLVE_ANY", sb.toString(), raw, "SOLVE_ANY");
        return (raw == null || raw.trim().isEmpty())
                ? "AI chưa phản hồi được. Vui lòng thử lại."
                : raw.trim();
    }

    /**
     * Chat tổng quát, giữ ngữ cảnh hội thoại.
     */
    public String chat(List<Map<String, String>> messages, int userId) {
        if (!isConfigured()) {
            return "Hệ thống AI chưa được cấu hình. Vui lòng liên hệ quản trị viên.";
        }
        String raw = client.chat(systemPrompt(), messages);
        log(userId, "CHAT", null, raw, "CHAT");
        if (raw == null || raw.trim().isEmpty()) {
            String err = client.getLastError();
            return (err != null && !err.trim().isEmpty())
                    ? "AI chưa phản hồi được. Lý do: " + err
                    : "AI chưa phản hồi được. Vui lòng thử lại.";
        }
        return raw.trim();
    }

    /**
     * Chat có kèm ảnh (ví dụ học sinh chụp đề bài rồi hỏi AI).
     * <p>Ảnh được gửi theo chuẩn OpenAI vision (base64 data URL) nên model
     * phải hỗ trợ đọc ảnh (Gemini hỗ trợ tốt).
     *
     * @param messages    lịch sử hội thoại (không chứa ảnh)
     * @param imageBytes  dữ liệu ảnh (null nếu không có ảnh)
     * @param imageMime   kiểu MIME của ảnh (vd image/png)
     * @param userId      id học sinh để ghi log
     * @return nội dung trả lời của AI
     */
    public String chatWithImage(List<Map<String, String>> messages, byte[] imageBytes,
            String imageMime, int userId) {
        if (!isConfigured()) {
            return "Hệ thống AI chưa được cấu hình. Vui lòng liên hệ quản trị viên.";
        }
        if (imageBytes == null || imageBytes.length == 0) {
            return chat(messages, userId);
        }
        // Ảnh phải là dữ liệu ảnh thật, nếu không sẽ gây lỗi 400 từ API.
        if (!isImageData(imageBytes)) {
            return "Ảnh bạn gửi không đúng định dạng. Vui lòng chọn lại ảnh (jpg, png, webp).";
        }
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("model", config.getModel());
            payload.put("temperature", config.getTemperature());
            payload.put("max_tokens", config.getMaxTokens());

            List<Map<String, Object>> msgs = new ArrayList<>();
            Map<String, Object> sys = new LinkedHashMap<>();
            sys.put("role", "system");
            sys.put("content", systemPrompt());
            msgs.add(sys);

            // Chuyển lịch sử chat (text) sang định dạng messages của API
            if (messages != null) {
                for (Map<String, String> m : messages) {
                    if (m == null || m.get("content") == null || m.get("content").trim().isEmpty()) {
                        continue;
                    }
                    Map<String, Object> mm = new LinkedHashMap<>();
                    mm.put("role", m.get("role"));
                    mm.put("content", m.get("content"));
                    msgs.add(mm);
                }
            }

            // Tin nhắn cuối kèm ảnh
            Map<String, Object> userMsg = new LinkedHashMap<>();
            userMsg.put("role", "user");
            List<Map<String, Object>> content = new ArrayList<>();
            Map<String, Object> textPart = new LinkedHashMap<>();
            textPart.put("type", "text");
            textPart.put("text", "");
            content.add(textPart);
            Map<String, Object> imgPart = new LinkedHashMap<>();
            imgPart.put("type", "image_url");
            Map<String, Object> imgUrl = new LinkedHashMap<>();
            imgUrl.put("url", "data:" + (imageMime == null ? "image/jpeg" : imageMime)
                    + ";base64," + java.util.Base64.getEncoder().encodeToString(imageBytes));
            imgPart.put("image_url", imgUrl);
            content.add(imgPart);
            userMsg.put("content", content);
            msgs.add(userMsg);
            payload.put("messages", msgs);

            String raw = postAndExtract(JsonUtil.stringify(payload));
            log(userId, "CHAT_IMAGE", null, raw, "CHAT_IMAGE");
            if (raw == null || raw.trim().isEmpty()) {
                String err = client.getLastError();
                return (err != null && !err.trim().isEmpty())
                        ? "AI chưa đọc được ảnh này. Lý do: " + err
                        : "AI chưa phản hồi được. Vui lòng thử lại.";
            }
            return raw.trim();

        } catch (Exception e) {
            e.printStackTrace();
            return "AI chưa phản hồi được. Vui lòng thử lại.";
        }
    }

    /**
     * Lỗi kỹ thuật gần nhất khi gọi AI (null nếu không có lỗi).
     */
    public String lastError() {
        return client.getLastError();
    }

    // ==================================================================
    // Helper
    // ==================================================================
    private String postAndExtract(String body) {
        try {
            Map<String, Object> payload = JsonUtil.parseObject(body);
            if (payload.isEmpty()) {
                return null;
            }
            return client.send(payload);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void log(int userId, String feature, String prompt, String response, String f) {
        try {
            AiLog l = new AiLog();
            l.setUserId(userId);
            l.setFeature(f);
            l.setPromptText(prompt);
            l.setResponseText(response);
            l.setSuccess(response != null && !response.trim().isEmpty());
            l.setErrorMessage(response == null ? "Không nhận được phản hồi từ AI" : null);
            l.setTokensUsed(estimateTokens(prompt, response));
            logDAO.save(l);
        } catch (Exception ignored) {
        }
    }

    private int estimateTokens(String prompt, String response) {
        int chars = (prompt == null ? 0 : prompt.length()) + (response == null ? 0 : response.length());
        return chars / 4;
    }

    /**
     * Rút mảng JSON từ response (chịu được cả khi có text bao quanh).
     */
    private Object extractJsonArray(String raw) {
        String s = raw.trim();
        int start = s.indexOf('[');
        int end = s.lastIndexOf(']');
        if (start >= 0 && end > start) {
            try {
                return JsonUtil.parse(s.substring(start, end + 1));
            } catch (Exception ignored) {
            }
        }
        try {
            return JsonUtil.parse(s);
        } catch (Exception ignored) {
        }
        return null;
    }

    /**
     * Rút object JSON từ response.
     */
    private Object extractJsonObject(String raw) {
        String s = raw.trim();
        if (s.startsWith("```")) {
            int nl = s.indexOf('\n');
            if (nl > 0) {
                s = s.substring(nl + 1);
            }
            int fence = s.lastIndexOf("```");
            if (fence > 0) {
                s = s.substring(0, fence);
            }
            s = s.trim();
        }
        int start = s.indexOf('{');
        int end = s.lastIndexOf('}');
        if (start >= 0 && end > start) {
            try {
                return JsonUtil.parse(s.substring(start, end + 1));
            } catch (Exception ignored) {
            }
        }
        try {
            return JsonUtil.parse(s);
        } catch (Exception ignored) {
        }
        return null;
    }

    private String str(Object o) {
        if (o == null) {
            return null;
        }
        String s = String.valueOf(o).trim();
        return s.isEmpty() ? null : s;
    }

    private boolean bool(Object o) {
        if (o == null) {
            return false;
        }
        if (o instanceof Boolean) {
            return (Boolean) o;
        }
        String s = String.valueOf(o).trim().toLowerCase();
        return "true".equals(s) || "1".equals(s) || "yes".equals(s);
    }

    private String nz(String s) {
        return (s == null || s.trim().isEmpty()) ? "(không có)" : s;
    }

    /**
     * Kiểm tra dữ liệu có phải ảnh thật không (nhận diện qua magic bytes).
     * Tránh gửi dữ liệu rác lên API AI - API sẽ trả lỗi 400.
     */
    private static boolean isImageData(byte[] b) {
        if (b == null || b.length < 12) {
            return false;
        }
        // PNG
        if ((b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G') {
            return true;
        }
        // JPEG
        if ((b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF) {
            return true;
        }
        // GIF
        if (b[0] == 'G' && b[1] == 'I' && b[2] == 'F') {
            return true;
        }
        // WEBP (RIFF....WEBP)
        if (b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
                && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P') {
            return true;
        }
        // BMP
        if (b[0] == 'B' && b[1] == 'M') {
            return true;
        }
        return false;
    }
}
