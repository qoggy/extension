package io.github.qoggy.extension.core;

import io.github.qoggy.extension.core.util.ToStringHelper;

import java.util.Objects;

/**
 * Extension implementation wrapper that encapsulates an extension instance along with its matcher and priority.
 * Provides a unified way to manage extension implementations with their associated metadata.
 *
 * @author yutianhong
 * @version 1.0
 * @since 2025/10/4 22:20
 */
public final class ExtensionImpl {
    private static final Matcher<Object> DEFAULT_MATCHER = (param) -> true;
    private static final Priority DEFAULT_PRIORITY = () -> 0;

    private final Object instance;
    private final Matcher<?> matcher;
    private final Priority priority;

    /**
     * Creates a Builder instance for constructing ExtensionImpl objects.
     *
     * @param instance The extension implementation instance
     * @return Builder instance for fluent configuration
     */
    public static Builder builder(Object instance) {
        return new Builder(instance);
    }

    /**
     * Constructs an extension implementation with the specified matcher and priority.
     */
    ExtensionImpl(Object instance, Matcher<?> matcher, Priority priority) {
        this.instance = Objects.requireNonNull(instance, "Extension instance must not be null");
        this.matcher = matcher != null ? matcher : DEFAULT_MATCHER;
        this.priority = priority != null ? priority : DEFAULT_PRIORITY;
    }

    /**
     * @return The extension implementation instance
     */
    public Object getInstance() {
        return instance;
    }

    /**
     * @return The matcher instance for context matching
     */
    public <T> Matcher<T> getMatcher() {
        return (Matcher<T>) matcher;
    }

    /**
     * @return The priority instance for ordering
     */
    public Priority getPriority() {
        return priority;
    }

    /**
     * Checks if this extension implementation can be assigned to the specified extension point interface.
     *
     * @param extensionPointInterface The extension point interface to check against
     * @return true if this implementation can be assigned to the interface, false otherwise
     */
    public boolean isAssignableTo(Class<?> extensionPointInterface) {
        Objects.requireNonNull(extensionPointInterface, "Extension point interface must not be null");
        return extensionPointInterface.isAssignableFrom(instance.getClass());
    }

    /**
     * Builder class for ExtensionImpl that provides a fluent API for constructing ExtensionImpl instances.
     */
    public static final class Builder {
        private final Object instance;
        private Matcher<?> matcher;
        private Priority priority;

        private Builder(Object instance) {
            this.instance = Objects.requireNonNull(instance, "Extension instance must not be null");
            if (instance instanceof Matcher) {
                this.matcher = (Matcher<?>) instance;
            }
            if (instance instanceof Priority) {
                this.priority = (Priority) instance;
            }
        }

        /**
         * Sets the matcher for this extension implementation.
         */
        public Builder matcher(Matcher<?> matcher) {
            this.matcher = matcher;
            return this;
        }

        /**
         * Sets the priority for this extension implementation.
         */
        public Builder priority(Priority priority) {
            this.priority = priority;
            return this;
        }

        /**
         * Sets the priority value for this extension implementation.
         */
        public Builder priority(int priorityValue) {
            this.priority = () -> priorityValue;
            return this;
        }

        public ExtensionImpl build() {
            return new ExtensionImpl(instance, matcher, priority);
        }
    }

    @Override
    public String toString() {
        return ToStringHelper.toStringHelper(this)
                .add("instance", instance.getClass().getSimpleName())
                .add("matcher", matcher.getClass().getSimpleName())
                .add("priority", priority.getPriority())
                .toString();
    }
}
