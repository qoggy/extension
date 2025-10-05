package io.github.qoggy.extension.core;

/**
 * Priority interface for controlling the execution order of extension implementations.
 * Lower priority values indicate higher precedence (similar to Spring's Ordered interface).
 * Extension implementations can implement this interface to define their execution priority.
 *
 * @author yutianhong
 * @version 1.0
 * @since 2025/10/5 00:11
 */
public interface Priority {
    /**
     * Constant for the highest precedence value
     */
    int HIGHEST_PRECEDENCE = Integer.MIN_VALUE;
    /**
     * Constant for the lowest precedence value
     */
    int LOWEST_PRECEDENCE = Integer.MAX_VALUE;

    /**
     * Returns the priority value for this extension implementation.
     * Lower values indicate higher priority (executed first).
     *
     * @return The priority value, where lower numbers mean higher priority
     */
    int getPriority();
}
