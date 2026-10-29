package org.example.dao;

import org.example.entity.BookRecommend;
import org.example.util.JDBCTool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.List;

public class BookRecommendDao {

    public BookRecommend findById(String id) throws Exception {
        try (Connection conn = JDBCTool.getConnection()) {
            return JDBCTool.getOneById(id, BookRecommend.class, conn);
        }
    }

    public List<BookRecommend> findAll() throws Exception {
        String sql = "SELECT * FROM `bookrecommend` ORDER BY `createTime` DESC";
        try (Connection conn = JDBCTool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return JDBCTool.resultSetToList(rs, BookRecommend.class);
        }
    }

    public List<BookRecommend> findByUser(String userId) throws Exception {
        String sql = "SELECT * FROM `bookrecommend` WHERE `userId` = ? ORDER BY `createTime` DESC";
        try (Connection conn = JDBCTool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return JDBCTool.resultSetToList(rs, BookRecommend.class);
            }
        }
    }

    public int save(BookRecommend rec) throws Exception {
        LocalDateTime now = LocalDateTime.now();
        if (rec.getCreateTime() == null) {
            rec.setCreateTime(now);
        }
        rec.setUpdateTime(now);
        try (Connection conn = JDBCTool.getConnection()) {
            return JDBCTool.save(rec, conn);
        }
    }

    public int update(BookRecommend rec) throws Exception {
        rec.setUpdateTime(LocalDateTime.now());
        try (Connection conn = JDBCTool.getConnection()) {
            return JDBCTool.update(rec, conn);
        }
    }
}
