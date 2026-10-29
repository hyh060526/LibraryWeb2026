package org.example.service;

import org.example.dao.BookCopyDao;
import org.example.dao.BookDao;
import org.example.entity.Book;
import org.example.entity.BookCopy;
import org.example.util.JDBCTool;

import java.sql.Connection;
import java.util.List;

public class BookService {

    public static final String STATUS_NORMAL = "正常";
    public static final String STATUS_DISCARDED = "已废弃";

    private final BookDao bookDao = new BookDao();
    private final BookCopyDao copyDao = new BookCopyDao();

    public List<Book> listAll() throws Exception {
        return bookDao.findAll();
    }

    public List<Book> search(String keyword) throws Exception {
        return bookDao.search(keyword);
    }

    public List<Book> listAvailable() throws Exception {
        return bookDao.findAvailable();
    }

    public Book getDetail(String id) throws Exception {
        Book book = bookDao.findById(id);
        if (book == null) {
            throw new IllegalArgumentException("图书不存在");
        }
        List<BookCopy> copies = copyDao.findByBookId(id);
        for (BookCopy c : copies) {
            c.setBook(null);
        }
        book.setBookCopies(copies);
        return book;
    }

    public Book add(Book book) throws Exception {
        if (isBlank(book.getTitle())) {
            throw new IllegalArgumentException("请输入书名");
        }
        if (isBlank(book.getAuthor())) {
            throw new IllegalArgumentException("请输入作者");
        }
        if (isBlank(book.getType())) {
            throw new IllegalArgumentException("请输入类型");
        }
        if (book.getDays() == null || book.getDays() <= 0) {
            book.setDays(14);
        }
        if (book.getStock() == null || book.getStock() < 0) {
            book.setStock(1);
        }
        book.setStatus(STATUS_NORMAL);
        bookDao.save(book);

        int stock = book.getStock();
        try (Connection conn = JDBCTool.getConnection()) {
            conn.setAutoCommit(false);
            try {
                for (int i = 0; i < stock; i++) {
                    BookCopy copy = new BookCopy();
                    copy.setBookId(book.getId());
                    copy.setBorrowed(false);
                    copyDao.save(copy, conn);
                }
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
        return getDetail(book.getId());
    }

    public void edit(Book form) throws Exception {
        Book db = bookDao.findById(form.getId());
        if (db == null) {
            throw new IllegalArgumentException("图书不存在");
        }
        if (STATUS_DISCARDED.equals(db.getStatus())) {
            throw new IllegalArgumentException("已废弃图书不能编辑");
        }
        if (isBlank(form.getTitle()) || isBlank(form.getAuthor()) || isBlank(form.getType())) {
            throw new IllegalArgumentException("书名、作者、类型不能为空");
        }
        db.setTitle(form.getTitle());
        db.setAuthor(form.getAuthor());
        db.setType(form.getType());
        db.setPublisher(form.getPublisher());
        db.setDays(form.getDays());
        db.setPrice(form.getPrice());
        db.setIntroduction(form.getIntroduction());
        bookDao.updateEditable(db);
    }

    public void discard(String id) throws Exception {
        Book book = bookDao.findById(id);
        if (book == null) {
            throw new IllegalArgumentException("图书不存在");
        }
        if (STATUS_DISCARDED.equals(book.getStatus())) {
            throw new IllegalArgumentException("该书已废弃");
        }
        int free = bookDao.countFreeCopies(id);
        int stock = book.getStock() == null ? 0 : book.getStock();
        if (free != stock) {
            throw new IllegalArgumentException("存在外借副本，不能废弃（可借出副本 " + free + " / 库存 " + stock + "）");
        }
        bookDao.updateStatus(id, STATUS_DISCARDED);
    }

    public BookCopy addCopy(String bookId) throws Exception {
        Book book = bookDao.findById(bookId);
        if (book == null) {
            throw new IllegalArgumentException("图书不存在");
        }
        if (STATUS_DISCARDED.equals(book.getStatus())) {
            throw new IllegalArgumentException("已废弃图书不能新增副本");
        }
        BookCopy copy = new BookCopy();
        copy.setBookId(bookId);
        copy.setBorrowed(false);
        copyDao.save(copy);
        int stock = book.getStock() == null ? 0 : book.getStock();
        bookDao.updateStock(bookId, stock + 1);
        return copy;
    }

    public void deleteCopy(String copyId) throws Exception {
        BookCopy copy = copyDao.findById(copyId);
        if (copy == null) {
            throw new IllegalArgumentException("副本不存在");
        }
        if (copy.isBorrowed()) {
            throw new IllegalArgumentException("副本在借阅状态下不能删除");
        }
        Book book = bookDao.findById(copy.getBookId());
        copyDao.delete(copy);
        if (book != null) {
            int stock = book.getStock() == null ? 0 : book.getStock();
            bookDao.updateStock(book.getId(), Math.max(0, stock - 1));
        }
    }

    public boolean existsSimilar(String title, String author) throws Exception {
        return bookDao.existsSimilar(title, author);
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
