package io.github.qoggy.extension.core;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 上下文作用域，用于管理上下文的生命周期
 *
 * @author yutianhong
 * @version 1.0
 * @since 2025/10/5 03:20
 */
public class ExtensionScope implements AutoCloseable {
    private final AtomicBoolean closed = new AtomicBoolean(false);
    private final ExtensionScopeHolder extensionScopeHolder;

    ExtensionScope(ExtensionScopeHolder extensionScopeHolder, Object scopeContext) {
        this.extensionScopeHolder = extensionScopeHolder;
        this.extensionScopeHolder.push(scopeContext);
    }

    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            this.extensionScopeHolder.pop();
        }
    }
}
