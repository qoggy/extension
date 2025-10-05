package io.github.qoggy.extension.core;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yutianhong
 * @version 1.0
 * @since 2025/10/4 22:49
 */
class ExtensionScopeHolder {
    private final ThreadLocal<List<Object>> contextStack = ThreadLocal.withInitial(ArrayList::new);

    public void push(Object context) {
        contextStack.get().add(context);
    }

    public <T> T getContext(Class<T> contextClass) {
        List<Object> stack = contextStack.get();
        for (int i = stack.size() - 1; i >= 0; i--) {
            Object obj = stack.get(i);
            if (obj != null && contextClass.isAssignableFrom(obj.getClass())) {
                return (T) obj;
            }
        }
        return null;
    }

    public void pop() {
        List<Object> stack = contextStack.get();
        if (!stack.isEmpty()) {
            stack.remove(stack.size() - 1);
        }
        if (stack.isEmpty()) {
            contextStack.remove();
        }
    }
}
