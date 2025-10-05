package io.github.qoggy.extension.core;

/**
 * @author yutianhong
 * @version 1.0
 * @since 2025/10/4 22:19
 */
public interface Matcher<T> {
    boolean match(T param);
}
