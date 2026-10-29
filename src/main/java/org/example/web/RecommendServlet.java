package org.example.web;

import org.example.entity.BookRecommend;
import org.example.service.RecommendService;
import org.example.service.UserService;
import org.example.util.Result;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@WebServlet("/api/recommend/*")
public class RecommendServlet extends BaseServlet {

    private final RecommendService recommendService = new RecommendService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!requireLogin(req, resp)) {
            return;
        }
        String path = subPath(req);
        try {
            if ("/list".equals(path)) {
                if (!requireRole(req, resp, UserService.ROLE_ADMIN)) {
                    return;
                }
                writeJson(resp, Result.ok(recommendService.listAll()));
                return;
            }
            if ("/mine".equals(path)) {
                writeJson(resp, Result.ok(recommendService.listMine(userId(req))));
                return;
            }
            writeJson(resp, Result.error("未知接口"));
        } catch (Exception e) {
            handle(resp, e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!requireLogin(req, resp)) {
            return;
        }
        String path = subPath(req);
        try {
            Map<String, String> form = readForm(req);
            if ("/submit".equals(path)) {
                if (!requireRole(req, resp, UserService.ROLE_TEACHER)) {
                    return;
                }
                BookRecommend rec = new BookRecommend();
                rec.setBookName(form.get("bookName") == null ? form.get("title") : form.get("bookName"));
                rec.setAuthor(form.get("author"));
                rec.setPublisher(form.get("publisher"));
                rec.setPrice(toDouble(form.get("price")));
                writeJson(resp, Result.ok(recommendService.recommend(userId(req), rec)));
                return;
            }
            if ("/approve".equals(path)) {
                if (!requireRole(req, resp, UserService.ROLE_ADMIN)) {
                    return;
                }
                writeJson(resp, Result.ok(recommendService.approve(
                        form.get("id"),
                        toInt(form.get("stock")),
                        toInt(form.get("days")),
                        form.get("type"),
                        form.get("introduction"))));
                return;
            }
            if ("/reject".equals(path)) {
                if (!requireRole(req, resp, UserService.ROLE_ADMIN)) {
                    return;
                }
                recommendService.reject(form.get("id"), form.get("reason"));
                writeJson(resp, Result.ok());
                return;
            }
            writeJson(resp, Result.error("未知接口"));
        } catch (Exception e) {
            handle(resp, e);
        }
    }

    private String subPath(HttpServletRequest req) {
        String path = req.getPathInfo();
        return path == null ? "" : path;
    }
}
