package org.example.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标注在实体类字段上，表示该字段不参与数据库持久化（不映射到任何列）。
 *
 * <p>主要用于对象间的关联引用（如 {@code Book.bookCopies}、{@code Loan.user}），
 * 这些引用是内存中的关系，真正持久化到数据库的是对应的外键 id 列。</p>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Transient {
}
