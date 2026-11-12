package com.giasuly9.util;

import java.io.InputStream;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.util.Random;

/**
 * Tiện ích bảo mật: hash mật khẩu, sinh token ngẫu nhiên.
 */
public final class SecurityUtil {

    private SecurityUtil() {
    }

    /**
     * Hash mật khẩu bằng SHA-256 + salt.
     */
    public static String hashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt.getBytes(StandardCharsets.UTF_8));
            md.update(password.getBytes(StandardCharsets.UTF_8));
            byte[] digest = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Không hash được mật khẩu", e);
        }
    }

    public static String newSalt() {
        return randomToken(16);
    }

    public static String randomToken(int length) {
        final String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }

    /**
     * Tạo mật khẩu ngẫu nhiên dễ đọc cho việc admin tạo tài khoản.
     */
    public static String generatePassword() {
        final String upper = "ABCDEFGHJKLMNPQRSTUVWXYZ";
        final String lower = "abcdefghijkmnopqrstuvwxyz";
        final String digit = "23456789";
        final String all = upper + lower + digit;
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder();
        sb.append(upper.charAt(rnd.nextInt(upper.length())));
        sb.append(lower.charAt(rnd.nextInt(lower.length())));
        sb.append(digit.charAt(rnd.nextInt(digit.length())));
        for (int i = 0; i < 5; i++) {
            sb.append(all.charAt(rnd.nextInt(all.length())));
        }
        return sb.toString();
    }

    /**
     * Đọc file properties từ classpath hoặc đường dẫn hệ thống.
     */
    public static java.util.Properties loadProperties(String fileName) {
        java.util.Properties props = new java.util.Properties();
        try (InputStream is = SecurityUtil.class.getClassLoader().getResourceAsStream(fileName)) {
            if (is != null) {
                props.load(is);
                return props;
            }
        } catch (Exception ignored) {
        }
        try (InputStream is = new java.io.FileInputStream(fileName)) {
            props.load(is);
        } catch (Exception ignored) {
        }
        return props;
    }
}
