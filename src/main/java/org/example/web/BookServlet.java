package org.example.web;

import org.example.entity.Book;
import org.example.service.BookService;
import org.example.service.UserService;
import org.example.util.Result;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@WebServlet("/api/book/*")
public class BookServlet extends BaseServlet {

    private final BookService bookService = new BookService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!requireLogin(req, resp)) {
            return;
        }
        String path = subPath(req);
        try {
            if ("/list".equals(path) || path.isEmpty()) {
                writeJson(resp, Result.ok(bookService.listAll()));
                return;
            }
            if ("/search".equals(path)) {
                writeJson(resp, Result.ok(bookService.search(req.getParameter("keyword"))));
                return;
            }
            if ("/available".equals(path)) {
                writeJson(resp, Result.ok(bookService.listAvailable()));
                return;
            }
            if ("/detail".equals(path)) {
                writeJson(resp, Result.ok(bookService.getDetail(req.getParameter("id"))));
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
            if ("/add".equals(path)) {
                if (!requireRole(req, resp, UserService.ROLE_OPERATOR)) {
                    return;
                }
                writeJson(resp, Result.ok(bookService.add(fromForm(form))));
                return;
            }
            if ("/edit".equals(path)) {
                if (!requireRole(req, resp, UserService.ROLE_OPERATOR)) {
                    return;
                }
                bookService.edit(fromForm(form));
                writeJson(resp, Result.ok());
                return;
            }
            if ("/discard".equals(path)) {
                if (!requireRole(req, resp, UserService.ROLE_OPERATOR)) {
                    return;
                }
                bookService.discard(form.get("id"));
                writeJson(resp, Result.ok());
                return;
            }
            if ("/copy/add".equals(path)) {
                if (!requireRole(req, resp, UserService.ROLE_OPERATOR)) {
                    return;
                }
                writeJson(resp, Result.ok(bookService.addCopy(form.get("bookId"))));
                return;
            }
            if ("/copy/delete".equals(path)) {
                if (!requireRole(req, resp, UserService.ROLE_OPERATOR)) {
                    return;
                }
                bookService.deleteCopy(form.get("copyId") == null ? form.get("id") : form.get("copyId"));
                writeJson(resp, Result.ok());
                return;
            }
            writeJson(resp, Result.error("未知接口"));
        } catch (Exception e) {
            handle(resp, e);
        }
    }

    private Book fromForm(Map<String, String> form) {
        Book book = new Book();
        book.setId(form.get("id"));
        book.setTitle(form.get("title"));
        book.setAuthor(form.get("author"));
        book.setType(form.get("type"));
        book.setPublisher(form.get("publisher"));
        book.setDays(toInt(form.get("days")));
        book.setPrice(toDouble(form.get("price")));
        book.setStock(toInt(form.get("stock")));
        book.setIntroduction(form.get("introduction"));
        return book;
    }

    private String subPath(HttpServletRequest req) {
        String path = req.getPathInfo();
        return path == null ? "" : path;
    }
}
