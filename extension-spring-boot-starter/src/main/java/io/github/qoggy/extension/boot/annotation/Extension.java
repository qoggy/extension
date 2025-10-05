package io.github.qoggy.extension.boot.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * @author yutianhong
 * @version 1.0
 * @since 2025/10/4 22:33
 */
@Component
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Extension {
}
