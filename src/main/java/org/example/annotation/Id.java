package org.example.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标注在实体类的“主键”字段上。
 *
 * <p>JDBCTool 在 update / delete / getOneById 时用主键字段的值作为 WHERE 条件。
 * 如果类中没有字段标注 @Id，则默认把名为 {@code id} 的字段视为主键。</p>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Id {
}
