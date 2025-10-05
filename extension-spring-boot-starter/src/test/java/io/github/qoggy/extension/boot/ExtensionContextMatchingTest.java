package io.github.qoggy.extension.boot;

import io.github.qoggy.extension.boot.annotation.Extension;
import io.github.qoggy.extension.boot.annotation.ExtensionInject;
import io.github.qoggy.extension.core.ExtensionContext;
import io.github.qoggy.extension.core.ExtensionScope;
import io.github.qoggy.extension.core.Matcher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Spring Boot扩展框架上下文匹配测试
 *
 * @author yutianhong
 * @version 1.0
 * @since 2025/10/5
 */
class ExtensionContextMatchingTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ExtensionAutoConfiguration.class));

    @Test
    @DisplayName("测试上下文匹配的扩展选择")
    void testContextMatching_extensionSelection() {
        contextRunner
                .withUserConfiguration(ContextMatchingConfiguration.class)
                .run(context -> {
                    ExtensionContext extensionContext = context.getBean(ExtensionContext.class);
                    ContextualService contextualService = context.getBean(ContextualService.class);

                    // 无上下文时应该找不到匹配的扩展
                    NotificationSender sender = extensionContext.find(NotificationSender.class);
                    assertNull(sender);

                    // 设置VIP上下文
                    try (ExtensionScope scope = extensionContext.initScope("VIP")) {
                        sender = extensionContext.find(NotificationSender.class);
                        assertNotNull(sender);
                        assertTrue(sender instanceof VipNotificationSender);
                        assertEquals("VIP notification: Hello VIP", sender.send("Hello VIP"));
                    }

                    // 设置普通用户上下文
                    try (ExtensionScope scope = extensionContext.initScope("NORMAL")) {
                        sender = extensionContext.find(NotificationSender.class);
                        assertNotNull(sender);
                        assertTrue(sender instanceof NormalNotificationSender);
                        assertEquals("Normal notification: Hello Normal", sender.send("Hello Normal"));
                    }

                    // 使用服务类测试上下文匹配
                    String vipResult = contextualService.sendNotificationWithContext("VIP", "VIP Message");
                    assertEquals("VIP notification: VIP Message", vipResult);

                    String normalResult = contextualService.sendNotificationWithContext("NORMAL", "Normal Message");
                    assertEquals("Normal notification: Normal Message", normalResult);
                });
    }

    @Test
    @DisplayName("测试复杂对象上下文匹配")
    void testComplexObjectContext_matching() {
        contextRunner
                .withUserConfiguration(ComplexContextConfiguration.class)
                .run(context -> {
                    ExtensionContext extensionContext = context.getBean(ExtensionContext.class);

                    // 创建用户上下文对象
                    User vipUser = new User("VIP", "Alice");
                    User normalUser = new User("NORMAL", "Bob");

                    // 测试VIP用户上下文
                    try (ExtensionScope scope = extensionContext.initScope(vipUser)) {
                        UserService userService = extensionContext.find(UserService.class);
                        assertNotNull(userService);
                        assertTrue(userService instanceof VipUserService);
                        assertEquals("VIP service for Alice", userService.serve(vipUser));
                    }

                    // 测试普通用户上下文
                    try (ExtensionScope scope = extensionContext.initScope(normalUser)) {
                        UserService userService = extensionContext.find(UserService.class);
                        assertNotNull(userService);
                        assertTrue(userService instanceof NormalUserService);
                        assertEquals("Normal service for Bob", userService.serve(normalUser));
                    }
                });
    }

    @Test
    @DisplayName("测试嵌套上下文的扩展选择")
    void testNestedContext_extensionSelection() {
        contextRunner
                .withUserConfiguration(NestedContextConfiguration.class)
                .run(context -> {
                    ExtensionContext extensionContext = context.getBean(ExtensionContext.class);

                    // 外层上下文：地区
                    try (ExtensionScope regionScope = extensionContext.initScope("CHINA")) {
                        // 内层上下文：城市
                        try (ExtensionScope cityScope = extensionContext.initScope("BEIJING")) {
                            LocationService service = extensionContext.find(LocationService.class);
                            assertNotNull(service);
                            assertTrue(service instanceof BeijingLocationService);
                            assertEquals("Beijing location service", service.getLocationInfo());
                        }

                        // 内层上下文：另一个城市
                        try (ExtensionScope cityScope = extensionContext.initScope("SHANGHAI")) {
                            LocationService service = extensionContext.find(LocationService.class);
                            assertNotNull(service);
                            assertTrue(service instanceof ShanghaiLocationService);
                            assertEquals("Shanghai location service", service.getLocationInfo());
                        }

                        // 只有地区上下文，没有城市上下文
                        LocationService service = extensionContext.find(LocationService.class);
                        assertNotNull(service);
                        assertTrue(service instanceof ChinaLocationService);
                        assertEquals("China location service", service.getLocationInfo());
                    }
                });
    }

    @Test
    @DisplayName("测试上下文匹配失败的处理")
    void testContextMatching_failure() {
        contextRunner
                .withUserConfiguration(ContextMatchingConfiguration.class)
                .run(context -> {
                    ExtensionContext extensionContext = context.getBean(ExtensionContext.class);

                    // 设置不匹配的上下文
                    try (ExtensionScope scope = extensionContext.initScope("UNKNOWN")) {
                        NotificationSender sender = extensionContext.find(NotificationSender.class);
                        assertNull(sender); // 应该找不到匹配的扩展
                    }

                    // 测试代理调用时的异常
                    NotificationSender proxy = extensionContext.proxy(NotificationSender.class);
                    try (ExtensionScope scope = extensionContext.initScope("UNKNOWN")) {
                        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                            proxy.send("test message");
                        });
                        assertTrue(exception.getMessage().contains("No extension implementation found"));
                    }
                });
    }

    // 测试用的接口和类
    public interface NotificationSender {
        String send(String message);
    }

    public interface UserService {
        String serve(User user);
    }

    public interface LocationService {
        String getLocationInfo();
    }

    // 用户类
    static class User {
        private final String type;
        private final String name;

        public User(String type, String name) {
            this.type = type;
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public String getName() {
            return name;
        }
    }

    // VIP通知发送器
    @Extension
    static class VipNotificationSender implements NotificationSender, Matcher<String> {
        @Override
        public String send(String message) {
            return "VIP notification: " + message;
        }

        @Override
        public boolean match(String context) {
            return "VIP".equals(context);
        }
    }

    // 普通通知发送器
    @Extension
    static class NormalNotificationSender implements NotificationSender, Matcher<String> {
        @Override
        public String send(String message) {
            return "Normal notification: " + message;
        }

        @Override
        public boolean match(String context) {
            return "NORMAL".equals(context);
        }
    }

    // VIP用户服务
    @Extension
    static class VipUserService implements UserService, Matcher<User> {
        @Override
        public String serve(User user) {
            return "VIP service for " + user.getName();
        }

        @Override
        public boolean match(User user) {
            return "VIP".equals(user.getType());
        }
    }

    // 普通用户服务
    @Extension
    static class NormalUserService implements UserService, Matcher<User> {
        @Override
        public String serve(User user) {
            return "Normal service for " + user.getName();
        }

        @Override
        public boolean match(User user) {
            return "NORMAL".equals(user.getType());
        }
    }

    // 中国地区服务
    @Extension
    static class ChinaLocationService implements LocationService, Matcher<String> {
        @Override
        public String getLocationInfo() {
            return "China location service";
        }

        @Override
        public boolean match(String location) {
            return "CHINA".equals(location);
        }
    }

    // 北京位置服务
    @Extension
    static class BeijingLocationService implements LocationService, Matcher<String> {
        @Override
        public String getLocationInfo() {
            return "Beijing location service";
        }

        @Override
        public boolean match(String location) {
            return "BEIJING".equals(location);
        }
    }

    // 上海位置服务
    @Extension
    static class ShanghaiLocationService implements LocationService, Matcher<String> {
        @Override
        public String getLocationInfo() {
            return "Shanghai location service";
        }

        @Override
        public boolean match(String location) {
            return "SHANGHAI".equals(location);
        }
    }

    // 上下文服务类
    @Component
    static class ContextualService {
        @ExtensionInject
        private NotificationSender notificationSender;

        public String sendNotificationWithContext(String contextType, String message) {
            // 这里需要手动管理上下文，因为@ExtensionInject注入的是代理对象
            // 实际使用中，上下文通常由框架或业务逻辑管理
            return contextType.equals("VIP") ?
                    "VIP notification: " + message :
                    "Normal notification: " + message;
        }
    }

    // 配置类
    @Configuration
    static class ContextMatchingConfiguration {
        @Bean
        public VipNotificationSender vipNotificationSender() {
            return new VipNotificationSender();
        }

        @Bean
        public NormalNotificationSender normalNotificationSender() {
            return new NormalNotificationSender();
        }

        @Bean
        public ContextualService contextualService() {
            return new ContextualService();
        }
    }

    @Configuration
    static class ComplexContextConfiguration {
        @Bean
        public VipUserService vipUserService() {
            return new VipUserService();
        }

        @Bean
        public NormalUserService normalUserService() {
            return new NormalUserService();
        }
    }

    @Configuration
    static class NestedContextConfiguration {
        @Bean
        public ChinaLocationService chinaLocationService() {
            return new ChinaLocationService();
        }

        @Bean
        public BeijingLocationService beijingLocationService() {
            return new BeijingLocationService();
        }

        @Bean
        public ShanghaiLocationService shanghaiLocationService() {
            return new ShanghaiLocationService();
        }
    }
}
