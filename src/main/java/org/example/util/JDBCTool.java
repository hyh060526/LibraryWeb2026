package org.example.util;

import org.example.annotation.Column;
import org.example.annotation.Id;
import org.example.annotation.Table;
import org.example.annotation.Transient;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * JDBCTool —— 基于「JDBC + Java 反射」实现的简易 ORM 工具类（作业1 的 JDBCTool 改造而来）。
 *
 * <p>与作业1 相比，本项目做了如下适配：</p>
 * <ol>
 *   <li>数据库由 H2 改为 MySQL（libraryadmin），连接由 {@link DbConfig} 管理；</li>
 *   <li>主键为 {@code char(36)} 的 UUID 字符串，由程序生成（{@link #save} 时若为空自动填充）；</li>
 *   <li>数据库列名与 Java 字段名同为驼峰命名（如 {@code createTime}），故列名默认取字段名本身；</li>
 *   <li>表名默认取类名全小写（{@code BookCopy -> bookcopy}），子类通过继承父类的 {@code @Table} 复用表名；</li>
 *   <li>标注 {@link Transient} 的关联字段不参与持久化。</li>
 * </ol>
 */
public class JDBCTool {

    /** 获取数据库连接（由调用方负责关闭）。 */
    public static Connection getConnection() throws SQLException {
        return DbConfig.getConnection();
    }

    // ======================= 方法1：结果集 -> 对象列表 =======================

    /** 把 ResultSet 中的记录转换成目标类的对象列表。 */
    public static <T> List<T> resultSetToList(ResultSet rs, Class<T> clazz) throws Exception {
        List<T> list = new ArrayList<>();
        while (rs.next()) {
            list.add(mapRow(rs, clazz));
        }
        return list;
    }

    /** 把 ResultSet 当前行（不移动游标）转换成目标类对象，供自定义 DAO 复用。 */
    public static <T> T mapRow(ResultSet rs, Class<T> clazz) throws Exception {
        ResultSetMetaData meta = rs.getMetaData();
        int columnCount = meta.getColumnCount();

        Map<String, Field> fieldMap = new HashMap<>();
        for (Field field : getAllFields(clazz)) {
            fieldMap.put(field.getName().toLowerCase(), field);
            fieldMap.put(getColumnName(field).toLowerCase(), field);
        }

        T obj = clazz.getDeclaredConstructor().newInstance();
        for (int i = 1; i <= columnCount; i++) {
            String columnLabel = meta.getColumnLabel(i).toLowerCase();
            Field field = fieldMap.get(columnLabel);
            if (field == null) {
                continue; // 该列在实体类中没有对应字段，跳过
            }
            field.set(obj, readColumnValue(rs, i, field.getType()));
        }
        return obj;
    }

    // ======================= 方法2：保存（增） =======================

    /** 把实体对象插入数据库（主键为 String UUID，若为空则自动生成）。 */
    public static <T> int save(T obj, Connection connection) throws Exception {
        Class<?> clazz = obj.getClass();
        String table = getTableName(clazz);
        Field idField = getIdField(clazz);

        // 主键为空时生成 UUID
        if (idField.get(obj) == null) {
            if (idField.getType() == String.class) {
                idField.set(obj, UUID.randomUUID().toString());
            } else {
                throw new IllegalStateException("主键 " + clazz.getSimpleName() + "." + idField.getName() + " 为空");
            }
        }

        List<Field> fields = getAllFields(clazz);
        StringBuilder sql = new StringBuilder("INSERT INTO ").append(table).append(" (");
        for (Field field : fields) {
            sql.append(getColumnName(field)).append(',');
        }
        sql.setLength(sql.length() - 1);
        sql.append(") VALUES (");
        for (int i = 0; i < fields.size(); i++) {
            sql.append("?,");
        }
        sql.setLength(sql.length() - 1);
        sql.append(')');

        try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            int index = 1;
            for (Field field : fields) {
                setParameter(ps, index++, field.get(obj));
            }
            return ps.executeUpdate();
        }
    }

    // ======================= 方法3：更新（改） =======================

    /** 根据主键更新实体对象对应的记录。 */
    public static <T> int update(T obj, Connection connection) throws Exception {
        Class<?> clazz = obj.getClass();
        String table = getTableName(clazz);
        Field idField = getIdField(clazz);

        List<Field> fields = getAllFields(clazz);
        StringBuilder sql = new StringBuilder("UPDATE ").append(table).append(" SET ");
        boolean first = true;
        for (Field field : fields) {
            if (isIdField(field)) {
                continue;
            }
            if (!first) {
                sql.append(", ");
            }
            sql.append(getColumnName(field)).append(" = ?");
            first = false;
        }
        sql.append(" WHERE ").append(getColumnName(idField)).append(" = ?");

        try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            int index = 1;
            for (Field field : fields) {
                if (isIdField(field)) {
                    continue;
                }
                setParameter(ps, index++, field.get(obj));
            }
            setParameter(ps, index, idField.get(obj));
            return ps.executeUpdate();
        }
    }

    // ======================= 方法4：删除（删） =======================

    /** 根据主键删除实体对象对应的记录。 */
    public static <T> int delete(T obj, Connection connection) throws Exception {
        Class<?> clazz = obj.getClass();
        String table = getTableName(clazz);
        Field idField = getIdField(clazz);

        String sql = "DELETE FROM " + table + " WHERE " + getColumnName(idField) + " = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            setParameter(ps, 1, idField.get(obj));
            return ps.executeUpdate();
        }
    }

    // ======================= 方法5：根据主键查询 =======================

    /** 根据主键 id 查询单个实体对象。 */
    public static <T> T getOneById(String id, Class<T> clazz, Connection connection) throws Exception {
        String table = getTableName(clazz);
        Field idField = getIdField(clazz);

        String sql = "SELECT * FROM " + table + " WHERE " + getColumnName(idField) + " = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            setParameter(ps, 1, id);
            try (ResultSet rs = ps.executeQuery()) {
                List<T> list = resultSetToList(rs, clazz);
                return list.isEmpty() ? null : list.get(0);
            }
        }
    }

    /** 查询一张表的所有记录。 */
    public static <T> List<T> findAll(Class<T> clazz, Connection connection) throws Exception {
        String table = getTableName(clazz);
        String sql = "SELECT * FROM " + table;
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return resultSetToList(rs, clazz);
        }
    }

    // ======================= 辅助方法 =======================

    /** 获取类所有可持久化字段（含父类字段，跳过 static 与 @Transient），并开启私有访问。 */
    private static List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        for (Class<?> c = clazz; c != null && c != Object.class; c = c.getSuperclass()) {
            for (Field field : c.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                if (field.isAnnotationPresent(Transient.class)) {
                    continue;
                }
                field.setAccessible(true);
                fields.add(field);
            }
        }
        return fields;
    }

    /** 解析表名：优先 @Table（可被继承），否则取类名全小写。 */
    private static String getTableName(Class<?> clazz) {
        for (Class<?> c = clazz; c != null && c != Object.class; c = c.getSuperclass()) {
            Table table = c.getAnnotation(Table.class);
            if (table != null && !table.value().isEmpty()) {
                return table.value();
            }
        }
        return clazz.getSimpleName().toLowerCase();
    }

    /** 解析列名：优先 @Column，否则取字段名本身（本项目列名为驼峰）。 */
    private static String getColumnName(Field field) {
        Column column = field.getAnnotation(Column.class);
        if (column != null && !column.value().isEmpty()) {
            return column.value();
        }
        return field.getName();
    }

    /** 判断字段是否为主键：@Id 注解或名为 id。 */
    private static boolean isIdField(Field field) {
        return field.isAnnotationPresent(Id.class) || field.getName().equalsIgnoreCase("id");
    }

    /** 查找主键字段。 */
    private static Field getIdField(Class<?> clazz) {
        for (Field field : getAllFields(clazz)) {
            if (isIdField(field)) {
                return field;
            }
        }
        throw new IllegalArgumentException("类 " + clazz.getName() + " 中未找到主键字段(@Id 或名为 id 的字段)");
    }

    /** 根据字段类型从 ResultSet 读取对应列的值。 */
    private static Object readColumnValue(ResultSet rs, int index, Class<?> type) throws SQLException {
        if (type == String.class) {
            return rs.getString(index);
        }
        if (type == int.class || type == Integer.class) {
            int v = rs.getInt(index);
            return rs.wasNull() ? (type == int.class ? 0 : null) : v;
        }
        if (type == long.class || type == Long.class) {
            long v = rs.getLong(index);
            return rs.wasNull() ? (type == long.class ? 0L : null) : v;
        }
        if (type == double.class || type == Double.class) {
            double v = rs.getDouble(index);
            return rs.wasNull() ? (type == double.class ? 0d : null) : v;
        }
        if (type == float.class || type == Float.class) {
            return rs.getFloat(index);
        }
        if (type == short.class || type == Short.class) {
            return rs.getShort(index);
        }
        if (type == byte.class || type == Byte.class) {
            return rs.getByte(index);
        }
        if (type == boolean.class || type == Boolean.class) {
            boolean v = rs.getBoolean(index);
            return rs.wasNull() ? (type == boolean.class ? Boolean.FALSE : null) : v;
        }
        if (type == BigDecimal.class) {
            return rs.getBigDecimal(index);
        }
        if (type == java.util.Date.class || type == Date.class) {
            Timestamp ts = rs.getTimestamp(index);
            return ts == null ? null : new java.util.Date(ts.getTime());
        }
        if (type == LocalDate.class) {
            Date d = rs.getDate(index);
            return d == null ? null : d.toLocalDate();
        }
        if (type == LocalDateTime.class) {
            Timestamp t = rs.getTimestamp(index);
            return t == null ? null : t.toLocalDateTime();
        }
        return rs.getObject(index);
    }

    /** 根据值类型为 PreparedStatement 绑定参数。 */
    private static void setParameter(PreparedStatement ps, int index, Object value) throws SQLException {
        if (value == null) {
            ps.setObject(index, null);
        } else if (value instanceof java.util.Date) {
            ps.setTimestamp(index, new Timestamp(((java.util.Date) value).getTime()));
        } else if (value instanceof LocalDate) {
            ps.setDate(index, Date.valueOf((LocalDate) value));
        } else if (value instanceof LocalDateTime) {
            ps.setTimestamp(index, Timestamp.valueOf((LocalDateTime) value));
        } else if (value instanceof Boolean) {
            ps.setBoolean(index, (Boolean) value);
        } else {
            ps.setObject(index, value);
        }
    }
}
