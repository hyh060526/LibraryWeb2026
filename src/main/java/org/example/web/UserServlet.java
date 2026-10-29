package org.example.web;

import org.example.entity.User;
import org.example.service.UserService;
import org.example.util.Result;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Map;

@WebServlet("/api/user/*")
public class UserServlet extends BaseServlet {

    private final UserService userService = new UserService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = subPath(req);
        try {
            if ("/me".equals(path) || path.isEmpty()) {
                if (!requireLogin(req, resp)) {
                    return;
                }
                writeJson(resp, Result.ok(currentUser(req)));
                return;
            }
            if ("/search".equals(path)) {
                if (!requireRole(req, resp, UserService.ROLE_ADMIN)) {
                    return;
                }
                String keyword = req.getParameter("keyword");
                java.util.List<java.util.Map<String, Object>> rows = new java.util.ArrayList<>();
                for (User u : userService.search(keyword)) {
                    rows.add(userService.toSafeMap(u));
                }
                writeJson(resp, Result.ok(rows));
                return;
            }
            if ("/detail".equals(path)) {
                if (!requireRole(req, resp, UserService.ROLE_ADMIN)) {
                    return;
                }
                User u = userService.findById(req.getParameter("id"));
                if (u == null) {
                    writeJson(resp, Result.error("用户不存在"));
                    return;
                }
                writeJson(resp, Result.ok(userService.toSafeMap(u)));
                return;
            }
            writeJson(resp, Result.error("未知接口"));
        } catch (Exception e) {
            handle(resp, e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = subPath(req);
        try {
            Map<String, String> form = readForm(req);
            if ("/login".equals(path)) {
                login(req, resp, form);
                return;
            }
            if ("/register".equals(path)) {
                User user = userService.register(form);
                writeJson(resp, Result.ok(userService.toSafeMap(user)));
                return;
            }
            if ("/logout".equals(path)) {
                HttpSession session = req.getSession(false);
                if (session != null) {
                    session.invalidate();
                }
                writeJson(resp, Result.ok());
                return;
            }
            writeJson(resp, Result.error("未知接口"));
        } catch (Exception e) {
            handle(resp, e);
        }
    }

    private void login(HttpServletRequest req, HttpServletResponse resp, Map<String, String> form) throws Exception {
        HttpSession session = req.getSession(true);
        String expect = (String) session.getAttribute(CaptchaServlet.SESSION_CAPTCHA);
        String captcha = form.get("captcha");
        if (expect == null || captcha == null || !expect.equalsIgnoreCase(captcha.trim())) {
            writeJson(resp, Result.error("验证码错误"));
            return;
        }
        session.removeAttribute(CaptchaServlet.SESSION_CAPTCHA);
        User user = userService.login(form.get("name"), form.get("password"));
        Map<String, Object> safe = userService.toSafeMap(user);
        session.setAttribute(SESSION_USER, safe);
        writeJson(resp, Result.ok(safe));
    }

    private String subPath(HttpServletRequest req) {
        String path = req.getPathInfo();
        return path == null ? "" : path;
    }
}
