package io.github.qoggy.extension.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ExtensionContext核心功能测试
 *
 * @author yutianhong
 * @version 1.0
 * @since 2025/10/5
 */
class ExtensionContextTest {

    private ExtensionContext extensionContext;

    @BeforeEach
    void setUp() {
        extensionContext = new ExtensionContext();
    }

    @Test
    @DisplayName("测试注册扩展实现")
    void testRegister_withExtensionImpl() {
        // 准备测试数据
        TestExtensionImpl impl1 = new TestExtensionImpl("impl1");
        TestExtensionImpl impl2 = new TestExtensionImpl("impl2");
        ExtensionImpl extension1 = ExtensionImpl.builder(impl1).build();
        ExtensionImpl extension2 = ExtensionImpl.builder(impl2).build();

        // 执行注册
        extensionContext.register(extension1, extension2);

        // 验证结果
        List<ExtensionImpl> allExtensions = extensionContext.getAll();
        assertEquals(2, allExtensions.size());
        assertTrue(allExtensions.contains(extension1));
        assertTrue(allExtensions.contains(extension2));
    }

    @Test
    @DisplayName("测试注册扩展实现实例")
    void testRegister_withInstances() {
        // 准备测试数据
        TestExtensionImpl impl1 = new TestExtensionImpl("impl1");
        TestExtensionImpl impl2 = new TestExtensionImpl("impl2");

        // 执行注册
        extensionContext.register(impl1, impl2);

        // 验证结果
        List<ExtensionImpl> allExtensions = extensionContext.getAll();
        assertEquals(2, allExtensions.size());
        assertEquals(impl1, allExtensions.get(0).getInstance());
        assertEquals(impl2, allExtensions.get(1).getInstance());
    }

    @Test
    @DisplayName("测试注册null参数")
    void testRegister_withNullParams() {
        // 测试ExtensionImpl数组为null
        extensionContext.register((ExtensionImpl[]) null);
        assertEquals(0, extensionContext.getAll().size());

        // 测试ExtensionImpl数组为空
        extensionContext.register(new ExtensionImpl[0]);
        assertEquals(0, extensionContext.getAll().size());

        // 测试实例数组为null
        extensionContext.register((Object[]) null);
        assertEquals(0, extensionContext.getAll().size());

        // 测试实例数组为空
        extensionContext.register(new Object[0]);
        assertEquals(0, extensionContext.getAll().size());
    }

    @Test
    @DisplayName("测试查找单个扩展实现")
    void testFind_singleExtension() {
        // 准备测试数据
        TestExtensionImpl impl = new TestExtensionImpl("test");
        extensionContext.register(impl);

        // 执行查找
        TestExtensionPoint result = extensionContext.find(TestExtensionPoint.class);

        // 验证结果
        assertNotNull(result);
        assertEquals(impl, result);
        assertEquals("test", result.getName());
    }

    @Test
    @DisplayName("测试查找不存在的扩展实现")
    void testFind_notFound() {
        // 执行查找
        TestExtensionPoint result = extensionContext.find(TestExtensionPoint.class);

        // 验证结果
        assertNull(result);
    }

    @Test
    @DisplayName("测试查找所有扩展实现")
    void testFindAll_multipleExtensions() {
        // 准备测试数据，按优先级排序
        TestExtensionImpl impl1 = new TestExtensionImpl("impl1");
        TestExtensionImpl impl2 = new TestExtensionImpl("impl2");
        TestExtensionImpl impl3 = new TestExtensionImpl("impl3");

        ExtensionImpl extension1 = ExtensionImpl.builder(impl1).priority(10).build();
        ExtensionImpl extension2 = ExtensionImpl.builder(impl2).priority(5).build();
        ExtensionImpl extension3 = ExtensionImpl.builder(impl3).priority(15).build();

        extensionContext.register(extension1, extension2, extension3);

        // 执行查找
        List<TestExtensionPoint> results = extensionContext.findAll(TestExtensionPoint.class);

        // 验证结果：应该按优先级从低到高排序（数值越小优先级越高）
        assertEquals(3, results.size());
        assertEquals("impl2", results.get(0).getName()); // 优先级5
        assertEquals("impl1", results.get(1).getName()); // 优先级10
        assertEquals("impl3", results.get(2).getName()); // 优先级15
    }

    @Test
    @DisplayName("测试查找所有扩展实现_空结果")
    void testFindAll_emptyResult() {
        // 执行查找
        List<TestExtensionPoint> results = extensionContext.findAll(TestExtensionPoint.class);

        // 验证结果
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("测试查找扩展实现_null参数")
    void testFind_nullParameter() {
        // 验证null参数抛出异常
        assertThrows(NullPointerException.class, () -> {
            extensionContext.find(null);
        });

        assertThrows(NullPointerException.class, () -> {
            extensionContext.findAll(null);
        });
    }

    @Test
    @DisplayName("测试创建代理对象")
    void testProxy() {
        // 准备测试数据
        TestExtensionImpl impl = new TestExtensionImpl("proxy-test");
        extensionContext.register(impl);

        // 创建代理
        TestExtensionPoint proxy = extensionContext.proxy(TestExtensionPoint.class);

        // 验证代理对象
        assertNotNull(proxy);
        assertNotEquals(impl, proxy); // 代理对象不等于原始对象
        assertEquals("proxy-test", proxy.getName()); // 但方法调用结果相同
    }

    @Test
    @DisplayName("测试上下文作用域管理")
    void testInitScope() {
        // 准备测试数据
        String contextData = "test-context";

        // 测试作用域创建和自动关闭
        try (ExtensionScope scope = extensionContext.initScope(contextData)) {
            assertNotNull(scope);
        }
        // 作用域应该自动关闭，无异常
    }

    @Test
    @DisplayName("测试上下文匹配")
    void testContextMatching() {
        // 准备测试数据
        TestContextMatcher matcher = new TestContextMatcher("expected-context");
        TestExtensionImpl impl = new TestExtensionImpl("context-test");
        ExtensionImpl extension = ExtensionImpl.builder(impl).matcher(matcher).build();

        extensionContext.register(extension);

        // 测试无上下文时找不到匹配的扩展
        TestExtensionPoint result = extensionContext.find(TestExtensionPoint.class);
        assertNull(result);

        // 测试有匹配上下文时能找到扩展
        try (ExtensionScope scope = extensionContext.initScope("expected-context")) {
            result = extensionContext.find(TestExtensionPoint.class);
            assertNotNull(result);
            assertEquals("context-test", result.getName());
        }

        // 测试上下文关闭后又找不到匹配的扩展
        result = extensionContext.find(TestExtensionPoint.class);
        assertNull(result);
    }

    @Test
    @DisplayName("测试缓存机制")
    void testCacheClearing() {
        // 准备测试数据
        TestExtensionImpl impl1 = new TestExtensionImpl("impl1");
        extensionContext.register(impl1);

        // 第一次查找，会缓存结果
        TestExtensionPoint result1 = extensionContext.find(TestExtensionPoint.class);
        assertNotNull(result1);

        // 注册新的扩展，缓存应该被清空
        TestExtensionImpl impl2 = new TestExtensionImpl("impl2");
        ExtensionImpl extension2 = ExtensionImpl.builder(impl2).priority(-1).build(); // 更高优先级
        extensionContext.register(extension2);

        // 再次查找，应该返回新的高优先级扩展
        TestExtensionPoint result2 = extensionContext.find(TestExtensionPoint.class);
        assertNotNull(result2);
        assertEquals("impl2", result2.getName());
    }

    // 测试用的扩展点接口
    public interface TestExtensionPoint {
        String getName();
    }

    // 测试用的扩展实现
    static class TestExtensionImpl implements TestExtensionPoint {
        private final String name;

        public TestExtensionImpl(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }
    }

    // 测试用的上下文匹配器
    static class TestContextMatcher implements Matcher<String> {
        private final String expectedContext;

        public TestContextMatcher(String expectedContext) {
            this.expectedContext = expectedContext;
        }

        @Override
        public boolean match(String param) {
            return expectedContext.equals(param);
        }
    }
}
