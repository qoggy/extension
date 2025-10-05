package io.github.qoggy.extension.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ExtensionImpl测试
 *
 * @author yutianhong
 * @version 1.0
 * @since 2025/10/5
 */
class ExtensionImplTest {

    @Test
    @DisplayName("测试Builder创建ExtensionImpl")
    void testBuilder_basic() {
        // 准备测试数据
        TestExtension instance = new TestExtension();

        // 使用Builder创建ExtensionImpl
        ExtensionImpl extension = ExtensionImpl.builder(instance).build();

        // 验证结果
        assertNotNull(extension);
        assertEquals(instance, extension.getInstance());
        assertNotNull(extension.getMatcher());
        assertNotNull(extension.getPriority());
        assertEquals(0, extension.getPriority().getPriority()); // 默认优先级
    }

    @Test
    @DisplayName("测试Builder设置匹配器")
    void testBuilder_withMatcher() {
        // 准备测试数据
        TestExtension instance = new TestExtension();
        TestMatcher matcher = new TestMatcher();

        // 使用Builder创建ExtensionImpl
        ExtensionImpl extension = ExtensionImpl.builder(instance)
                .matcher(matcher)
                .build();

        // 验证结果
        assertEquals(matcher, extension.getMatcher());
    }

    @Test
    @DisplayName("测试Builder设置优先级对象")
    void testBuilder_withPriorityObject() {
        // 准备测试数据
        TestExtension instance = new TestExtension();
        Priority priority = () -> 100;

        // 使用Builder创建ExtensionImpl
        ExtensionImpl extension = ExtensionImpl.builder(instance)
                .priority(priority)
                .build();

        // 验证结果
        assertEquals(priority, extension.getPriority());
        assertEquals(100, extension.getPriority().getPriority());
    }

    @Test
    @DisplayName("测试Builder设置优先级值")
    void testBuilder_withPriorityValue() {
        // 准备测试数据
        TestExtension instance = new TestExtension();

        // 使用Builder创建ExtensionImpl
        ExtensionImpl extension = ExtensionImpl.builder(instance)
                .priority(200)
                .build();

        // 验证结果
        assertEquals(200, extension.getPriority().getPriority());
    }

    @Test
    @DisplayName("测试Builder链式调用")
    void testBuilder_chainedCalls() {
        // 准备测试数据
        TestExtension instance = new TestExtension();
        TestMatcher matcher = new TestMatcher();

        // 链式调用
        ExtensionImpl extension = ExtensionImpl.builder(instance)
                .matcher(matcher)
                .priority(300)
                .build();

        // 验证结果
        assertEquals(instance, extension.getInstance());
        assertEquals(matcher, extension.getMatcher());
        assertEquals(300, extension.getPriority().getPriority());
    }

    @Test
    @DisplayName("测试Builder_null实例")
    void testBuilder_nullInstance() {
        // 验证null实例抛出异常
        assertThrows(NullPointerException.class, () -> {
            ExtensionImpl.builder(null);
        });
    }

    @Test
    @DisplayName("测试isAssignableTo方法")
    void testIsAssignableTo() {
        // 准备测试数据
        TestExtension instance = new TestExtension();
        ExtensionImpl extension = ExtensionImpl.builder(instance).build();

        // 测试正确的接口分配
        assertTrue(extension.isAssignableTo(TestExtensionPoint.class));
        assertTrue(extension.isAssignableTo(Object.class));

        // 测试错误的接口分配
        assertFalse(extension.isAssignableTo(String.class));
        assertFalse(extension.isAssignableTo(Integer.class));
    }

    @Test
    @DisplayName("测试isAssignableTo_null参数")
    void testIsAssignableTo_nullParameter() {
        // 准备测试数据
        TestExtension instance = new TestExtension();
        ExtensionImpl extension = ExtensionImpl.builder(instance).build();

        // 验证null参数抛出异常
        assertThrows(NullPointerException.class, () -> {
            extension.isAssignableTo(null);
        });
    }

    @Test
    @DisplayName("测试默认匹配器")
    void testDefaultMatcher() {
        // 准备测试数据
        TestExtension instance = new TestExtension();
        ExtensionImpl extension = ExtensionImpl.builder(instance).build();

        // 验证默认匹配器总是返回true
        assertTrue(extension.getMatcher().match(null));
        assertTrue(extension.getMatcher().match("any-object"));
        assertTrue(extension.getMatcher().match(123));
    }

    @Test
    @DisplayName("测试null匹配器使用默认值")
    void testNullMatcherUsesDefault() {
        // 准备测试数据
        TestExtension instance = new TestExtension();
        ExtensionImpl extension = ExtensionImpl.builder(instance)
                .matcher(null)
                .build();

        // 验证使用默认匹配器
        assertTrue(extension.getMatcher().match("anything"));
    }

    @Test
    @DisplayName("测试null优先级使用默认值")
    void testNullPriorityUsesDefault() {
        // 准备测试数据
        TestExtension instance = new TestExtension();
        ExtensionImpl extension = ExtensionImpl.builder(instance)
                .priority((Priority) null)
                .build();

        // 验证使用默认优先级
        assertEquals(0, extension.getPriority().getPriority());
    }

    @Test
    @DisplayName("测试toString方法")
    void testToString() {
        // 准备测试数据
        TestExtension instance = new TestExtension();
        TestMatcher matcher = new TestMatcher();
        ExtensionImpl extension = ExtensionImpl.builder(instance)
                .matcher(matcher)
                .priority(500)
                .build();

        // 验证toString包含关键信息
        String toString = extension.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("TestExtension"));
        assertTrue(toString.contains("TestMatcher"));
        assertTrue(toString.contains("500"));
    }

    // 测试用的扩展点接口
    interface TestExtensionPoint {
        void doSomething();
    }

    // 测试用的扩展实现
    static class TestExtension implements TestExtensionPoint {
        @Override
        public void doSomething() {
            // 测试实现
        }
    }

    // 测试用的匹配器
    static class TestMatcher implements Matcher<Object> {
        @Override
        public boolean match(Object param) {
            return true;
        }
    }
}
