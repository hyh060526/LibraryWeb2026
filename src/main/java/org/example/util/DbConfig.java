package org.example.util;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * 数据库连接配置：从 classpath 下的 db.properties 读取连接信息。
 * 数据库 libraryadmin 由数据库备份还原，本工具只负责建立连接，不再自动建表。
 */
public class DbConfig {

    private static final Properties PROPS = new Properties();

    static {
        try (InputStream in = DbConfig.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in == null) {
                throw new IllegalStateException("未找到 db.properties 资源文件");
            }
            PROPS.load(in);
            Class.forName(PROPS.getProperty("jdbc.driver"));
        } catch (Exception e) {
            throw new RuntimeException("加载数据库配置失败", e);
        }
    }

    /** 获取数据库连接（每次调用新建，由调用方负责关闭）。 */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                PROPS.getProperty("jdbc.url"),
                PROPS.getProperty("jdbc.username"),
                PROPS.getProperty("jdbc.password"));
    }

    /** 读取配置项。 */
    public static String get(String key) {
        return PROPS.getProperty(key);
    }
}
