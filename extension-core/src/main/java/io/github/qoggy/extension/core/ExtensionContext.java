package io.github.qoggy.extension.core;

import io.github.qoggy.extension.core.util.CollectionUtils;
import io.github.qoggy.extension.core.util.GenericTypeResolver;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Extension context that manages registration and discovery of extension implementations.
 * Provides centralized management for extensions with support for priority-based selection,
 * context-aware matching, and proxy creation.
 *
 * @author yutianhong
 * @version 1.0
 * @since 2025/10/4 22:16
 */
public final class ExtensionContext {
    private static final Logger logger = Logger.getLogger(ExtensionContext.class.getName());

    private final ExtensionScopeHolder extensionScopeHolder = new ExtensionScopeHolder();
    private final List<ExtensionImpl> extensions = new ArrayList<>();
    private final ExtensionProxyFactory proxyFactory = new ExtensionProxyFactory(this);

    private final Map<Class<?>, List<ExtensionImpl>> extensionCache = new ConcurrentHashMap<>();

    /**
     * Registers extension implementations into the context.
     *
     * @param extensionImpls Array of extension implementations to register
     */
    public void register(ExtensionImpl... extensionImpls) {
        if (extensionImpls == null || extensionImpls.length == 0) {
            return;
        }

        extensions.addAll(Arrays.asList(extensionImpls));
        // 清空缓存，因为新注册的扩展可能影响查找结果
        clearCache();

        logger.fine("Successfully registered " + extensionImpls.length +
                " extension implementations, total extensions: " + extensions.size());
    }

    /**
     * Registers extension implementation instances into the context.
     * Automatically wraps instances into ExtensionImpl objects.
     *
     * @param instances Array of extension implementation instances to register
     */
    public void register(Object... instances) {
        if (instances == null || instances.length == 0) {
            return;
        }

        ExtensionImpl[] impls = Arrays.stream(instances)
                .filter(Objects::nonNull)
                .map(e -> ExtensionImpl.builder(e).build())
                .toArray(ExtensionImpl[]::new);
        register(impls);
    }

    /**
     * Returns all registered extension implementations.
     *
     * @return Unmodifiable list of all registered extension implementations
     */
    public List<ExtensionImpl> getAll() {
        return Collections.unmodifiableList(extensions);
    }

    /**
     * Finds a single extension implementation with the highest priority that matches the current context.
     *
     * @param extensionPointInterface The extension point interface to find implementation for
     * @return The matching extension implementation, or null if none found
     */
    public <T> T find(Class<T> extensionPointInterface) {
        List<T> all = findAll(extensionPointInterface);
        return CollectionUtils.isEmpty(all) ? null : all.get(0);
    }

    /**
     * Finds all extension implementations that match the current context, sorted by priority (highest first).
     *
     * @param extensionPointInterface The extension point interface to find implementations for
     * @return List of matching extension implementations, sorted by priority
     */
    public <T> List<T> findAll(Class<T> extensionPointInterface) {
        Objects.requireNonNull(extensionPointInterface, "Extension point interface must not be null");

        // 获取匹配的扩展实现（使用缓存）
        List<ExtensionImpl> matchingExtensions = getMatchingExtensions(extensionPointInterface);

        // 过滤上下文匹配的实现并转换类型
        return matchingExtensions.stream()
                .filter(this::matchesCurrentContext)
                .map(e -> (T) e.getInstance())
                .collect(Collectors.toList());
    }

    /**
     * Creates a proxy object for the extension point interface that automatically routes method calls
     * to the appropriate extension implementation based on the current context.
     *
     * @param extensionPointInterface The extension point interface to create proxy for
     * @param <T>                     The extension point interface type
     * @return Proxy object that delegates to matching extension implementations
     */
    public <T> T proxy(Class<T> extensionPointInterface) {
        return proxyFactory.createProxy(extensionPointInterface);
    }

    /**
     * Initializes an extension scope with the given context object.
     * The scope should be used with try-with-resources for automatic cleanup.
     *
     * @param scopeContext The context object to be used for extension matching
     * @return Extension scope that manages the context lifecycle
     */
    public ExtensionScope initScope(Object scopeContext) {
        return new ExtensionScope(extensionScopeHolder, scopeContext);
    }

    /**
     * Clears all internal caches.
     */
    void clearCache() {
        extensionCache.clear();
    }

    /**
     * 获取匹配指定扩展点接口的扩展实现（使用缓存）
     */
    private List<ExtensionImpl> getMatchingExtensions(Class<?> extensionPointInterface) {
        return extensionCache.computeIfAbsent(extensionPointInterface, (epi) -> extensions.stream()
                .filter(e -> e.isAssignableTo(epi))
                .sorted(Comparator.comparingInt(e -> e.getPriority().getPriority()))
                .collect(Collectors.toList())
        );
    }

    /**
     * 检查扩展实现是否匹配当前上下文
     */
    private boolean matchesCurrentContext(ExtensionImpl extensionImpl) {
        try {
            return extensionImpl.getMatcher().match(getCurrentContext(extensionImpl));
        } catch (Throwable e) {
            String errorMsg = "Failed to match extension: " + extensionImpl.getInstance().getClass().getName();
            logger.log(Level.SEVERE, errorMsg, e);
            throw new RuntimeException(errorMsg, e);
        }
    }

    /**
     * 获取当前上下文对象
     */
    private <T> T getCurrentContext(ExtensionImpl extensionImpl) {
        Class<?> type = GenericTypeResolver.resolveTypeArgument(
                extensionImpl.getMatcher().getClass(), Matcher.class);
        if (type == null) {
            type = Object.class;
        }
        return (T) extensionScopeHolder.getContext(type);
    }
}