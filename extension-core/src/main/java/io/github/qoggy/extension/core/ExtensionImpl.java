package io.github.qoggy.extension.core;

import io.github.qoggy.extension.core.util.ToStringHelper;

import java.util.Objects;

/**
 * 扩展实现包装器，用于封装扩展实现实例及其匹配器和优先级
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
     * 创建ExtensionImpl的Builder实例
     *
     * @param instance 扩展实现实例
     * @return Builder实例
     */
    public static Builder builder(Object instance) {
        return new Builder(instance);
    }

    /**
     * 构造扩展实现，使用指定的匹配器和优先级
     *
     * @param instance 扩展实现实例，不能为null
     * @param matcher  匹配器实现，如果为null则使用默认匹配器（总是匹配）
     * @param priority 优先级实现，如果为null则使用默认优先级
     */
    ExtensionImpl(Object instance, Matcher<?> matcher, Priority priority) {
        this.instance = Objects.requireNonNull(instance, "Extension instance must not be null");
        this.matcher = matcher != null ? matcher : DEFAULT_MATCHER;
        this.priority = priority != null ? priority : DEFAULT_PRIORITY;
    }

    /**
     * ExtensionImpl的Builder类，提供流式API来构建ExtensionImpl实例
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
         * 设置匹配器
         */
        public Builder matcher(Matcher<?> matcher) {
            this.matcher = matcher;
            return this;
        }

        /**
         * 设置优先级
         */
        public Builder priority(Priority priority) {
            this.priority = priority;
            return this;
        }

        /**
         * 设置优先级值
         */
        public Builder priority(int priorityValue) {
            this.priority = () -> priorityValue;
            return this;
        }

        public ExtensionImpl build() {
            return new ExtensionImpl(instance, matcher, priority);
        }
    }

    /**
     * @return 扩展实现实例
     */
    public Object getInstance() {
        return instance;
    }

    /**
     * @return 匹配器实例
     */
    public <T> Matcher<T> getMatcher() {
        return (Matcher<T>) matcher;
    }

    /**
     * @return 优先级实例
     */
    public Priority getPriority() {
        return priority;
    }

    /**
     * 检查当前扩展实现是否可以分配给指定的扩展点接口
     *
     * @param extensionPointInterface 扩展点接口
     * @return 如果可以分配返回true，否则返回false
     */
    public boolean isAssignableTo(Class<?> extensionPointInterface) {
        Objects.requireNonNull(extensionPointInterface, "Extension point interface must not be null");
        return extensionPointInterface.isAssignableFrom(instance.getClass());
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
