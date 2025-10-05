package io.github.qoggy.extension.boot.annotation;

import java.lang.annotation.*;

/**
 * Annotation for injecting extension point proxies into fields.
 * Fields annotated with {@code @ExtensionInject} will automatically receive
 * a proxy object that delegates method calls to the appropriate extension
 * implementations based on the current context.
 *
 * <p>The injected proxy will:
 * <ul>
 * <li>Automatically select the matching extension implementation</li>
 * <li>Route method calls based on current context and priority</li>
 * <li>Handle multiple implementations according to priority ordering</li>
 * </ul>
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
