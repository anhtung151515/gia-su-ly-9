package com.giasuly9.util;

import jakarta.servlet.http.HttpServletRequest;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.imageio.ImageIO;

/**
 * Xử lý upload file (ảnh bài giải của học sinh, ảnh bìa bài giảng).
 */
public final class FileUploadUtil {

    private static final long MAX_SIZE = 5L * 1024 * 1024; // 5MB
    private static final Set<String> ALLOWED_IMAGE =
            Set.of("jpg", "jpeg", "png", "gif", "webp");

    /** Kích thước tối đa của ảnh sau khi chuẩn hóa (px). Gemini đọc ảnh tốt nhất ở ~1568px. */
    private static final int MAX_DIMENSION = 1568;

    private FileUploadUtil() {
    }

    /**
     * Dữ liệu ảnh đã chuẩn hóa, sẵn sàng gửi cho AI.
     */
    public static final class ImageData {

        private final byte[] bytes;
        private final String mime;

        public ImageData(byte[] bytes, String mime) {
            this.bytes = bytes;
            this.mime = mime;
        }

        public byte[] getBytes() {
            return bytes;
        }

        public String getMime() {
            return mime;
        }
    }

    /**
     * Đọc ảnh từ request và chuẩn hóa để AI đọc được.
     * <p>Các bước xử lý:
     * <ol>
     *   <li>Đọc toàn bộ dữ liệu ảnh (chỉ đọc 1 lần, tránh mất dữ liệu).</li>
     *   <li>Nhận diện định dạng thật bằng chữ ký file (magic bytes) - không tin
     *       vào Content-Type do trình duyệt gửi lên.</li>
     *   <li>Chuyển ảnh không phải PNG/JPEG (gif, webp, bmp...) về PNG để AI đọc ổn định.</li>
     *   <li>Thu nhỏ ảnh quá lớn về {@value #MAX_DIMENSION}px để tránh lỗi API.</li>
     * </ol>
     *
     * @return dữ liệu ảnh đã chuẩn hóa, hoặc null nếu không có ảnh hợp lệ
     */
    public static ImageData readImage(HttpServletRequest request, String partName) {
        try {
            jakarta.servlet.http.Part part = request.getPart(partName);
            if (part == null || part.getSize() <= 0 || part.getSize() > MAX_SIZE) {
                return null;
            }
            byte[] raw;
            try (InputStream is = part.getInputStream()) {
                raw = is.readAllBytes();
            }
            if (raw == null || raw.length == 0) {
                return null;
            }
            // Chỉ chấp nhận ảnh thật (nhận diện qua magic bytes), tránh gửi
            // dữ liệu rác (vd "undefined") khiến API AI báo lỗi 400.
            if (detectMime(raw) == null) {
                return null;
            }
            return normalize(raw);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Chuẩn hóa ảnh: xác định định dạng thật, chuyển về PNG/JPEG và thu nhỏ nếu cần.
     */
    public static ImageData normalize(byte[] raw) {
        String detected = detectMime(raw);
        try {
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(raw));
            if (img == null) {
                // Không đọc được (ví dụ webp trên JRE thiếu codec) -> gửi nguyên bản
                return new ImageData(raw, detected == null ? "image/jpeg" : detected);
            }
            BufferedImage resized = scaleDown(img);
            if (isJpeg(detected)) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ImageIO.write(resized, "jpg", bos);
                return new ImageData(bos.toByteArray(), "image/jpeg");
            }
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ImageIO.write(resized, "png", bos);
            return new ImageData(bos.toByteArray(), "image/png");
        } catch (Exception e) {
            e.printStackTrace();
            return new ImageData(raw, detected == null ? "image/jpeg" : detected);
        }
    }

    /**
     * Nhận diện định dạng ảnh qua chữ ký file (magic bytes).
     */
    private static String detectMime(byte[] b) {
        if (b.length >= 8
                && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G') {
            return "image/png";
        }
        if (b.length >= 3
                && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF) {
            return "image/jpeg";
        }
        if (b.length >= 6 && b[0] == 'G' && b[1] == 'I' && b[2] == 'F') {
            return "image/gif";
        }
        if (b.length >= 12 && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
                && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P') {
            return "image/webp";
        }
        if (b.length >= 2 && b[0] == 'B' && b[1] == 'M') {
            return "image/bmp";
        }
        return null;
    }

    private static boolean isJpeg(String mime) {
        return "image/jpeg".equals(mime);
    }

    /**
     * Thu nhỏ ảnh nếu chiều rộng/cao vượt quá giới hạn.
     */
    private static BufferedImage scaleDown(BufferedImage img) {
        int w = img.getWidth();
        int h = img.getHeight();
        int max = Math.max(w, h);
        if (max <= MAX_DIMENSION) {
            return img;
        }
        double ratio = (double) MAX_DIMENSION / max;
        int nw = Math.max(1, (int) (w * ratio));
        int nh = Math.max(1, (int) (h * ratio));
        Image scaled = img.getScaledInstance(nw, nh, Image.SCALE_SMOOTH);
        BufferedImage out = new BufferedImage(nw, nh, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = out.createGraphics();
        g.drawImage(scaled, 0, 0, null);
        g.dispose();
        return out;
    }

    /**
     * Lưu ảnh đã đọc sẵn vào thư mục upload.
     * <p>Dùng hàm này thay vì {@link #saveImage} khi đã đọc part ở nơi khác,
     * tránh đọc lại input stream lần 2 (sẽ ra rỗng và làm AI không đọc được ảnh).
     *
     * @param bytes    dữ liệu ảnh
     * @param mime     kiểu MIME (image/png, image/jpeg...)
     * @param basePath thư mục gốc để lưu
     * @param subFolder thư mục con
     * @return đường dẫn tương đối để lưu DB, hoặc null nếu lỗi
     */
    public static String saveImageBytes(byte[] bytes, String mime, String basePath,
            String subFolder) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        try {
            String ext = "png";
            if ("image/jpeg".equals(mime)) {
                ext = "jpg";
            }
            Path dir = Paths.get(basePath, subFolder);
            Files.createDirectories(dir);
            String fileName = UUID.randomUUID().toString().replace("-", "") + "." + ext;
            Path target = dir.resolve(fileName);
            Files.write(target, bytes);
            return "/uploads/" + subFolder + "/" + fileName;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Lưu file upload vào thư mục trong webapp.
     *
     * @return đường dẫn tương đối để lưu DB, hoặc null nếu lỗi
     */
    public static String saveImage(HttpServletRequest request, String partName, String subFolder) {
        try {
            if (request.getPart(partName) == null) {
                return null;
            }
            jakarta.servlet.http.Part part = request.getPart(partName);
            if (part == null || part.getSubmittedFileName() == null
                    || part.getSubmittedFileName().trim().isEmpty()) {
                return null;
            }
            if (part.getSize() > MAX_SIZE) {
                return null;
            }
            String original = part.getSubmittedFileName();
            int dot = original.lastIndexOf('.');
            String ext = dot >= 0 ? original.substring(dot + 1).toLowerCase() : "";
            if (!ALLOWED_IMAGE.contains(ext)) {
                return null;
            }

            String basePath = getUploadBasePath(request);
            Path dir = Paths.get(basePath, subFolder);
            Files.createDirectories(dir);

            String fileName = UUID.randomUUID().toString().replace("-", "") + "." + ext;
            Path target = dir.resolve(fileName);

            try (InputStream is = part.getInputStream()) {
                Files.copy(is, target, StandardCopyOption.REPLACE_EXISTING);
            }
            return "/uploads/" + subFolder + "/" + fileName;

        } catch (IOException | jakarta.servlet.ServletException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Lấy đường dẫn vật lý để lưu upload.
     */
    public static String getUploadBasePath(HttpServletRequest request) {
        // Ưu tiên thư mục deploy để file tồn tại sau khi restart server
        String realPath = request.getServletContext().getRealPath("/uploads");
        if (realPath != null) {
            return realPath;
        }
        // Fallback: thư mục project khi chạy ở chế độ dev
        String projectDir = System.getProperty("catalina.base", ".");
        return projectDir + File.separator + "uploads";
    }
}
