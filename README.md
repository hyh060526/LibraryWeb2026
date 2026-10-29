# LibraryWeb2026 图书馆管理系统

GitHub：https://github.com/hyh060526/LibraryWeb2026

Servlet + JDBC + HTML/CSS/jQuery/Axios/Vue（无 JSP、无 Spring）。

## 技术栈

- JDK 17
- Maven WAR
- Tomcat 9（`javax.servlet`）
- MySQL 8，数据库名 `libraryadmin`（由备份还原，不自行建表）
- 作业 1 的 `JDBCTool`（反射 ORM）

## 数据库还原

```bash
# 已提供还原脚本 db/libraryadmin.sql
mysql -u root -p < db/libraryadmin.sql
```

修改 `src/main/resources/db.properties` 中的账号密码。

## 演示账号（备份数据）

| 用户名 | 密码 | 角色 |
| --- | --- | --- |
| admin | admin123 | 管理员 |
| operator | operator123 | 操作员 |
| student1 | student123 | 学生 |
| teacher | teacher123 | 教师 |

## 本地运行

```bash
mvn -DskipTests package
# 将 target/LibraryWeb2026.war 拷到 Tomcat webapps/
# 访问 http://localhost:8080/LibraryWeb2026/
```

局域网访问：把 `localhost` 换成本机 IP。

## 功能

- 用户：注册（按角色动态字段）、登录（验证码+重置）、按角色功能卡、管理员用户查询
- 图书：添加（自动生成副本）、查询、编辑、废弃、副本增删、借书、还书、推荐、审阅
- 查询选作：借阅记录查询、借阅统计、个人借阅查询
