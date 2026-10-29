package org.example.dao;

import org.example.entity.Admin;
import org.example.entity.Operator;
import org.example.entity.Student;
import org.example.entity.Teacher;
import org.example.entity.User;
import org.example.util.JDBCTool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UserDao {

    public User findById(String id) throws Exception {
        try (Connection conn = JDBCTool.getConnection()) {
            return loadById(conn, id);
        }
    }

    public User findByName(String name) throws Exception {
        String sql = "SELECT * FROM `user` WHERE `name` = ?";
        try (Connection conn = JDBCTool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs);
                }
            }
        }
        return null;
    }

    public User findByPhone(String phone) throws Exception {
        String sql = "SELECT * FROM `user` WHERE `phone` = ?";
        try (Connection conn = JDBCTool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, phone);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs);
                }
            }
        }
        return null;
    }

    public User findByNameAndPassword(String name, String password) throws Exception {
        String sql = "SELECT * FROM `user` WHERE `name` = ? AND `password` = ?";
        try (Connection conn = JDBCTool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs);
                }
            }
        }
        return null;
    }

    public List<User> search(String keyword) throws Exception {
        String like = "%" + keyword + "%";
        String sql = "SELECT * FROM `user` WHERE `name` LIKE ? OR `phone` LIKE ? OR `role` LIKE ? "
                + "OR `major` LIKE ? OR `college` LIKE ? ORDER BY `createTime` DESC";
        List<User> list = new ArrayList<>();
        try (Connection conn = JDBCTool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 1; i <= 5; i++) {
                ps.setString(i, like);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapUser(rs));
                }
            }
        }
        return list;
    }

    public int save(User user) throws Exception {
        LocalDateTime now = LocalDateTime.now();
        if (user.getCreateTime() == null) {
            user.setCreateTime(now);
        }
        user.setUpdateTime(now);
        try (Connection conn = JDBCTool.getConnection()) {
            return JDBCTool.save(user, conn);
        }
    }

    public int updateLoginTime(String id, LocalDateTime loginTime) throws Exception {
        String sql = "UPDATE `user` SET `loginTime` = ?, `updateTime` = ? WHERE `id` = ?";
        try (Connection conn = JDBCTool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, loginTime);
            ps.setObject(2, LocalDateTime.now());
            ps.setString(3, id);
            return ps.executeUpdate();
        }
    }

    public User loadById(Connection conn, String id) throws Exception {
        String sql = "SELECT * FROM `user` WHERE `id` = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs);
                }
            }
        }
        return null;
    }

    public User mapUser(ResultSet rs) throws Exception {
        String role = rs.getString("role");
        User user;
        if ("管理员".equals(role) || "admin".equalsIgnoreCase(role)) {
            user = JDBCTool.mapRow(rs, Admin.class);
        } else if ("操作员".equals(role) || "operator".equalsIgnoreCase(role)) {
            user = JDBCTool.mapRow(rs, Operator.class);
        } else if ("教师".equals(role) || "teacher".equalsIgnoreCase(role)) {
            user = JDBCTool.mapRow(rs, Teacher.class);
        } else {
            user = JDBCTool.mapRow(rs, Student.class);
        }
        return user;
    }
}
