package com.giasuly9.filter;

import com.giasuly9.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * Tiện ích đọc user từ session.
 */
public final class SessionUtil {

    private SessionUtil() {
    }

    public static User currentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        return (User) session.getAttribute("user");
    }

    public static int currentUserId(HttpServletRequest request) {
        User u = currentUser(request);
        return (u == null) ? 0 : u.getUserId();
    }

    public static String currentRole(HttpServletRequest request) {
        User u = currentUser(request);
        return (u == null) ? "" : u.getRole();
    }

    public static boolean isAdmin(HttpServletRequest request) {
        User u = currentUser(request);
        return u != null && u.isAdmin();
    }

    public static boolean isTeacher(HttpServletRequest request) {
        User u = currentUser(request);
        return u != null && u.isTeacher();
    }

    public static boolean isStudent(HttpServletRequest request) {
        User u = currentUser(request);
        return u != null && u.isStudent();
    }

    /**
     * Trang chủ tương ứng với role của user.
     */
    public static String homeFor(User user) {
        if (user == null) {
            return "/login";
        }
        if (user.isAdmin()) {
            return "/admin/dashboard";
        }
        if (user.isTeacher()) {
            return "/teacher/dashboard";
        }
        return "/student/dashboard";
    }
}
