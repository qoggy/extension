package io.github.qoggy.extension.boot;

import io.github.qoggy.extension.boot.annotation.Extension;
import io.github.qoggy.extension.boot.annotation.ExtensionInject;
import io.github.qoggy.extension.core.ExtensionContext;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
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
            assertTrue(context.containsBean("extensionBeanPostProcessor"));
            assertNotNull(context.getBean(ExtensionContext.class));
            assertNotNull(context.getBean(ExtensionBeanPostProcessor.class));
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

    @Test
    void testExtensionRegistration_shouldRegisterSpringProxiedBean() {
        contextRunner
                .withUserConfiguration(ProxyTestConfiguration.class)
                .run(context -> {
                    ExtensionContext extensionContext = context.getBean(ExtensionContext.class);

                    // 通过Bean名称获取Spring容器中的Bean（可能是代理对象）
                    ProxiedExtensionPoint springBean = context.getBean(ProxiedExtensionPoint.class);

                    // 获取扩展框架中注册的实现
                    ProxiedExtensionPoint registeredExtension = extensionContext.find(ProxiedExtensionPoint.class);
                    assertNotNull(registeredExtension, "扩展框架中应该能找到ProxiedExtensionPoint的实现");

                    // 验证扩展注册：Spring Bean和注册的扩展应该是同一个代理对象
                    assertTrue(AopUtils.isAopProxy(springBean), "Spring Bean应该被代理");
                    assertTrue(AopUtils.isAopProxy(registeredExtension), "注册的扩展实现应该被代理");
                    assertSame(springBean, registeredExtension, "Spring Bean和注册的扩展实现应该是同一个对象");

                    // 验证方法调用正常
                    String springResult = springBean.doSomethingWithTransaction();
                    String extensionResult = registeredExtension.doSomethingWithTransaction();
                    assertEquals(springResult, extensionResult, "通过不同方式调用应该返回相同结果");
                });
    }

    @Test
    void testExtensionInject_shouldInjectIntoSpringProxiedBean() {
        contextRunner
                .withUserConfiguration(ProxyTestConfiguration.class)
                .run(context -> {
                    // 验证@ExtensionInject注入功能
                    ProxiedServiceWithInject serviceWithInject = context.getBean(ProxiedServiceWithInject.class);

                    // 验证服务类被正确代理
                    assertTrue(AopUtils.isAopProxy(serviceWithInject), "带有@Transactional的服务类应该被代理");

                    // 验证注入的扩展是框架代理（这是预期行为）
                    assertNotNull(serviceWithInject.getInjectedExtension(), "@ExtensionInject应该成功注入扩展实现");
                    String injectedExtensionClassName = serviceWithInject.getInjectedExtension().getClass().getName();
                    assertTrue(injectedExtensionClassName.contains("$ByteBuddy"), "注入的扩展应该是代理对象");

                    // 验证注入的扩展功能正常
                    String injectedResult = serviceWithInject.callInjectedExtension();
                    assertEquals("ProxiedExtensionImpl with transaction", injectedResult,
                            "@ExtensionInject注入的扩展应该能正常调用");
                });
    }

    // 测试用的扩展点接口（带事务注解）
    public interface ProxiedExtensionPoint {
        String doSomethingWithTransaction();
    }

    // 测试用的扩展实现（带事务注解，会被Spring代理）
    @Extension
    static class ProxiedExtensionImpl implements ProxiedExtensionPoint {

        @Override
        @org.springframework.transaction.annotation.Transactional
        public String doSomethingWithTransaction() {
            return "ProxiedExtensionImpl with transaction";
        }
    }

    // 测试用的服务类，使用@ExtensionInject注入扩展
    static class ProxiedServiceWithInject {
        @ExtensionInject
        private ProxiedExtensionPoint injectedExtension;

        public ProxiedExtensionPoint getInjectedExtension() {
            return injectedExtension;
        }

        @org.springframework.transaction.annotation.Transactional
        public String callInjectedExtension() {
            return injectedExtension != null ? injectedExtension.doSomethingWithTransaction() : null;
        }
    }

    @Configuration
    @org.springframework.transaction.annotation.EnableTransactionManagement
    static class ProxyTestConfiguration {
        @Bean
        public javax.sql.DataSource dataSource() {
            return new org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder()
                    .setType(org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType.H2)
                    .build();
        }

        // 添加一个简单的事务管理器，让Spring创建代理
        @Bean
        public org.springframework.transaction.PlatformTransactionManager transactionManager(javax.sql.DataSource dataSource) {
            return new org.springframework.jdbc.datasource.DataSourceTransactionManager(dataSource);
        }

        @Bean
        public ProxiedExtensionImpl proxiedExtensionImpl() {
            return new ProxiedExtensionImpl();
        }

        @Bean
        public ProxiedServiceWithInject proxiedServiceWithInject() {
            return new ProxiedServiceWithInject();
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
