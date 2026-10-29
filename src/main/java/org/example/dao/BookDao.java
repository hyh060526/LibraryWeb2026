package org.example.dao;

import org.example.entity.Book;
import org.example.entity.BookCopy;
import org.example.util.JDBCTool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BookDao {

    public Book findById(String id) throws Exception {
        try (Connection conn = JDBCTool.getConnection()) {
            return JDBCTool.getOneById(id, Book.class, conn);
        }
    }

    public List<Book> findAll() throws Exception {
        String sql = "SELECT * FROM `book` ORDER BY `createTime` DESC";
        try (Connection conn = JDBCTool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return JDBCTool.resultSetToList(rs, Book.class);
        }
    }

    public List<Book> search(String keyword) throws Exception {
        if (keyword == null || keyword.trim().isEmpty()) {
            return findAll();
        }
        String like = "%" + keyword.trim() + "%";
        String sql = "SELECT * FROM `book` WHERE `title` LIKE ? OR `author` LIKE ? OR `type` LIKE ? "
                + "OR `publisher` LIKE ? OR `introduction` LIKE ? ORDER BY `createTime` DESC";
        try (Connection conn = JDBCTool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 1; i <= 5; i++) {
                ps.setString(i, like);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return JDBCTool.resultSetToList(rs, Book.class);
            }
        }
    }

    public List<Book> findAvailable() throws Exception {
        String sql = "SELECT * FROM `book` WHERE `status` = '正常' AND `stock` > 0 ORDER BY `createTime` DESC";
        try (Connection conn = JDBCTool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return JDBCTool.resultSetToList(rs, Book.class);
        }
    }

    public boolean existsSimilar(String title, String author) throws Exception {
        String sql = "SELECT COUNT(*) FROM `book` WHERE `title` = ? AND (`author` = ? OR ? IS NULL)";
        try (Connection conn = JDBCTool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setString(2, author);
            ps.setString(3, author);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public int save(Book book) throws Exception {
        LocalDateTime now = LocalDateTime.now();
        if (book.getCreateTime() == null) {
            book.setCreateTime(now);
        }
        book.setUpdateTime(now);
        try (Connection conn = JDBCTool.getConnection()) {
            return JDBCTool.save(book, conn);
        }
    }

    public int update(Book book) throws Exception {
        book.setUpdateTime(LocalDateTime.now());
        try (Connection conn = JDBCTool.getConnection()) {
            return JDBCTool.update(book, conn);
        }
    }

    public int updateEditable(Book book) throws Exception {
        String sql = "UPDATE `book` SET `title`=?, `author`=?, `type`=?, `publisher`=?, `days`=?, "
                + "`price`=?, `introduction`=?, `updateTime`=? WHERE `id`=?";
        try (Connection conn = JDBCTool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, book.getTitle());
            ps.setString(2, book.getAuthor());
            ps.setString(3, book.getType());
            ps.setString(4, book.getPublisher());
            if (book.getDays() == null) {
                ps.setObject(5, null);
            } else {
                ps.setInt(5, book.getDays());
            }
            if (book.getPrice() == null) {
                ps.setObject(6, null);
            } else {
                ps.setDouble(6, book.getPrice());
            }
            ps.setString(7, book.getIntroduction());
            ps.setObject(8, LocalDateTime.now());
            ps.setString(9, book.getId());
            return ps.executeUpdate();
        }
    }

    public int updateStatus(String id, String status) throws Exception {
        String sql = "UPDATE `book` SET `status`=?, `updateTime`=? WHERE `id`=?";
        try (Connection conn = JDBCTool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setObject(2, LocalDateTime.now());
            ps.setString(3, id);
            return ps.executeUpdate();
        }
    }

    public int updateStock(String id, int stock) throws Exception {
        String sql = "UPDATE `book` SET `stock`=?, `updateTime`=? WHERE `id`=?";
        try (Connection conn = JDBCTool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, stock);
            ps.setObject(2, LocalDateTime.now());
            ps.setString(3, id);
            return ps.executeUpdate();
        }
    }

    public List<BookCopy> findCopies(String bookId) throws Exception {
        String sql = "SELECT * FROM `bookcopy` WHERE `bookId` = ? ORDER BY `createTime`";
        try (Connection conn = JDBCTool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                return JDBCTool.resultSetToList(rs, BookCopy.class);
            }
        }
    }

    public int countFreeCopies(String bookId) throws Exception {
        String sql = "SELECT COUNT(*) FROM `bookcopy` WHERE `bookId` = ? AND (`isBorrowed` = 0 OR `isBorrowed` IS NULL)";
        try (Connection conn = JDBCTool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }
}
