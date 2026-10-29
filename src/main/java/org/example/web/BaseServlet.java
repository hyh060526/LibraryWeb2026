package org.example.web;

import org.example.util.JsonUtil;
import org.example.util.Result;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public abstract class BaseServlet extends HttpServlet {

    public static final String SESSION_USER = "loginUser";

    protected void writeJson(HttpServletResponse resp, Result result) throws IOException {
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        resp.getWriter().write(JsonUtil.toJson(result));
    }

    protected String readBody(HttpServletRequest req) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    protected Map<String, String> readForm(HttpServletRequest req) throws IOException {
        Map<String, String> map = new HashMap<>();
        if (req.getContentType() != null && req.getContentType().toLowerCase().contains("application/json")) {
            String body = readBody(req);
            if (body != null && !body.trim().isEmpty()) {
                Map<?, ?> raw = JsonUtil.fromJson(body, Map.class);
                if (raw != null) {
                    for (Map.Entry<?, ?> e : raw.entrySet()) {
                        map.put(String.valueOf(e.getKey()), e.getValue() == null ? null : String.valueOf(e.getValue()));
                    }
                }
            }
        } else {
            req.setCharacterEncoding("UTF-8");
            Map<String, String[]> params = req.getParameterMap();
            for (Map.Entry<String, String[]> e : params.entrySet()) {
                String[] v = e.getValue();
                map.put(e.getKey(), v == null || v.length == 0 ? null : v[0]);
            }
        }
        return map;
    }

    protected Map<String, Object> currentUser(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) {
            return null;
        }
        Object u = session.getAttribute(SESSION_USER);
        if (u instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) u;
            return map;
        }
        return null;
    }

    protected boolean requireLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (currentUser(req) == null) {
            writeJson(resp, Result.error(401, "请先登录"));
            return false;
        }
        return true;
    }

    protected boolean requireRole(HttpServletRequest req, HttpServletResponse resp, String... roles) throws IOException {
        Map<String, Object> user = currentUser(req);
        if (user == null) {
            writeJson(resp, Result.error(401, "请先登录"));
            return false;
        }
        String role = String.valueOf(user.get("role"));
        for (String r : roles) {
            if (r.equals(role)) {
                return true;
            }
        }
        writeJson(resp, Result.error(403, "当前角色无权执行该操作"));
        return false;
    }

    protected boolean hasPermission(HttpServletRequest req, String code) {
        Map<String, Object> user = currentUser(req);
        if (user == null) {
            return false;
        }
        Object p = user.get("permissions");
        if (p instanceof Iterable) {
            for (Object o : (Iterable<?>) p) {
                if (code.equals(String.valueOf(o))) {
                    return true;
                }
            }
        }
        return false;
    }

    protected String param(Map<String, String> form, String key) {
        return form.get(key);
    }

    protected String userId(HttpServletRequest req) {
        Map<String, Object> u = currentUser(req);
        return u == null ? null : String.valueOf(u.get("id"));
    }

    protected String userRole(HttpServletRequest req) {
        Map<String, Object> u = currentUser(req);
        return u == null ? null : String.valueOf(u.get("role"));
    }

    protected Integer toInt(String v) {
        if (v == null || v.trim().isEmpty() || "null".equals(v)) {
            return null;
        }
        try {
            return Integer.parseInt(v.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    protected Double toDouble(String v) {
        if (v == null || v.trim().isEmpty() || "null".equals(v)) {
            return null;
        }
        try {
            return Double.parseDouble(v.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    protected void handle(HttpServletResponse resp, Exception e) throws IOException {
        if (e instanceof IllegalArgumentException) {
            writeJson(resp, Result.error(e.getMessage()));
        } else {
            e.printStackTrace();
            writeJson(resp, Result.error("服务器异常：" + e.getMessage()));
        }
    }

    static {
        // 保证中文请求体按 UTF-8 解析
        System.setProperty("file.encoding", StandardCharsets.UTF_8.name());
    }
}
