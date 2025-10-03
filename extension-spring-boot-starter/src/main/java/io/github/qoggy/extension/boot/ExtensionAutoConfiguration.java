package io.github.qoggy.extension.boot;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author yutianhong
 * @version 1.0
 * @since 2025/10/4 00:55
 */
@Configuration
@EnableConfigurationProperties(ExtensionProperties.class)
public class ExtensionAutoConfiguration {
}
