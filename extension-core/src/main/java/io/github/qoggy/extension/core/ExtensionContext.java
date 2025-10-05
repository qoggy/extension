package io.github.qoggy.extension.core;

import io.github.qoggy.extension.core.util.CollectionUtils;
import io.github.qoggy.extension.core.util.GenericTypeResolver;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * 扩展点上下文，负责扩展实现的注册、查找
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
     * 注册扩展实现
     *
     * @param extensionImpls 扩展实现数组
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
     * 注册扩展实现实例
     *
     * @param instances 扩展实现实例数组
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
     * 获取所有已注册的扩展实现
     *
     * @return 不可修改的扩展实现列表
     */
    public List<ExtensionImpl> getAll() {
        return Collections.unmodifiableList(extensions);
    }

    /**
     * 查找单个扩展实现（优先级最高的匹配实现）
     *
     * @param extensionPointInterface 扩展点接口
     * @return 匹配的扩展实现，如果没有找到返回null
     */
    public <T> T find(Class<T> extensionPointInterface) {
        List<T> all = findAll(extensionPointInterface);
        return CollectionUtils.isEmpty(all) ? null : all.get(0);
    }

    /**
     * 查找所有匹配的扩展实现，按优先级从高到低排序
     *
     * @param extensionPointInterface 扩展点接口
     * @return 匹配的扩展实现列表，按优先级排序
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
     * 创建扩展点的代理对象
     *
     * @param extensionPointInterface 扩展点接口
     * @param <T>                     扩展点接口类型
     * @return 代理对象
     */
    public <T> T proxy(Class<T> extensionPointInterface) {
        return proxyFactory.createProxy(extensionPointInterface);
    }

    /**
     * 初始化扩展上下文作用域
     *
     * @param scopeContext 作用域上下文对象
     * @return 扩展作用域，使用try-with-resources自动管理
     */
    public ExtensionScope initScope(Object scopeContext) {
        return new ExtensionScope(extensionScopeHolder, scopeContext);
    }

    /**
     * 清空所有缓存
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