package io.github.qoggy.extension.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ExtensionScope测试
 *
 * @author yutianhong
 * @version 1.0
 * @since 2025/10/5
 */
class ExtensionScopeTest {

    @Test
    @DisplayName("测试ExtensionScope自动关闭")
    void testExtensionScope_autoClose() {
        ExtensionScopeHolder scopeHolder = new ExtensionScopeHolder();
        String contextData = "test-context";

        // 使用try-with-resources
        try (ExtensionScope scope = new ExtensionScope(scopeHolder, contextData)) {
            // 在作用域内应该能获取到上下文
            String result = scopeHolder.getContext(String.class);
            assertEquals(contextData, result);
        }

        // 作用域关闭后应该获取不到上下文
        String result = scopeHolder.getContext(String.class);
        assertNull(result);
    }

    @Test
    @DisplayName("测试ExtensionScope手动关闭")
    void testExtensionScope_manualClose() {
        ExtensionScopeHolder scopeHolder = new ExtensionScopeHolder();
        String contextData = "manual-close-context";

        ExtensionScope scope = new ExtensionScope(scopeHolder, contextData);

        // 在关闭前应该能获取到上下文
        String result = scopeHolder.getContext(String.class);
        assertEquals(contextData, result);

        // 手动关闭
        scope.close();

        // 关闭后应该获取不到上下文
        result = scopeHolder.getContext(String.class);
        assertNull(result);
    }

    @Test
    @DisplayName("测试ExtensionScope重复关闭")
    void testExtensionScope_multipleClose() {
        ExtensionScopeHolder scopeHolder = new ExtensionScopeHolder();
        String contextData = "multiple-close-context";

        ExtensionScope scope = new ExtensionScope(scopeHolder, contextData);

        // 验证上下文存在
        String result = scopeHolder.getContext(String.class);
        assertEquals(contextData, result);

        // 第一次关闭
        scope.close();
        result = scopeHolder.getContext(String.class);
        assertNull(result);

        // 第二次关闭不应该抛出异常
        assertDoesNotThrow(() -> {
            scope.close();
        });

        // 上下文仍然应该是null
        result = scopeHolder.getContext(String.class);
        assertNull(result);
    }

    @Test
    @DisplayName("测试嵌套ExtensionScope")
    void testExtensionScope_nested() {
        ExtensionScopeHolder scopeHolder = new ExtensionScopeHolder();
        String outerContext = "outer-context";
        String innerContext = "inner-context";

        try (ExtensionScope outerScope = new ExtensionScope(scopeHolder, outerContext)) {
            // 外层作用域
            String result = scopeHolder.getContext(String.class);
            assertEquals(outerContext, result);

            try (ExtensionScope innerScope = new ExtensionScope(scopeHolder, innerContext)) {
                // 内层作用域，应该获取到内层上下文
                result = scopeHolder.getContext(String.class);
                assertEquals(innerContext, result);
            }

            // 内层作用域关闭后，应该恢复到外层上下文
            result = scopeHolder.getContext(String.class);
            assertEquals(outerContext, result);
        }

        // 所有作用域关闭后，应该获取不到上下文
        String result = scopeHolder.getContext(String.class);
        assertNull(result);
    }

    @Test
    @DisplayName("测试不同类型的上下文")
    void testExtensionScope_differentTypes() {
        ExtensionScopeHolder scopeHolder = new ExtensionScopeHolder();
        String stringContext = "string-context";
        Integer intContext = 42;

        try (ExtensionScope stringScope = new ExtensionScope(scopeHolder, stringContext);
             ExtensionScope intScope = new ExtensionScope(scopeHolder, intContext)) {

            // 应该能获取到两种类型的上下文
            String stringResult = scopeHolder.getContext(String.class);
            assertEquals(stringContext, stringResult);

            Integer intResult = scopeHolder.getContext(Integer.class);
            assertEquals(intContext, intResult);
        }

        // 作用域关闭后应该都获取不到
        String stringResult = scopeHolder.getContext(String.class);
        assertNull(stringResult);

        Integer intResult = scopeHolder.getContext(Integer.class);
        assertNull(intResult);
    }

    @Test
    @DisplayName("测试null上下文")
    void testExtensionScope_nullContext() {
        ExtensionScopeHolder scopeHolder = new ExtensionScopeHolder();

        // 使用null上下文不应该抛出异常
        assertDoesNotThrow(() -> {
            try (ExtensionScope scope = new ExtensionScope(scopeHolder, null)) {
                // null上下文也会被推入栈中
                Object result = scopeHolder.getContext(Object.class);
                assertNull(result); // 但获取时会被跳过
            }
        });
    }

    @Test
    @DisplayName("测试ExtensionScope在异常情况下的清理")
    void testExtensionScope_exceptionCleanup() {
        ExtensionScopeHolder scopeHolder = new ExtensionScopeHolder();
        String contextData = "exception-context";

        // 模拟在try-with-resources中抛出异常
        RuntimeException thrownException = assertThrows(RuntimeException.class, () -> {
            try (ExtensionScope scope = new ExtensionScope(scopeHolder, contextData)) {
                // 验证上下文存在
                String result = scopeHolder.getContext(String.class);
                assertEquals(contextData, result);

                // 抛出异常
                throw new RuntimeException("Test exception");
            }
        });

        assertEquals("Test exception", thrownException.getMessage());

        // 即使抛出异常，作用域也应该被正确清理
        String result = scopeHolder.getContext(String.class);
        assertNull(result);
    }

    @Test
    @DisplayName("测试多线程环境下的ExtensionScope")
    void testExtensionScope_multiThread() throws InterruptedException {
        ExtensionScopeHolder scopeHolder = new ExtensionScopeHolder();
        String mainThreadContext = "main-thread-context";

        // 在主线程创建作用域
        try (ExtensionScope mainScope = new ExtensionScope(scopeHolder, mainThreadContext)) {
            // 验证主线程上下文
            String mainResult = scopeHolder.getContext(String.class);
            assertEquals(mainThreadContext, mainResult);

            // 用于存储其他线程的结果
            final String[] otherThreadResult = new String[1];
            final Exception[] otherThreadException = new Exception[1];

            // 在其他线程中创建独立的作用域
            Thread otherThread = new Thread(() -> {
                try {
                    String otherThreadContext = "other-thread-context";
                    try (ExtensionScope otherScope = new ExtensionScope(scopeHolder, otherThreadContext)) {
                        // 其他线程应该只能看到自己的上下文
                        String result = scopeHolder.getContext(String.class);
                        otherThreadResult[0] = result;
                    }
                } catch (Exception e) {
                    otherThreadException[0] = e;
                }
            });

            otherThread.start();
            otherThread.join();

            // 检查其他线程的结果
            if (otherThreadException[0] != null) {
                throw new AssertionError("Exception in other thread", otherThreadException[0]);
            }

            assertEquals("other-thread-context", otherThreadResult[0]);

            // 主线程的上下文应该不受影响
            mainResult = scopeHolder.getContext(String.class);
            assertEquals(mainThreadContext, mainResult);
        }
    }
}
