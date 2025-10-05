package io.github.qoggy.extension.boot.annotation;

import java.lang.annotation.*;

/**
 * 扩展点注入注解，用于标记需要注入扩展实现的字段
 *
 * @author yutianhong
 * @version 1.0
 * @since 2025/10/4 22:40
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ExtensionInject {
}
