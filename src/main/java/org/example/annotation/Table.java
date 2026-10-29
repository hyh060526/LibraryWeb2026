package org.example.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标注在实体类上，声明该类映射到数据库中的哪张表。
 *
 * <p>如果不写该注解（或 value 为空），JDBCTool 会默认取类名的全小写形式作为表名
 * （例如 {@code Book -> book}、{@code BookCopy -> bookcopy}）。</p>
 *
 * <p>{@link Inherited}：让子类继承父类的表名映射，例如 {@code Admin extends User}，
 * 而 {@code User} 标注了 {@code @Table("user")}，则 Admin 也映射到 user 表。</p>
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Table {

    /** 表名 */
    String value() default "";
}
