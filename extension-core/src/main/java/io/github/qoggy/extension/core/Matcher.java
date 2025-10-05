package io.github.qoggy.extension.core;

/**
 * Matcher interface for determining whether an extension implementation should be selected
 * based on the current context. Extension implementations can implement this interface
 * to provide custom matching logic.
 *
 * @param <T> The type of context object used for matching
 * @author yutianhong
 * @version 1.0
 * @since 2025/10/4 22:19
 */
public interface Matcher<T> {
    /**
     * Determines if this extension implementation matches the given context.
     *
     * @param param The context object to match against
     * @return true if this extension should be selected for the given context, false otherwise
     */
    boolean match(T param);
}
