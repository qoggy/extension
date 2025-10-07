package io.github.qoggy.extension.core;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yutianhong
 * @version 1.0
 * @since 2025/10/4 22:49
 */
class ExtensionScopeHolder {
    private final ThreadLocal<List<ExtensionScope<?>>> scopeStack = ThreadLocal.withInitial(ArrayList::new);

    void push(ExtensionScope<?> scope) {
        scopeStack.get().add(scope);
    }

    <T> T getContext(Class<T> contextClass) {
        List<ExtensionScope<?>> stack = scopeStack.get();
        for (int i = stack.size() - 1; i >= 0; i--) {
            Object obj = stack.get(i).getScopeContext();
            if (obj != null && contextClass.isAssignableFrom(obj.getClass())) {
                return (T) obj;
            }
        }
        return null;
    }

    void pop(ExtensionScope<?> scope) {
        List<ExtensionScope<?>> stack = scopeStack.get();
        for (int i = stack.size() - 1; i >= 0; i--) {
            if (stack.get(i) == scope) {
                stack.remove(i);
                break;
            }
        }
        if (stack.isEmpty()) {
            scopeStack.remove();
        }
    }
}
