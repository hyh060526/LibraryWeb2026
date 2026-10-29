package org.example.service;

import org.example.dao.BookRecommendDao;
import org.example.dao.UserDao;
import org.example.entity.Book;
import org.example.entity.BookRecommend;
import org.example.entity.User;
import org.example.util.JDBCTool;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecommendService {

    public static final String PENDING = "待审";
    public static final String APPROVED = "通过";
    public static final String REJECTED = "驳回";

    private final BookRecommendDao dao = new BookRecommendDao();
    private final BookService bookService = new BookService();
    private final UserDao userDao = new UserDao();

    public Map<String, Object> recommend(String userId, BookRecommend form) throws Exception {
        if (isBlank(form.getBookName())) {
            throw new IllegalArgumentException("请输入书名");
        }
        Map<String, Object> result = new HashMap<>();
        boolean exists = bookService.existsSimilar(form.getBookName().trim(),
                isBlank(form.getAuthor()) ? form.getAuthor() : form.getAuthor().trim());
        if (exists) {
            result.put("exists", true);
            result.put("message", "图书馆已有相关书籍，无需重复推荐");
            return result;
        }
        form.setUserId(userId);
        form.setBookName(form.getBookName().trim());
        if (form.getAuthor() != null) {
            form.setAuthor(form.getAuthor().trim());
        }
        form.setStatus(PENDING);
        dao.save(form);
        result.put("exists", false);
        result.put("message", "馆内暂无此书，已提交审核");
        result.put("recommend", form);
        return result;
    }

    public List<Map<String, Object>> listAll() throws Exception {
        List<BookRecommend> list = dao.findAll();
        List<Map<String, Object>> rows = new ArrayList<>();
        try (Connection conn = JDBCTool.getConnection()) {
            for (BookRecommend r : list) {
                User u = userDao.loadById(conn, r.getUserId());
                Map<String, Object> m = new HashMap<>();
                m.put("id", r.getId());
                m.put("userId", r.getUserId());
                m.put("userName", u == null ? null : u.getName());
                m.put("bookName", r.getBookName());
                m.put("author", r.getAuthor());
                m.put("publisher", r.getPublisher());
                m.put("price", r.getPrice());
                m.put("status", r.getStatus());
                m.put("reason", r.getReason());
                m.put("createTime", r.getCreateTime());
                rows.add(m);
            }
        }
        return rows;
    }

    public List<BookRecommend> listMine(String userId) throws Exception {
        return dao.findByUser(userId);
    }

    public Book approve(String id, Integer stock, Integer days, String type, String introduction) throws Exception {
        BookRecommend rec = dao.findById(id);
        if (rec == null) {
            throw new IllegalArgumentException("推荐记录不存在");
        }
        if (!PENDING.equals(rec.getStatus())) {
            throw new IllegalArgumentException("该推荐已处理");
        }
        Book book = new Book();
        book.setTitle(rec.getBookName());
        book.setAuthor(rec.getAuthor());
        book.setPublisher(rec.getPublisher());
        book.setPrice(rec.getPrice());
        book.setType(isBlank(type) ? "其他" : type);
        book.setDays(days == null || days <= 0 ? 14 : days);
        book.setStock(stock == null || stock <= 0 ? 1 : stock);
        book.setIntroduction(introduction);
        Book saved = bookService.add(book);
        rec.setStatus(APPROVED);
        rec.setReason("已加入馆藏");
        dao.update(rec);
        return saved;
    }

    public void reject(String id, String reason) throws Exception {
        if (isBlank(reason)) {
            throw new IllegalArgumentException("请填写驳回理由");
        }
        BookRecommend rec = dao.findById(id);
        if (rec == null) {
            throw new IllegalArgumentException("推荐记录不存在");
        }
        if (!PENDING.equals(rec.getStatus())) {
            throw new IllegalArgumentException("该推荐已处理");
        }
        rec.setStatus(REJECTED);
        rec.setReason(reason.trim());
        dao.update(rec);
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
