package org.example.dao;

import org.example.util.JDBCTool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StatDao {

    public List<Map<String, Object>> countByBookType() throws Exception {
        String sql = "SELECT IFNULL(b.`type`, '未分类') AS name, COUNT(*) AS cnt "
                + "FROM `loan` l "
                + "LEFT JOIN `bookcopy` c ON l.`bookCopyId` = c.`id` "
                + "LEFT JOIN `book` b ON c.`bookId` = b.`id` "
                + "GROUP BY b.`type` ORDER BY cnt DESC";
        return queryPairs(sql);
    }

    public List<Map<String, Object>> countByMajor() throws Exception {
        String sql = "SELECT IFNULL(u.`major`, '未填写') AS name, COUNT(*) AS cnt "
                + "FROM `loan` l "
                + "LEFT JOIN `user` u ON l.`userId` = u.`id` "
                + "GROUP BY u.`major` ORDER BY cnt DESC";
        return queryPairs(sql);
    }

    private List<Map<String, Object>> queryPairs(String sql) throws Exception {
        List<Map<String, Object>> list = new ArrayList<>();
        try (Connection conn = JDBCTool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("name", rs.getString("name"));
                row.put("count", rs.getInt("cnt"));
                list.add(row);
            }
        }
        return list;
    }
}
