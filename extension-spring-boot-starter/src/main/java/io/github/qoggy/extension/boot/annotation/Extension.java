package io.github.qoggy.extension.boot.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * Marks a class as an extension implementation that should be automatically registered
 * with the extension context. This annotation combines Spring's {@code @Component}
 * functionality with extension framework registration.
 *
 * <p>Classes annotated with {@code @Extension} will be:
 * <ul>
 * <li>Registered as Spring beans</li>
 * <li>Automatically discovered and registered with the extension context</li>
 * <li>Available for extension point resolution</li>
 * </ul>
 *
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
