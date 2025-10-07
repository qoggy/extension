package io.github.qoggy.extension.core;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 上下文作用域，用于管理上下文的生命周期
 *
 * @param <T> 上下文对象的类型
 * @author yutianhong
 * @version 1.0
 * @since 2025/10/5 03:20
 */
public class ExtensionScope<T> implements AutoCloseable {
    private final AtomicBoolean closed = new AtomicBoolean(false);
    private final ExtensionScopeHolder extensionScopeHolder;
    private final T scopeContext;

    ExtensionScope(ExtensionScopeHolder extensionScopeHolder, T scopeContext) {
        this.extensionScopeHolder = extensionScopeHolder;
        this.scopeContext = scopeContext;
        this.extensionScopeHolder.push(this);
    }

    /**
     * 获取当前作用域的上下文对象
     *
     * @return 上下文对象
     */
    public T getScopeContext() {
        return scopeContext;
    }

    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            this.extensionScopeHolder.pop(this);
        }
    }
}
