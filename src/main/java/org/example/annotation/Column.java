package org.example.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标注在实体类字段上，声明该字段对应数据库表中的哪一列。
 *
 * <p>本项目的数据库列名与 Java 字段名同为驼峰命名（如 {@code createTime}、{@code bookCopyId}），
 * 因此通常无需显式标注；仅当字段名与列名不一致时才需要写本注解。</p>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Column {

    /** 列名 */
    String value() default "";
}
