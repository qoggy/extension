package io.github.qoggy.extension.core;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 扩展点代理工厂，负责创建扩展点接口的代理对象
 *
 * @author yutianhong
 * @version 1.0
 * @since 2025/10/5 16:32
 */
class ExtensionProxyFactory {
    private static final Logger logger = Logger.getLogger(ExtensionProxyFactory.class.getName());

    private final ExtensionContext extensionContext;
    private final ConcurrentHashMap<Class<?>, Object> proxyCache = new ConcurrentHashMap<>();

    public ExtensionProxyFactory(ExtensionContext extensionContext) {
        this.extensionContext = extensionContext;
    }

    /**
     * 创建扩展点接口的代理对象
     *
     * @param extensionPointInterface 扩展点接口
     * @param <T>                     扩展点接口类型
     * @return 代理对象
     */
    @SuppressWarnings("unchecked")
    public <T> T createProxy(Class<T> extensionPointInterface) {
        T proxy = (T) proxyCache.computeIfAbsent(extensionPointInterface, this::doCreateProxy);
        logger.fine("Proxy created successfully for interface: " + extensionPointInterface.getName());
        return proxy;
    }

    /**
     * 实际创建代理对象的方法
     */
    private <T> T doCreateProxy(Class<T> extensionPointInterface) {
        // 校验接口类型的合法性
        if (extensionPointInterface == null) {
            throw new IllegalArgumentException("Extension point interface cannot be null");
        }
        if (!extensionPointInterface.isInterface()) {
            throw new IllegalArgumentException("Extension point must be an interface, but got: " + extensionPointInterface.getName());
        }
        if (extensionPointInterface.isPrimitive() || extensionPointInterface.isArray() ||
                (extensionPointInterface.getModifiers() & java.lang.reflect.Modifier.FINAL) != 0) {
            throw new IllegalArgumentException("Cannot subclass primitive, array or final types: " + extensionPointInterface.getName());
        }
        if (extensionPointInterface.isAnnotation()) {
            throw new IllegalArgumentException("Cannot create proxy for annotation type: " + extensionPointInterface.getName());
        }
        if (extensionPointInterface.isEnum()) {
            throw new IllegalArgumentException("Cannot create proxy for enum type: " + extensionPointInterface.getName());
        }
        // 检查是否是系统内置接口，避免代理系统接口
        String packageName = extensionPointInterface.getPackage() != null ? extensionPointInterface.getPackage().getName() : "";
        if (packageName.startsWith("java.") || packageName.startsWith("javax.") || packageName.startsWith("sun.")) {
            throw new IllegalArgumentException("Cannot create proxy for system interface: " + extensionPointInterface.getName());
        }

        try {
            return new ByteBuddy()
                    .subclass(extensionPointInterface)
                    .method(ElementMatchers.not(ElementMatchers.isDeclaredBy(Object.class)))
                    .intercept(MethodDelegation.to(new ExtensionMethodInterceptor(extensionContext, extensionPointInterface)))
                    .make()
                    .load(getClass().getClassLoader())
                    .getLoaded()
                    .newInstance();
        } catch (Throwable e) {
            String errorMsg = "Failed to create proxy instance for extension point interface: " + extensionPointInterface.getName();
            logger.log(Level.SEVERE, errorMsg, e);
            throw new RuntimeException(errorMsg, e);
        }
    }

    public void clearCache() {
        proxyCache.clear();
    }

    /**
     * 扩展点方法拦截器，负责将代理方法调用转发到实际的扩展实现
     */
    public static class ExtensionMethodInterceptor {
        private final ExtensionContext extensionContext;
        private final Class<?> extensionPointInterface;

        ExtensionMethodInterceptor(ExtensionContext extensionContext, Class<?> extensionPointInterface) {
            this.extensionContext = extensionContext;
            this.extensionPointInterface = extensionPointInterface;
        }

        @RuntimeType
        public Object intercept(@Origin Method method, @AllArguments Object[] allArguments) {
            logger.fine("Intercepting method call: " + extensionPointInterface.getName() + "." + method.getName());

            Object candidate = extensionContext.find(extensionPointInterface);
            if (candidate == null) {
                String errorMsg = "No extension implementation found for interface: " + extensionPointInterface.getName() +
                        ". Please ensure that at least one implementation is registered and matches the current context.";
                logger.severe(errorMsg);
                throw new RuntimeException(errorMsg);
            }

            logger.fine("Found extension implementation: " + candidate.getClass().getName() +
                    " for interface: " + extensionPointInterface.getName());

            try {
                Object result = method.invoke(candidate, allArguments);
                logger.fine("Method " + method.getName() + " executed successfully");
                return result;
            } catch (IllegalAccessException e) {
                String errorMsg = "Failed to invoke method '" + method.getName() + "' on extension implementation: " +
                        candidate.getClass().getName() + ". The method may not be accessible.";
                logger.log(Level.SEVERE, errorMsg, e);
                throw new RuntimeException(errorMsg, e);
            } catch (InvocationTargetException e) {
                String errorMsg = "Extension method '" + method.getName() + "' threw an exception in implementation: " +
                        candidate.getClass().getName();
                logger.log(Level.SEVERE, errorMsg, e);

                Throwable cause = e.getCause();
                if (cause instanceof RuntimeException) {
                    throw (RuntimeException) cause;
                } else if (cause instanceof Error) {
                    throw (Error) cause;
                } else {
                    throw new RuntimeException(errorMsg, cause);
                }
            }
        }
    }
}
