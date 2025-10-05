package io.github.qoggy.extension.boot;

import io.github.qoggy.extension.boot.annotation.Extension;
import io.github.qoggy.extension.boot.annotation.ExtensionInject;
import io.github.qoggy.extension.core.ExtensionContext;
import io.github.qoggy.extension.core.Priority;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Spring Boot扩展框架集成测试 - 增强版
 *
 * @author yutianhong
 * @version 1.0
 * @since 2025/10/5
 */
class ExtensionIntegrationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ExtensionAutoConfiguration.class));


    @Test
    @DisplayName("测试多个扩展实现的优先级排序")
    void testMultipleExtensions_priorityOrdering() {
        contextRunner
                .withUserConfiguration(MultipleExtensionsConfiguration.class)
                .run(context -> {
                    ExtensionContext extensionContext = context.getBean(ExtensionContext.class);

                    // 验证返回优先级最高的实现
                    PaymentProcessor processor = extensionContext.find(PaymentProcessor.class);
                    assertNotNull(processor);
                    assertTrue(processor instanceof WechatProcessor); // 优先级更高

                    // 验证所有实现都被注册了
                    List<PaymentProcessor> allProcessors = extensionContext.findAll(PaymentProcessor.class);
                    assertEquals(2, allProcessors.size());
                });
    }


    @Test
    @DisplayName("测试@ExtensionInject注入不存在的扩展")
    void testExtensionInject_noImplementation() {
        contextRunner
                .withUserConfiguration(NoImplementationConfiguration.class)
                .run(context -> {
                    OrderService orderService = context.getBean(OrderService.class);

                    // 验证字段已被注入代理对象
                    assertNotNull(orderService.getOrderProcessor());

                    // 调用代理方法应该抛出异常（没有实现）
                    RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                        orderService.processOrder("ORDER-123");
                    });

                    assertTrue(exception.getMessage().contains("No extension implementation found"));
                });
    }

    @Test
    @DisplayName("测试继承层次的扩展注册")
    void testInheritanceHierarchy_extensionRegistration() {
        contextRunner
                .withUserConfiguration(InheritanceConfiguration.class)
                .run(context -> {
                    ExtensionContext extensionContext = context.getBean(ExtensionContext.class);

                    // 验证子类实现可以被父接口找到
                    PaymentProcessor processor = extensionContext.find(PaymentProcessor.class);
                    assertNotNull(processor);
                    assertTrue(processor instanceof AdvancedPaymentProcessor);

                    // 验证子接口也能找到实现
                    AdvancedPaymentProcessor advancedProcessor = extensionContext.find(AdvancedPaymentProcessor.class);
                    assertNotNull(advancedProcessor);
                    assertEquals("Advanced payment processed: 200.0", advancedProcessor.processAdvancedPayment(200.0));
                });
    }

    @Test
    @DisplayName("测试Component和Extension注解组合")
    void testComponentAndExtension_combination() {
        contextRunner
                .withUserConfiguration(ComponentExtensionConfiguration.class)
                .run(context -> {
                    // 验证Bean可以通过@Component注入
                    assertTrue(context.containsBean("bankProcessor"));
                    BankProcessor bankProcessor = context.getBean(BankProcessor.class);
                    assertNotNull(bankProcessor);

                    // 验证同时也被注册为扩展实现
                    ExtensionContext extensionContext = context.getBean(ExtensionContext.class);
                    PaymentProcessor processor = extensionContext.find(PaymentProcessor.class);
                    assertNotNull(processor);
                    assertSame(bankProcessor, processor);
                });
    }

    @Test
    @DisplayName("测试私有字段的扩展注入")
    void testPrivateField_extensionInject() {
        contextRunner
                .withUserConfiguration(PrivateFieldConfiguration.class)
                .run(context -> {
                    PrivateFieldService service = context.getBean(PrivateFieldService.class);

                    // 验证私有字段也能被正确注入
                    String result = service.doPayment(75.0);
                    assertEquals("Alipay processed 75.0", result);
                });
    }

    @Test
    @DisplayName("测试扩展注入异常处理")
    void testExtensionInject_exceptionHandling() {
        // 测试不合适的字段类型应该导致应用启动失败
        contextRunner
                .withUserConfiguration(ExceptionConfiguration.class)
                .run(context -> {
                    // 应该启动失败，因为String类型不能作为扩展点
                    assertThrows(RuntimeException.class, context::isRunning);
                });
    }

    // 测试用的扩展点接口
    public interface PaymentProcessor {
        String processPayment(double amount);
    }

    // 高级支付处理器接口（继承PaymentProcessor）
    public interface AdvancedPaymentProcessor extends PaymentProcessor {
        String processAdvancedPayment(double amount);
    }

    // 订单处理器接口
    public interface OrderProcessor {
        String processOrder(String orderId);
    }

    // 支付宝处理器实现
    @Extension
    static class AlipayProcessor implements PaymentProcessor {
        @Override
        public String processPayment(double amount) {
            return "Alipay processed " + amount;
        }
    }

    // 微信支付处理器实现（高优先级）
    @Extension
    static class WechatProcessor implements PaymentProcessor, Priority {
        @Override
        public String processPayment(double amount) {
            return "Wechat processed " + amount;
        }

        @Override
        public int getPriority() {
            return -1; // 更高优先级
        }
    }

    // 银行处理器实现
    @Component("bankProcessor")
    @Extension
    static class BankProcessor implements PaymentProcessor {
        @Override
        public String processPayment(double amount) {
            return "Bank processed " + amount;
        }
    }

    // 高级支付处理器实现
    @Extension
    static class AdvancedPaymentProcessorImpl implements AdvancedPaymentProcessor {
        @Override
        public String processPayment(double amount) {
            return "Basic payment processed: " + amount;
        }

        @Override
        public String processAdvancedPayment(double amount) {
            return "Advanced payment processed: " + amount;
        }
    }

    // 支付服务类
    @Component
    static class PaymentService {
        @ExtensionInject
        private PaymentProcessor paymentProcessor;

        public PaymentProcessor getPaymentProcessor() {
            return paymentProcessor;
        }

        public String processPayment(double amount) {
            return paymentProcessor.processPayment(amount);
        }
    }

    // 订单服务类（注入不存在的扩展）
    @Component
    static class OrderService {
        @ExtensionInject
        private OrderProcessor orderProcessor;

        public OrderProcessor getOrderProcessor() {
            return orderProcessor;
        }

        public String processOrder(String orderId) {
            return orderProcessor.processOrder(orderId);
        }
    }

    // 私有字段服务类
    @Component
    static class PrivateFieldService {
        @ExtensionInject
        private PaymentProcessor processor;

        public String doPayment(double amount) {
            return processor.processPayment(amount);
        }
    }

    // 异常处理服务类
    @Component
    static class ExceptionService {
        @ExtensionInject
        private PaymentProcessor paymentProcessor;

        @ExtensionInject
        private String invalidField; // 错误的字段类型，应该被忽略或处理

        public PaymentProcessor getPaymentProcessor() {
            return paymentProcessor;
        }
    }


    @Configuration
    static class MultipleExtensionsConfiguration {
        @Bean
        public AlipayProcessor alipayProcessor() {
            return new AlipayProcessor();
        }

        @Bean
        public WechatProcessor wechatProcessor() {
            return new WechatProcessor();
        }
    }


    @Configuration
    static class NoImplementationConfiguration {
        @Bean
        public OrderService orderService() {
            return new OrderService();
        }
    }

    @Configuration
    static class InheritanceConfiguration {
        @Bean
        public AdvancedPaymentProcessorImpl advancedPaymentProcessor() {
            return new AdvancedPaymentProcessorImpl();
        }
    }

    @Configuration
    static class ComponentExtensionConfiguration {
        @Bean
        public BankProcessor bankProcessor() {
            return new BankProcessor();
        }
    }

    @Configuration
    static class PrivateFieldConfiguration {
        @Bean
        public AlipayProcessor alipayProcessor() {
            return new AlipayProcessor();
        }

        @Bean
        public PrivateFieldService privateFieldService() {
            return new PrivateFieldService();
        }
    }

    @Configuration
    static class ExceptionConfiguration {
        @Bean
        public AlipayProcessor alipayProcessor() {
            return new AlipayProcessor();
        }

        @Bean
        public ExceptionService exceptionService() {
            return new ExceptionService();
        }
    }
}
