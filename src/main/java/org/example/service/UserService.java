package org.example.service;

import org.example.dao.UserDao;
import org.example.entity.Admin;
import org.example.entity.Operator;
import org.example.entity.Student;
import org.example.entity.Teacher;
import org.example.entity.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class UserService {

    public static final String ROLE_ADMIN = "管理员";
    public static final String ROLE_OPERATOR = "操作员";
    public static final String ROLE_STUDENT = "学生";
    public static final String ROLE_TEACHER = "教师";

    private static final Pattern PHONE = Pattern.compile("^1[3-9]\\d{9}$");

    private final UserDao userDao = new UserDao();

    public User login(String name, String password) throws Exception {
        if (isBlank(name) || isBlank(password)) {
            throw new IllegalArgumentException("请输入用户名和密码");
        }
        User user = userDao.findByNameAndPassword(name.trim(), password);
        if (user == null) {
            throw new IllegalArgumentException("用户名或密码错误");
        }
        userDao.updateLoginTime(user.getId(), LocalDateTime.now());
        user.setLoginTime(LocalDateTime.now());
        return user;
    }

    public User register(Map<String, String> form) throws Exception {
        String name = trim(form.get("name"));
        String password = form.get("password");
        String phone = trim(form.get("phone"));
        String role = trim(form.get("role"));
        String major = trim(form.get("major"));

        if (isBlank(name)) {
            throw new IllegalArgumentException("请输入姓名");
        }
        if (isBlank(password)) {
            throw new IllegalArgumentException("请输入密码");
        }
        if (isBlank(phone) || !PHONE.matcher(phone).matches()) {
            throw new IllegalArgumentException("手机号格式不正确（11 位，1 开头）");
        }
        if (isBlank(role)) {
            throw new IllegalArgumentException("请选择角色");
        }
        if (isBlank(major)) {
            throw new IllegalArgumentException("请选择或填写专业");
        }
        if (userDao.findByName(name) != null) {
            throw new IllegalArgumentException("同名账号不能重复注册");
        }
        if (userDao.findByPhone(phone) != null) {
            throw new IllegalArgumentException("该手机号已被注册");
        }

        User user = createByRole(role);
        user.setName(name);
        user.setPassword(password);
        user.setPhone(phone);
        user.setRole(normalizeRole(role));
        user.setMajor(major);
        fillRoleFields(user, form);
        userDao.save(user);
        return user;
    }

    public List<User> search(String keyword) throws Exception {
        if (isBlank(keyword)) {
            throw new IllegalArgumentException("请输入查询关键字");
        }
        return userDao.search(keyword.trim());
    }

    public User findById(String id) throws Exception {
        return userDao.findById(id);
    }

    public Map<String, Object> toSafeMap(User user) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", user.getId());
        map.put("name", user.getName());
        map.put("phone", user.getPhone());
        map.put("role", user.getRole());
        map.put("major", user.getMajor());
        map.put("loginTime", user.getLoginTime());
        map.put("createTime", user.getCreateTime());
        map.put("updateTime", user.getUpdateTime());
        map.put("permissions", user.hasPermission() == null ? new String[0] : Arrays.asList(user.hasPermission()));
        if (user instanceof Admin) {
            Admin a = (Admin) user;
            map.put("permissionLevel", a.getPermissionLevel());
            map.put("entryTime", a.getEntryTime());
        } else if (user instanceof Operator) {
            Operator o = (Operator) user;
            map.put("area", o.getArea());
            map.put("duty", o.getDuty());
        } else if (user instanceof Student) {
            Student s = (Student) user;
            map.put("grade", s.getGrade());
            map.put("college", s.getCollege());
        } else if (user instanceof Teacher) {
            Teacher t = (Teacher) user;
            map.put("college", t.getCollege());
            map.put("title", t.getTitle());
        }
        return map;
    }

    public static String normalizeRole(String role) {
        if (role == null) {
            return ROLE_STUDENT;
        }
        switch (role.trim()) {
            case "管理员":
            case "admin":
            case "Admin":
                return ROLE_ADMIN;
            case "操作员":
            case "operator":
            case "Operator":
                return ROLE_OPERATOR;
            case "教师":
            case "teacher":
            case "Teacher":
                return ROLE_TEACHER;
            default:
                return ROLE_STUDENT;
        }
    }

    private User createByRole(String role) {
        String n = normalizeRole(role);
        if (ROLE_ADMIN.equals(n)) {
            return new Admin();
        }
        if (ROLE_OPERATOR.equals(n)) {
            return new Operator();
        }
        if (ROLE_TEACHER.equals(n)) {
            return new Teacher();
        }
        return new Student();
    }

    private void fillRoleFields(User user, Map<String, String> form) {
        if (user instanceof Admin) {
            Admin a = (Admin) user;
            a.setPermissionLevel(parseInt(form.get("permissionLevel"), 1));
            String entry = trim(form.get("entryTime"));
            a.setEntryTime(isBlank(entry) ? LocalDate.now() : LocalDate.parse(entry.substring(0, 10)));
        } else if (user instanceof Operator) {
            Operator o = (Operator) user;
            o.setArea(require(form.get("area"), "请输入负责区域"));
            o.setDuty(require(form.get("duty"), "请输入职务"));
        } else if (user instanceof Student) {
            Student s = (Student) user;
            s.setGrade(require(form.get("grade"), "请输入年级"));
            s.setCollege(require(form.get("college"), "请输入所属学院"));
        } else if (user instanceof Teacher) {
            Teacher t = (Teacher) user;
            t.setCollege(require(form.get("college"), "请输入所属学院"));
            t.setTitle(require(form.get("title"), "请输入职称"));
        }
    }

    private static Integer parseInt(String v, int def) {
        try {
            return isBlank(v) ? def : Integer.parseInt(v.trim());
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private static String require(String v, String msg) {
        if (isBlank(v)) {
            throw new IllegalArgumentException(msg);
        }
        return v.trim();
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static String trim(String s) {
        return s == null ? null : s.trim();
    }
}
