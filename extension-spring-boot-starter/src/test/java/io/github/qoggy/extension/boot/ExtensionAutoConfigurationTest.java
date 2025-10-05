package io.github.qoggy.extension.boot;

import io.github.qoggy.extension.boot.annotation.Extension;
import io.github.qoggy.extension.boot.annotation.ExtensionInject;
import io.github.qoggy.extension.core.ExtensionContext;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Spring Boot自动配置测试
 *
 * @author yutianhong
 * @version 1.0
 * @since 2025/10/5
 */
class ExtensionAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ExtensionAutoConfiguration.class));

    @Test
    void testAutoConfiguration_shouldCreateExtensionContextBean() {
        contextRunner.run(context -> {
            assertTrue(context.containsBean("extensionContext"));
            assertTrue(context.containsBean("extensionInstantiationAwareBeanPostProcessor"));
            assertNotNull(context.getBean(ExtensionContext.class));
            assertNotNull(context.getBean(ExtensionInstantiationAwareBeanPostProcessor.class));
        });
    }

    @Test
    void testExtensionRegistration_shouldRegisterAutomatically() {
        contextRunner
                .withUserConfiguration(TestConfiguration.class)
                .run(context -> {
                    ExtensionContext extensionContext = context.getBean(ExtensionContext.class);

                    // 验证扩展实现已被自动注册
                    TestExtensionPoint extension = extensionContext.find(TestExtensionPoint.class);
                    assertNotNull(extension);
                    assertTrue(extension instanceof TestExtensionImpl);

                    // 验证扩展方法可以正常调用
                    String result = extension.doSomething();
                    assertEquals("TestExtensionImpl", result);
                });
    }

    @Test
    void testExtensionInject_shouldInjectAutomatically() {
        contextRunner
                .withUserConfiguration(TestConfiguration.class)
                .run(context -> {
                    TestService testService = context.getBean(TestService.class);

                    // 验证@ExtensionInject注解的字段已被注入
                    assertNotNull(testService.getExtensionPoint());

                    // 验证注入的是代理对象，可以正常调用
                    String result = testService.callExtension();
                    assertEquals("TestExtensionImpl", result);
                });
    }

    // 测试用的扩展点接口
    public interface TestExtensionPoint {
        String doSomething();
    }

    // 测试用的扩展实现
    @Extension
    static class TestExtensionImpl implements TestExtensionPoint {
        @Override
        public String doSomething() {
            return "TestExtensionImpl";
        }
    }

    // 测试用的服务类
    @Configuration
    static class TestService {
        @ExtensionInject
        private TestExtensionPoint extensionPoint;

        public TestExtensionPoint getExtensionPoint() {
            return extensionPoint;
        }

        public String callExtension() {
            return extensionPoint != null ? extensionPoint.doSomething() : null;
        }

        @Bean
        public TestService testService() {
            return new TestService();
        }
    }

    @Configuration
    static class TestConfiguration {
        @Bean
        public TestExtensionImpl testExtensionImpl() {
            return new TestExtensionImpl();
        }

        @Bean
        public TestService testService() {
            return new TestService();
        }
    }
}
