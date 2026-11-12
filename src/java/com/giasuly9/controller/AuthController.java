package com.giasuly9.controller;

import com.giasuly9.dao.UserDAO;
import com.giasuly9.filter.SessionUtil;
import com.giasuly9.model.User;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class AuthController extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getServletPath();

        if ("/logout".equals(path)) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            response.sendRedirect(request.getContextPath() + "/home");
            return;
        }

        if ("/register".equals(path)) {
            // Đã đăng nhập thì chuyển thẳng về trang chủ theo role
            User current = SessionUtil.currentUser(request);
            if (current != null) {
                response.sendRedirect(request.getContextPath() + SessionUtil.homeFor(current));
                return;
            }
            request.getRequestDispatcher("/views/auth/register.jsp").forward(request, response);
            return;
        }

        // Đã đăng nhập thì chuyển thẳng về trang chủ theo role
        User current = SessionUtil.currentUser(request);
        if (current != null) {
            response.sendRedirect(request.getContextPath() + SessionUtil.homeFor(current));
            return;
        }

        request.getRequestDispatcher("/views/auth/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        String path = request.getServletPath();

        if ("/register".equals(path)) {
            handleRegister(request, response);
            return;
        }

        String username = trim(request.getParameter("username"));
        String password = request.getParameter("password");

        if (username.isEmpty() || password == null || password.isEmpty()) {
            request.setAttribute("error", "Vui lòng nhập tên đăng nhập và mật khẩu.");
            request.getRequestDispatcher("/views/auth/login.jsp").forward(request, response);
            return;
        }

        User user = userDAO.authenticate(username, password);

        if (user == null) {
            request.setAttribute("error", "Sai tên đăng nhập hoặc mật khẩu.");
            request.getRequestDispatcher("/views/auth/login.jsp").forward(request, response);
            return;
        }
        if (!user.isActive()) {
            request.setAttribute("error", "Tài khoản của bạn đã bị vô hiệu hoá. Liên hệ quản trị viên.");
            request.getRequestDispatcher("/views/auth/login.jsp").forward(request, response);
            return;
        }

        HttpSession session = request.getSession(true);
        session.setAttribute("user", user);
        response.sendRedirect(request.getContextPath() + SessionUtil.homeFor(user));
    }

    /**
     * Xử lý đăng ký tài khoản học sinh mới.
     */
    private void handleRegister(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String username = trim(request.getParameter("username"));
        String password = request.getParameter("password");
        String confirm = request.getParameter("confirmPassword");
        String fullName = trim(request.getParameter("fullName"));
        String email = trim(request.getParameter("email"));
        String phone = trim(request.getParameter("phone"));
        String className = trim(request.getParameter("className"));

        // Giữ lại dữ liệu đã nhập để không mất khi báo lỗi
        request.setAttribute("usernameVal", username);
        request.setAttribute("fullNameVal", fullName);
        request.setAttribute("emailVal", email);
        request.setAttribute("phoneVal", phone);
        request.setAttribute("classNameVal", className);

        String error = null;
        if (username.isEmpty() || password == null || password.isEmpty()
                || fullName.isEmpty()) {
            error = "Vui lòng nhập đầy đủ họ tên, tên đăng nhập và mật khẩu.";
        } else if (username.length() < 4 || username.length() > 50) {
            error = "Tên đăng nhập phải từ 4 đến 50 ký tự.";
        } else if (!username.matches("[A-Za-z0-9_\\.]+")) {
            error = "Tên đăng nhập chỉ dùng chữ cái, số, dấu gạch dưới và dấu chấm.";
        } else if (password.length() < 6) {
            error = "Mật khẩu phải có ít nhất 6 ký tự.";
        } else if (!password.equals(confirm)) {
            error = "Mật khẩu nhập lại chưa khớp.";
        } else if (userDAO.existsByUsername(username)) {
            error = "Tên đăng nhập đã tồn tại. Vui lòng chọn tên khác.";
        }

        if (error != null) {
            request.setAttribute("error", error);
            request.getRequestDispatcher("/views/auth/register.jsp").forward(request, response);
            return;
        }

        if (email != null && email.isEmpty()) { email = null; }
        if (phone != null && phone.isEmpty()) { phone = null; }
        if (className != null && className.isEmpty()) { className = null; }

        int userId = userDAO.create(username, password, fullName, email, phone, "STUDENT", className);

        if (userId <= 0) {
            request.setAttribute("error", "Không thể tạo tài khoản. Vui lòng thử lại.");
            request.getRequestDispatcher("/views/auth/register.jsp").forward(request, response);
            return;
        }

        User user = userDAO.findById(userId);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        HttpSession session = request.getSession(true);
        session.setAttribute("user", user);
        response.sendRedirect(request.getContextPath() + SessionUtil.homeFor(user));
    }

    private String trim(String s) {
        return s == null ? "" : s.trim();
    }
}
