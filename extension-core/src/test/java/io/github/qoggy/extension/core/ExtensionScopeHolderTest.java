package io.github.qoggy.extension.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ExtensionScopeHolder测试
 *
 * @author yutianhong
 * @version 1.0
 * @since 2025/10/5
 */
class ExtensionScopeHolderTest {

    private ExtensionScopeHolder scopeHolder;

    @BeforeEach
    void setUp() {
        scopeHolder = new ExtensionScopeHolder();
    }

    @Test
    @DisplayName("测试push和getContext基本功能")
    void testPush_and_getContext_basic() {
        // 准备测试数据
        String context = "test-context";

        // 推入上下文
        scopeHolder.push(context);

        // 获取上下文
        String result = scopeHolder.getContext(String.class);
        assertEquals(context, result);
    }

    @Test
    @DisplayName("测试getContext_未找到匹配类型")
    void testGetContext_notFound() {
        // 推入String类型的上下文
        scopeHolder.push("test-string");

        // 尝试获取Integer类型的上下文
        Integer result = scopeHolder.getContext(Integer.class);
        assertNull(result);
    }

    @Test
    @DisplayName("测试getContext_空栈")
    void testGetContext_emptyStack() {
        // 未推入任何上下文，直接获取
        String result = scopeHolder.getContext(String.class);
        assertNull(result);
    }

    @Test
    @DisplayName("测试多层上下文栈")
    void testMultipleContextStack() {
        // 推入多个上下文
        scopeHolder.push("first-context");
        scopeHolder.push(123);
        scopeHolder.push("second-context");

        // 获取String类型上下文，应该返回最后推入的String
        String stringResult = scopeHolder.getContext(String.class);
        assertEquals("second-context", stringResult);

        // 获取Integer类型上下文
        Integer intResult = scopeHolder.getContext(Integer.class);
        assertEquals(123, intResult);
    }

    @Test
    @DisplayName("测试上下文类型继承")
    void testContextInheritance() {
        // 推入子类型对象
        StringBuilder sb = new StringBuilder("test");
        scopeHolder.push(sb);

        // 使用父类型获取上下文
        CharSequence charSeq = scopeHolder.getContext(CharSequence.class);
        assertNotNull(charSeq);
        assertEquals(sb, charSeq);

        // 使用Object类型获取上下文
        Object obj = scopeHolder.getContext(Object.class);
        assertNotNull(obj);
        assertEquals(sb, obj);
    }

    @Test
    @DisplayName("测试pop基本功能")
    void testPop_basic() {
        // 推入上下文
        scopeHolder.push("context1");
        scopeHolder.push("context2");

        // 验证当前栈顶
        String current = scopeHolder.getContext(String.class);
        assertEquals("context2", current);

        // 弹出栈顶
        scopeHolder.pop();

        // 验证新的栈顶
        current = scopeHolder.getContext(String.class);
        assertEquals("context1", current);
    }

    @Test
    @DisplayName("测试pop_空栈")
    void testPop_emptyStack() {
        // 在空栈上调用pop不应该抛出异常
        assertDoesNotThrow(() -> {
            scopeHolder.pop();
        });
    }

    @Test
    @DisplayName("测试pop_清空栈后ThreadLocal被移除")
    void testPop_threadLocalCleanup() {
        // 推入上下文
        scopeHolder.push("only-context");

        // 验证上下文存在
        String context = scopeHolder.getContext(String.class);
        assertEquals("only-context", context);

        // 弹出最后一个上下文
        scopeHolder.pop();

        // 验证上下文已被清空
        context = scopeHolder.getContext(String.class);
        assertNull(context);
    }

    @Test
    @DisplayName("测试null上下文处理")
    void testNullContext() {
        // 推入null上下文
        scopeHolder.push(null);
        scopeHolder.push("valid-context");

        // 获取上下文应该跳过null值
        String result = scopeHolder.getContext(String.class);
        assertEquals("valid-context", result);
    }

    @Test
    @DisplayName("测试ThreadLocal隔离")
    void testThreadLocalIsolation() throws InterruptedException {
        // 在主线程推入上下文
        scopeHolder.push("main-thread-context");

        // 用于存储其他线程的结果
        final String[] otherThreadResult = new String[1];
        final Exception[] otherThreadException = new Exception[1];

        // 创建其他线程
        Thread otherThread = new Thread(() -> {
            try {
                // 其他线程应该看不到主线程的上下文
                String result = scopeHolder.getContext(String.class);
                otherThreadResult[0] = result;

                // 在其他线程推入自己的上下文
                scopeHolder.push("other-thread-context");
                String ownContext = scopeHolder.getContext(String.class);
                if (!"other-thread-context".equals(ownContext)) {
                    throw new AssertionError("Other thread context not isolated");
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

        // 其他线程应该看不到主线程的上下文
        assertNull(otherThreadResult[0]);

        // 主线程的上下文应该不受影响
        String mainThreadContext = scopeHolder.getContext(String.class);
        assertEquals("main-thread-context", mainThreadContext);
    }

    @Test
    @DisplayName("测试复杂上下文查找场景")
    void testComplexContextLookup() {
        // 推入多种类型的上下文
        scopeHolder.push("string1");
        scopeHolder.push(100);
        scopeHolder.push("string2");
        scopeHolder.push(200L);
        scopeHolder.push("string3");

        // 验证获取最近的String类型上下文
        String stringResult = scopeHolder.getContext(String.class);
        assertEquals("string3", stringResult);

        // 验证获取Integer类型上下文
        Integer intResult = scopeHolder.getContext(Integer.class);
        assertEquals(100, intResult);

        // 验证获取Long类型上下文
        Long longResult = scopeHolder.getContext(Long.class);
        assertEquals(200L, longResult);

        // 弹出最后的String上下文
        scopeHolder.pop();

        // 现在应该获取到倒数第二个String上下文
        stringResult = scopeHolder.getContext(String.class);
        assertEquals("string2", stringResult);
    }
}
