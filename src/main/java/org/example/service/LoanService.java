package org.example.service;

import org.example.dao.BookCopyDao;
import org.example.dao.BookDao;
import org.example.dao.LoanDao;
import org.example.dao.StatDao;
import org.example.dao.UserDao;
import org.example.entity.Book;
import org.example.entity.BookCopy;
import org.example.entity.Loan;
import org.example.entity.User;
import org.example.util.JDBCTool;

import java.sql.Connection;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoanService {

    public static final String STATUS_BORROWED = "未还";
    public static final String STATUS_RETURNED = "已还";
    public static final String STATUS_OVERDUE = "超期";

    private final LoanDao loanDao = new LoanDao();
    private final BookDao bookDao = new BookDao();
    private final BookCopyDao copyDao = new BookCopyDao();
    private final UserDao userDao = new UserDao();
    private final StatDao statDao = new StatDao();

    public List<Map<String, Object>> myLoans(String userId) throws Exception {
        return enrich(loanDao.findByUserId(userId));
    }

    public List<Map<String, Object>> myUnreturned(String userId) throws Exception {
        return enrich(loanDao.findUnreturnedByUser(userId));
    }

    public List<Map<String, Object>> myLoansByRange(String userId, String start, String end) throws Exception {
        LocalDateTime s = parseStart(start);
        LocalDateTime e = parseEnd(end);
        return enrich(loanDao.findByUserAndRange(userId, s, e));
    }

    public List<Map<String, Object>> search(String keyword) throws Exception {
        return enrich(loanDao.searchByKeyword(keyword));
    }

    public List<Map<String, Object>> searchOverdue(String keyword) throws Exception {
        return enrich(loanDao.searchOverdue(keyword));
    }

    public List<Map<String, Object>> searchByRange(String start, String end) throws Exception {
        return enrich(loanDao.findByDateRange(parseStart(start), parseEnd(end)));
    }

    public Loan borrow(String userId, String bookId) throws Exception {
        Book book = bookDao.findById(bookId);
        if (book == null) {
            throw new IllegalArgumentException("图书不存在");
        }
        if (!BookService.STATUS_NORMAL.equals(book.getStatus())) {
            throw new IllegalArgumentException("该书已废弃，不能借阅");
        }
        BookCopy copy = copyDao.findFreeCopy(bookId);
        if (copy == null) {
            throw new IllegalArgumentException("没有可借出的副本");
        }
        LocalDateTime now = LocalDateTime.now();
        int days = book.getDays() == null ? 14 : book.getDays();
        Loan loan = new Loan();
        loan.setUserId(userId);
        loan.setBookCopyId(copy.getId());
        loan.setBorrowDate(now);
        loan.setDueDate(now.plusDays(days));
        loan.setStatus(STATUS_BORROWED);

        try (Connection conn = JDBCTool.getConnection()) {
            conn.setAutoCommit(false);
            try {
                copyDao.updateBorrowed(copy.getId(), true, conn);
                loanDao.save(loan, conn);
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
        return loan;
    }

    public Loan returnBook(String userId, String bookCopyId, boolean privileged) throws Exception {
        Loan target = null;
        if (!privileged) {
            for (Loan l : loanDao.findUnreturnedByUser(userId)) {
                if (bookCopyId.equals(l.getBookCopyId())) {
                    target = l;
                    break;
                }
            }
        } else {
            target = findOpenByCopy(bookCopyId);
        }
        if (target == null) {
            throw new IllegalArgumentException("未找到该副本的未还记录");
        }
        if (!privileged && !userId.equals(target.getUserId())) {
            throw new IllegalArgumentException("只能归还自己借出的图书");
        }
        target.setReturnDate(LocalDateTime.now());
        target.setStatus(STATUS_RETURNED);
        try (Connection conn = JDBCTool.getConnection()) {
            conn.setAutoCommit(false);
            try {
                loanDao.update(target, conn);
                copyDao.updateBorrowed(bookCopyId, false, conn);
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
        return target;
    }

    public List<Map<String, Object>> countByBookType() throws Exception {
        return statDao.countByBookType();
    }

    public List<Map<String, Object>> countByMajor() throws Exception {
        return statDao.countByMajor();
    }

    private Loan findOpenByCopy(String copyId) throws Exception {
        List<Loan> all = loanDao.searchByKeyword("");
        for (Loan l : all) {
            if (copyId.equals(l.getBookCopyId())
                    && (STATUS_BORROWED.equals(l.getStatus()) || STATUS_OVERDUE.equals(l.getStatus()))) {
                return l;
            }
        }
        return null;
    }

    private List<Map<String, Object>> enrich(List<Loan> loans) throws Exception {
        List<Map<String, Object>> result = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        try (Connection conn = JDBCTool.getConnection()) {
            for (Loan loan : loans) {
                if (STATUS_BORROWED.equals(loan.getStatus())
                        && loan.getDueDate() != null
                        && loan.getDueDate().isBefore(now)) {
                    loan.setStatus(STATUS_OVERDUE);
                }
                BookCopy copy = JDBCTool.getOneById(loan.getBookCopyId(), BookCopy.class, conn);
                Book book = null;
                if (copy != null) {
                    book = JDBCTool.getOneById(copy.getBookId(), Book.class, conn);
                    copy.setBook(book);
                }
                User user = userDao.loadById(conn, loan.getUserId());
                Map<String, Object> row = new HashMap<>();
                row.put("id", loan.getId());
                row.put("bookCopyId", loan.getBookCopyId());
                row.put("userId", loan.getUserId());
                row.put("borrowDate", loan.getBorrowDate());
                row.put("returnDate", loan.getReturnDate());
                row.put("dueDate", loan.getDueDate());
                row.put("status", loan.getStatus());
                row.put("bookId", book == null ? null : book.getId());
                row.put("bookTitle", book == null ? null : book.getTitle());
                row.put("bookAuthor", book == null ? null : book.getAuthor());
                row.put("bookType", book == null ? null : book.getType());
                row.put("userName", user == null ? null : user.getName());
                row.put("userRole", user == null ? null : user.getRole());
                row.put("userMajor", user == null ? null : user.getMajor());
                result.add(row);
            }
        }
        return result;
    }

    private static LocalDateTime parseStart(String s) {
        if (s == null || s.trim().isEmpty()) {
            throw new IllegalArgumentException("请输入开始日期");
        }
        return LocalDate.parse(s.trim().substring(0, 10)).atStartOfDay();
    }

    private static LocalDateTime parseEnd(String s) {
        if (s == null || s.trim().isEmpty()) {
            throw new IllegalArgumentException("请输入结束日期");
        }
        return LocalDate.parse(s.trim().substring(0, 10)).atTime(LocalTime.MAX);
    }
}
