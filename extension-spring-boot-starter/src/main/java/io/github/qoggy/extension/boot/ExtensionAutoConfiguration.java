package io.github.qoggy.extension.boot;

import io.github.qoggy.extension.core.ExtensionContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Boot扩展框架自动配置类
 *
 * @author yutianhong
 * @version 1.0
 * @since 2025/10/4 00:55
 */
@Configuration
public class ExtensionAutoConfiguration {
    @Bean
    public ExtensionContext extensionContext() {
        return new ExtensionContext();
    }

    @Bean
    public ExtensionInstantiationAwareBeanPostProcessor extensionInstantiationAwareBeanPostProcessor(ExtensionContext extensionContext) {
        return new ExtensionInstantiationAwareBeanPostProcessor(extensionContext);
    }
}
