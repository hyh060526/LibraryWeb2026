package org.example.dao;

import org.example.entity.BookCopy;
import org.example.util.JDBCTool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.List;

public class BookCopyDao {

    public BookCopy findById(String id) throws Exception {
        try (Connection conn = JDBCTool.getConnection()) {
            return JDBCTool.getOneById(id, BookCopy.class, conn);
        }
    }

    public List<BookCopy> findByBookId(String bookId) throws Exception {
        String sql = "SELECT * FROM `bookcopy` WHERE `bookId` = ? ORDER BY `createTime`";
        try (Connection conn = JDBCTool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                return JDBCTool.resultSetToList(rs, BookCopy.class);
            }
        }
    }

    public BookCopy findFreeCopy(String bookId) throws Exception {
        String sql = "SELECT * FROM `bookcopy` WHERE `bookId` = ? AND (`isBorrowed` = 0 OR `isBorrowed` IS NULL) LIMIT 1";
        try (Connection conn = JDBCTool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                List<BookCopy> list = JDBCTool.resultSetToList(rs, BookCopy.class);
                return list.isEmpty() ? null : list.get(0);
            }
        }
    }

    public int save(BookCopy copy) throws Exception {
        LocalDateTime now = LocalDateTime.now();
        if (copy.getCreateTime() == null) {
            copy.setCreateTime(now);
        }
        copy.setUpdateTime(now);
        try (Connection conn = JDBCTool.getConnection()) {
            return JDBCTool.save(copy, conn);
        }
    }

    public int save(BookCopy copy, Connection conn) throws Exception {
        LocalDateTime now = LocalDateTime.now();
        if (copy.getCreateTime() == null) {
            copy.setCreateTime(now);
        }
        copy.setUpdateTime(now);
        return JDBCTool.save(copy, conn);
    }

    public int updateBorrowed(String id, boolean borrowed) throws Exception {
        String sql = "UPDATE `bookcopy` SET `isBorrowed`=?, `updateTime`=? WHERE `id`=?";
        try (Connection conn = JDBCTool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, borrowed);
            ps.setObject(2, LocalDateTime.now());
            ps.setString(3, id);
            return ps.executeUpdate();
        }
    }

    public int updateBorrowed(String id, boolean borrowed, Connection conn) throws Exception {
        String sql = "UPDATE `bookcopy` SET `isBorrowed`=?, `updateTime`=? WHERE `id`=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, borrowed);
            ps.setObject(2, LocalDateTime.now());
            ps.setString(3, id);
            return ps.executeUpdate();
        }
    }

    public int delete(BookCopy copy) throws Exception {
        try (Connection conn = JDBCTool.getConnection()) {
            return JDBCTool.delete(copy, conn);
        }
    }
}
