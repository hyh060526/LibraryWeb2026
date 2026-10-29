package org.example.web;

import org.example.service.LoanService;
import org.example.service.UserService;
import org.example.util.Result;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@WebServlet("/api/loan/*")
public class LoanServlet extends BaseServlet {

    private final LoanService loanService = new LoanService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!requireLogin(req, resp)) {
            return;
        }
        String path = subPath(req);
        try {
            if ("/mine".equals(path)) {
                writeJson(resp, Result.ok(loanService.myLoans(userId(req))));
                return;
            }
            if ("/mine/unreturned".equals(path)) {
                writeJson(resp, Result.ok(loanService.myUnreturned(userId(req))));
                return;
            }
            if ("/mine/range".equals(path)) {
                writeJson(resp, Result.ok(loanService.myLoansByRange(
                        userId(req), req.getParameter("start"), req.getParameter("end"))));
                return;
            }
            if ("/search".equals(path)) {
                if (!requireRole(req, resp, UserService.ROLE_ADMIN)) {
                    return;
                }
                writeJson(resp, Result.ok(loanService.search(req.getParameter("keyword"))));
                return;
            }
            if ("/overdue".equals(path)) {
                if (!requireRole(req, resp, UserService.ROLE_ADMIN)) {
                    return;
                }
                writeJson(resp, Result.ok(loanService.searchOverdue(req.getParameter("keyword"))));
                return;
            }
            if ("/range".equals(path)) {
                if (!requireRole(req, resp, UserService.ROLE_ADMIN)) {
                    return;
                }
                writeJson(resp, Result.ok(loanService.searchByRange(
                        req.getParameter("start"), req.getParameter("end"))));
                return;
            }
            if ("/stat/type".equals(path)) {
                if (!requireRole(req, resp, UserService.ROLE_ADMIN)) {
                    return;
                }
                writeJson(resp, Result.ok(loanService.countByBookType()));
                return;
            }
            if ("/stat/major".equals(path)) {
                if (!requireRole(req, resp, UserService.ROLE_ADMIN)) {
                    return;
                }
                writeJson(resp, Result.ok(loanService.countByMajor()));
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
            if ("/borrow".equals(path)) {
                String bookId = form.get("bookId") == null ? form.get("id") : form.get("bookId");
                writeJson(resp, Result.ok(loanService.borrow(userId(req), bookId)));
                return;
            }
            if ("/return".equals(path)) {
                String copyId = form.get("bookCopyId") == null ? form.get("id") : form.get("bookCopyId");
                boolean privileged = UserService.ROLE_ADMIN.equals(userRole(req))
                        || UserService.ROLE_OPERATOR.equals(userRole(req));
                writeJson(resp, Result.ok(loanService.returnBook(userId(req), copyId, privileged)));
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
