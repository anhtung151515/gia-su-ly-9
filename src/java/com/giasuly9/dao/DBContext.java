package com.giasuly9.dao;

import com.giasuly9.util.SecurityUtil;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Quản lý kết nối cơ sở dữ liệu SQL Server.
 *
 * <p>Mỗi thread của Tomcat giữ một connection riêng (ThreadLocal).
 * Trước đây connection được tạo 1 lần ở constructor và dùng chung cho MỌI request
 * của servlet. Khi 2 request chạy đồng thời, JDBC driver báo lỗi
 * "The connection is busy with results for another command", khiến lệnh INSERT
 * lưu đáp án thất lại -&gt; học sinh phải làm lại và có cảm giác bị quay về câu trước.
 * Ngoài ra connection cũng bị rò rỉ (không bao giờ close) làm chậm dần theo thời gian.
 */
public class DBContext {

    private static final Logger LOGGER = Logger.getLogger(DBContext.class.getName());

    /** Connection theo từng thread (Tomcat có pool thread, mỗi thread giữ 1 connection). */
    private static final ThreadLocal<Connection> LOCAL = new ThreadLocal<>();

    /** Connection được tiêm sẵn (dùng cho test / transaction ngoài). */
    private final Connection injected;

    private final String url;
    private final String user;
    private final String pass;

    public DBContext() {
        Properties properties = new Properties();
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("ConnectDB.properties");
        if (inputStream == null) {
            inputStream = getClass().getClassLoader().getResourceAsStream("../ConnectDB.properties");
        }
        try {
            if (inputStream != null) {
                properties.load(inputStream);
            } else {
                // Fallback để máy dev chạy được nhanh
                properties.setProperty("url", "jdbc:sqlserver://localhost:1433;databaseName=GiaSuLy9;trustServerCertificate=true");
                properties.setProperty("userID", "sa");
                properties.setProperty("password", "123");
            }
        } catch (java.io.IOException ex) {
            LOGGER.log(Level.SEVERE, "Không đọc được ConnectDB.properties", ex);
            properties.setProperty("url", "jdbc:sqlserver://localhost:1433;databaseName=GiaSuLy9;trustServerCertificate=true");
            properties.setProperty("userID", "sa");
            properties.setProperty("password", "123");
        }
        this.url = properties.getProperty("url");
        this.user = properties.getProperty("userID");
        this.pass = properties.getProperty("password");
        this.injected = null;
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException ex) {
            LOGGER.log(Level.SEVERE, "Không tìm thấy driver SQL Server", ex);
        }
    }

    public DBContext(Connection conn) {
        this.injected = conn;
        this.url = null;
        this.user = null;
        this.pass = null;
    }

    /**
     * Lấy connection của thread hiện tại. Nếu chưa có (hoặc đã bị đóng) thì mở mới.
     */
    public Connection getConnection() {
        if (injected != null) {
            return injected;
        }
        Connection c = LOCAL.get();
        if (c == null) {
            try {
                c = DriverManager.getConnection(url, user, pass);
                LOCAL.set(c);
            } catch (SQLException ex) {
                LOGGER.log(Level.SEVERE, "Không kết nối được CSDL", ex);
            }
        }
        return c;
    }

    /**
     * Đóng connection của thread hiện tại nếu đang mở.
     */
    public void close() {
        if (injected != null) {
            return;
        }
        Connection c = LOCAL.get();
        if (c != null) {
            try {
                if (!c.isClosed()) {
                    c.close();
                }
            } catch (SQLException ignored) {
            } finally {
                LOCAL.remove();
            }
        }
    }

    public static String hashPassword(String raw, String salt) {
        return SecurityUtil.hashPassword(raw, salt);
    }
}
