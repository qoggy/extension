package io.github.qoggy.extension.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ExtensionProxyFactory测试
 *
 * @author yutianhong
 * @version 1.0
 * @since 2025/10/5
 */
class ExtensionProxyFactoryTest {

    private ExtensionContext extensionContext;
    private ExtensionProxyFactory proxyFactory;

    @BeforeEach
    void setUp() {
        extensionContext = new ExtensionContext();
        proxyFactory = new ExtensionProxyFactory(extensionContext);
    }

    @Test
    @DisplayName("测试创建代理对象")
    void testCreateProxy_basic() {
        // 注册扩展实现
        TestExtensionImpl impl = new TestExtensionImpl("test-value");
        extensionContext.register(impl);

        // 创建代理
        TestExtensionPoint proxy = proxyFactory.createProxy(TestExtensionPoint.class);

        // 验证代理对象
        assertNotNull(proxy);
        assertNotEquals(impl, proxy); // 代理对象不等于原始实现

        // 验证代理方法调用
        String result = proxy.getValue();
        assertEquals("test-value", result);
    }

    @Test
    @DisplayName("测试代理对象缓存")
    void testCreateProxy_caching() {
        // 注册扩展实现
        TestExtensionImpl impl = new TestExtensionImpl("cached");
        extensionContext.register(impl);

        // 多次创建代理，应该返回相同的对象
        TestExtensionPoint proxy1 = proxyFactory.createProxy(TestExtensionPoint.class);
        TestExtensionPoint proxy2 = proxyFactory.createProxy(TestExtensionPoint.class);

        assertSame(proxy1, proxy2);
    }

    @Test
    @DisplayName("测试代理方法调用_无实现")
    void testCreateProxy_noImplementation() {
        // 创建代理但不注册任何实现
        TestExtensionPoint proxy = proxyFactory.createProxy(TestExtensionPoint.class);

        // 调用代理方法应该抛出RuntimeException
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            proxy.getValue();
        });

        assertTrue(exception.getMessage().contains("No extension implementation found"));
        assertTrue(exception.getMessage().contains("TestExtensionPoint"));
    }

    @Test
    @DisplayName("测试代理方法调用_实现抛出异常")
    void testCreateProxy_implementationThrowsException() {
        // 注册会抛出异常的扩展实现
        ExceptionThrowingExtension impl = new ExceptionThrowingExtension();
        extensionContext.register(impl);

        // 创建代理
        TestExtensionPoint proxy = proxyFactory.createProxy(TestExtensionPoint.class);

        // 调用代理方法应该传播原始异常
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            proxy.getValue();
        });

        assertEquals("Test exception from implementation", exception.getMessage());
    }

    @Test
    @DisplayName("测试代理方法调用_实现抛出检查异常")
    void testCreateProxy_implementationThrowsCheckedException() {
        // 注册会抛出检查异常的扩展实现
        CheckedExceptionThrowingExtension impl = new CheckedExceptionThrowingExtension();
        extensionContext.register(impl);

        // 创建代理
        CheckedExceptionExtensionPoint proxy = proxyFactory.createProxy(CheckedExceptionExtensionPoint.class);

        // 调用代理方法应该包装检查异常为RuntimeException
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            proxy.doSomethingWithCheckedException();
        });

        assertTrue(exception.getMessage().contains("Extension method"));
        assertTrue(exception.getCause() instanceof Exception);
        assertEquals("Checked exception", exception.getCause().getMessage());
    }

    @Test
    @DisplayName("测试代理方法调用_多个实现")
    void testCreateProxy_multipleImplementations() {
        // 注册多个扩展实现，优先级不同
        TestExtensionImpl impl1 = new TestExtensionImpl("impl1");
        TestExtensionImpl impl2 = new TestExtensionImpl("impl2");

        ExtensionImpl extension1 = ExtensionImpl.builder(impl1).priority(10).build();
        ExtensionImpl extension2 = ExtensionImpl.builder(impl2).priority(5).build(); // 更高优先级

        extensionContext.register(extension1, extension2);

        // 创建代理
        TestExtensionPoint proxy = proxyFactory.createProxy(TestExtensionPoint.class);

        // 应该调用优先级最高的实现
        String result = proxy.getValue();
        assertEquals("impl2", result);
    }

    @Test
    @DisplayName("测试代理方法调用_返回void")
    void testCreateProxy_voidMethod() {
        // 注册扩展实现
        VoidMethodExtension impl = new VoidMethodExtension();
        extensionContext.register(impl);

        // 创建代理
        VoidMethodExtensionPoint proxy = proxyFactory.createProxy(VoidMethodExtensionPoint.class);

        // 调用void方法不应该抛出异常
        assertDoesNotThrow(() -> {
            proxy.doVoidOperation();
        });

        // 验证方法确实被调用了
        assertTrue(impl.wasCalled());
    }

    @Test
    @DisplayName("测试清空缓存")
    void testClearCache() {
        // 注册扩展实现
        TestExtensionImpl impl = new TestExtensionImpl("test");
        extensionContext.register(impl);

        // 创建代理
        TestExtensionPoint proxy1 = proxyFactory.createProxy(TestExtensionPoint.class);

        // 清空缓存
        proxyFactory.clearCache();

        // 再次创建代理，应该是新的对象
        TestExtensionPoint proxy2 = proxyFactory.createProxy(TestExtensionPoint.class);

        assertNotSame(proxy1, proxy2);
    }

    // 测试用的扩展点接口
    public interface TestExtensionPoint {
        String getValue();
    }

    // 测试用的扩展实现
    static class TestExtensionImpl implements TestExtensionPoint {
        private final String value;

        public TestExtensionImpl(String value) {
            this.value = value;
        }

        @Override
        public String getValue() {
            return value;
        }
    }

    // 抛出运行时异常的扩展实现
    static class ExceptionThrowingExtension implements TestExtensionPoint {
        @Override
        public String getValue() {
            throw new RuntimeException("Test exception from implementation");
        }
    }

    // 抛出检查异常的扩展点接口
    public interface CheckedExceptionExtensionPoint {
        void doSomethingWithCheckedException() throws Exception;
    }

    // 抛出检查异常的扩展实现
    static class CheckedExceptionThrowingExtension implements CheckedExceptionExtensionPoint {
        @Override
        public void doSomethingWithCheckedException() throws Exception {
            throw new Exception("Checked exception");
        }
    }

    // void方法的扩展点接口
    public interface VoidMethodExtensionPoint {
        void doVoidOperation();
    }

    // void方法的扩展实现
    static class VoidMethodExtension implements VoidMethodExtensionPoint {
        private boolean called = false;

        @Override
        public void doVoidOperation() {
            called = true;
        }

        public boolean wasCalled() {
            return called;
        }
    }


    public interface TestInheritedExtensionPoint extends TestExtensionPoint {
        Integer getNumber();
    }

    static class TestInheritedExtensionImpl implements TestInheritedExtensionPoint {
        private final String value;
        private final Integer number;

        public TestInheritedExtensionImpl(String value, Integer number) {
            this.value = value;
            this.number = number;
        }

        @Override
        public Integer getNumber() {
            return number;
        }

        @Override
        public String getValue() {
            return value;
        }
    }

    @Test
    void testCreateProxy_inheritedExtensionPoint() {
        extensionContext.register(new TestInheritedExtensionImpl("test", 100));
        TestInheritedExtensionPoint proxy = proxyFactory.createProxy(TestInheritedExtensionPoint.class);
        assertEquals("test", proxy.getValue());
        assertEquals(100, proxy.getNumber());
    }
}
