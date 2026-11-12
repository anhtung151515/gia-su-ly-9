package com.giasuly9.filter;

import com.giasuly9.model.User;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Phân quyền theo role.
 * Cấu hình init-param "roles" = "ADMIN,TEACHER" (phân cách bằng dấu phẩy).
 * Nếu không khai báo roles thì mặc định cho phép ADMIN.
 */
public class RoleFilter implements Filter {

    private Set<String> allowedRoles;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        String roles = filterConfig.getInitParameter("roles");
        if (roles == null || roles.trim().isEmpty()) {
            allowedRoles = new HashSet<>(Arrays.asList("ADMIN"));
        } else {
            allowedRoles = new HashSet<>();
            for (String r : roles.split(",")) {
                if (!r.trim().isEmpty()) {
                    allowedRoles.add(r.trim().toUpperCase());
                }
            }
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        HttpSession session = req.getSession(false);

        User user = (session == null) ? null : (User) session.getAttribute("user");

        if (user == null) {
            res.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        if (!allowedRoles.contains(user.getRole().toUpperCase())) {
            res.sendRedirect(req.getContextPath() + SessionUtil.homeFor(user));
            return;
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}
