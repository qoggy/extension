package io.github.qoggy.extension.core;

/**
 * @author yutianhong
 * @version 1.0
 * @since 2025/10/5 00:11
 */
public interface Priority {
    int HIGHEST_PRECEDENCE = Integer.MIN_VALUE;
    int LOWEST_PRECEDENCE = Integer.MAX_VALUE;

    int getPriority();
}
