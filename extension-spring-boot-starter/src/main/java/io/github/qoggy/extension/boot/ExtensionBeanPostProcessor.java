package io.github.qoggy.extension.boot;

import io.github.qoggy.extension.boot.annotation.Extension;
import io.github.qoggy.extension.boot.annotation.ExtensionInject;
import io.github.qoggy.extension.core.ExtensionContext;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;

/**
 * Spring Boot扩展点Bean后处理器，负责：
 * 1. 自动注册标注了@Extension注解的Bean为扩展实现
 * 2. 为标注了@ExtensionInject注解的字段注入扩展实现
 *
 * @author yutianhong
 * @version 1.0
 * @since 2025/10/5 02:40
 */
public class ExtensionBeanPostProcessor implements BeanPostProcessor {

    private final ExtensionContext extensionContext;

    public ExtensionBeanPostProcessor(ExtensionContext extensionContext) {
        this.extensionContext = extensionContext;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        // 处理@ExtensionInject注入
        processExtensionInject(bean);
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        // 检查是否标注了@Extension注解，如果是则注册为扩展实现
        Class<?> targetClass = AopUtils.getTargetClass(bean);
        if (AnnotationUtils.findAnnotation(targetClass, Extension.class) != null) {
            extensionContext.register(bean);
        }
        return bean;
    }

    private void processExtensionInject(Object bean) {
        Class<?> clazz = bean.getClass();

        // 遍历所有字段，查找标注了@ExtensionInject的字段
        ReflectionUtils.doWithFields(clazz, field -> {
            ExtensionInject extensionInject = AnnotationUtils.findAnnotation(field, ExtensionInject.class);
            if (extensionInject != null) {
                injectExtension(bean, field);
            }
        });
    }

    private void injectExtension(Object bean, Field field) {
        try {
            Class<?> fieldType = field.getType();
            Object extension = extensionContext.proxy(fieldType);
            ReflectionUtils.makeAccessible(field);
            ReflectionUtils.setField(field, bean, extension);
        } catch (Throwable e) {
            throw new RuntimeException("Failed to inject extension for field: " + field.getName() +
                    " in class: " + bean.getClass().getName(), e);
        }
    }
}
