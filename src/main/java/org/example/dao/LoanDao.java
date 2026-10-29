package org.example.dao;

import org.example.entity.Loan;
import org.example.util.JDBCTool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.List;

public class LoanDao {

    public Loan findById(String id) throws Exception {
        try (Connection conn = JDBCTool.getConnection()) {
            return JDBCTool.getOneById(id, Loan.class, conn);
        }
    }

    public List<Loan> findByUserId(String userId) throws Exception {
        String sql = "SELECT * FROM `loan` WHERE `userId` = ? ORDER BY `borrowDate` DESC";
        try (Connection conn = JDBCTool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return JDBCTool.resultSetToList(rs, Loan.class);
            }
        }
    }

    public List<Loan> findUnreturnedByUser(String userId) throws Exception {
        String sql = "SELECT * FROM `loan` WHERE `userId` = ? AND (`status` = '未还' OR `status` = '超期') "
                + "ORDER BY `borrowDate` DESC";
        try (Connection conn = JDBCTool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return JDBCTool.resultSetToList(rs, Loan.class);
            }
        }
    }

    public List<Loan> findByUserAndRange(String userId, LocalDateTime start, LocalDateTime end) throws Exception {
        String sql = "SELECT * FROM `loan` WHERE `userId` = ? AND `borrowDate` >= ? AND `borrowDate` <= ? "
                + "ORDER BY `borrowDate` DESC";
        try (Connection conn = JDBCTool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.setObject(2, start);
            ps.setObject(3, end);
            try (ResultSet rs = ps.executeQuery()) {
                return JDBCTool.resultSetToList(rs, Loan.class);
            }
        }
    }

    public List<Loan> searchByKeyword(String keyword) throws Exception {
        String like = "%" + (keyword == null ? "" : keyword.trim()) + "%";
        String sql = "SELECT l.* FROM `loan` l "
                + "LEFT JOIN `user` u ON l.`userId` = u.`id` "
                + "LEFT JOIN `bookcopy` c ON l.`bookCopyId` = c.`id` "
                + "LEFT JOIN `book` b ON c.`bookId` = b.`id` "
                + "WHERE u.`name` LIKE ? OR b.`title` LIKE ? "
                + "ORDER BY l.`borrowDate` DESC";
        try (Connection conn = JDBCTool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, like);
            ps.setString(2, like);
            try (ResultSet rs = ps.executeQuery()) {
                return JDBCTool.resultSetToList(rs, Loan.class);
            }
        }
    }

    public List<Loan> searchOverdue(String keyword) throws Exception {
        String like = "%" + (keyword == null ? "" : keyword.trim()) + "%";
        String sql = "SELECT l.* FROM `loan` l "
                + "LEFT JOIN `user` u ON l.`userId` = u.`id` "
                + "LEFT JOIN `bookcopy` c ON l.`bookCopyId` = c.`id` "
                + "LEFT JOIN `book` b ON c.`bookId` = b.`id` "
                + "WHERE (l.`status` = '超期' OR (l.`status` = '未还' AND l.`dueDate` < NOW())) "
                + "AND (u.`name` LIKE ? OR b.`title` LIKE ?) "
                + "ORDER BY l.`dueDate`";
        try (Connection conn = JDBCTool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, like);
            ps.setString(2, like);
            try (ResultSet rs = ps.executeQuery()) {
                return JDBCTool.resultSetToList(rs, Loan.class);
            }
        }
    }

    public List<Loan> findByDateRange(LocalDateTime start, LocalDateTime end) throws Exception {
        String sql = "SELECT * FROM `loan` WHERE `borrowDate` >= ? AND `borrowDate` <= ? ORDER BY `borrowDate` DESC";
        try (Connection conn = JDBCTool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, start);
            ps.setObject(2, end);
            try (ResultSet rs = ps.executeQuery()) {
                return JDBCTool.resultSetToList(rs, Loan.class);
            }
        }
    }

    public int save(Loan loan) throws Exception {
        LocalDateTime now = LocalDateTime.now();
        if (loan.getCreateTime() == null) {
            loan.setCreateTime(now);
        }
        loan.setUpdateTime(now);
        try (Connection conn = JDBCTool.getConnection()) {
            return JDBCTool.save(loan, conn);
        }
    }

    public int save(Loan loan, Connection conn) throws Exception {
        LocalDateTime now = LocalDateTime.now();
        if (loan.getCreateTime() == null) {
            loan.setCreateTime(now);
        }
        loan.setUpdateTime(now);
        return JDBCTool.save(loan, conn);
    }

    public int update(Loan loan) throws Exception {
        loan.setUpdateTime(LocalDateTime.now());
        try (Connection conn = JDBCTool.getConnection()) {
            return JDBCTool.update(loan, conn);
        }
    }

    public int update(Loan loan, Connection conn) throws Exception {
        loan.setUpdateTime(LocalDateTime.now());
        return JDBCTool.update(loan, conn);
    }
}
