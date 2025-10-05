package io.github.qoggy.extension.core.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.Map;

/**
 * 泛型类型解析器，用于解析类的泛型接口类型参数
 *
 * @author yutianhong
 * @version 1.0
 * @since 2025/10/5 00:13
 */
public class GenericTypeResolver {

    /**
     * 解析指定类实现的泛型接口的第一个类型参数
     */
    public static Class<?> resolveTypeArgument(Class<?> clazz, Class<?> genericInterface) {
        return resolveTypeArgument(clazz, genericInterface, 0);
    }

    /**
     * 解析指定类实现的泛型接口的指定索引位置的类型参数
     */
    public static Class<?> resolveTypeArgument(Class<?> clazz, Class<?> genericInterface, int index) {
        Map<TypeVariable<?>, Type> typeVariableMap = new HashMap<>();
        return resolveGenericType(clazz, genericInterface, typeVariableMap, index);
    }

    private static Class<?> resolveGenericType(Class<?> clazz, Class<?> targetInterface, Map<TypeVariable<?>, Type> typeVariableMap, int index) {
        if (clazz == null || clazz == Object.class) {
            return null;
        }

        // 检查直接实现的接口
        Type[] interfaces = clazz.getGenericInterfaces();
        for (Type type : interfaces) {
            Class<?> resolved = resolveGenericType(type, targetInterface, typeVariableMap, index);
            if (resolved != null) {
                return resolved;
            }
        }

        // 检查父类
        Type superclass = clazz.getGenericSuperclass();
        if (superclass != null) {
            // 如果父类是参数化类型，需要记录类型变量映射
            if (superclass instanceof ParameterizedType) {
                ParameterizedType parameterizedSuperclass = (ParameterizedType) superclass;
                Class<?> rawSuperclass = (Class<?>) parameterizedSuperclass.getRawType();
                Map<TypeVariable<?>, Type> newTypeVariableMap = getNewTypeVariableMap(typeVariableMap, parameterizedSuperclass);

                return resolveGenericType(rawSuperclass, targetInterface, newTypeVariableMap, index);
            } else if (superclass instanceof Class) {
                return resolveGenericType((Class<?>) superclass, targetInterface, typeVariableMap, index);
            }
        }

        return null;
    }

    private static Map<TypeVariable<?>, Type> getNewTypeVariableMap(Map<TypeVariable<?>, Type> typeVariableMap, ParameterizedType parameterizedClass) {
        Class<?> rawClass = (Class<?>) parameterizedClass.getRawType();
        TypeVariable<?>[] typeParameters = rawClass.getTypeParameters();
        Type[] actualTypeArguments = parameterizedClass.getActualTypeArguments();

        // 创建新的类型变量映射
        Map<TypeVariable<?>, Type> newTypeVariableMap = new HashMap<>(typeVariableMap);
        for (int i = 0; i < typeParameters.length && i < actualTypeArguments.length; i++) {
            Type actualType = resolveTypeVariable(actualTypeArguments[i], typeVariableMap);
            newTypeVariableMap.put(typeParameters[i], actualType);
        }
        return newTypeVariableMap;
    }

    private static Class<?> resolveGenericType(Type type, Class<?> targetInterface, Map<TypeVariable<?>, Type> typeVariableMap, int index) {
        if (type instanceof ParameterizedType) {
            ParameterizedType paramType = (ParameterizedType) type;
            if (paramType.getRawType().equals(targetInterface)) {
                Type[] typeArgs = paramType.getActualTypeArguments();
                if (index >= 0 && index < typeArgs.length) {
                    Type typeArg = typeArgs[index];
                    Type resolvedType = resolveTypeVariable(typeArg, typeVariableMap);
                    if (resolvedType instanceof Class) {
                        return (Class<?>) resolvedType;
                    } else if (resolvedType instanceof ParameterizedType) {
                        return (Class<?>) ((ParameterizedType) resolvedType).getRawType();
                    }
                }
                return null;
            }
            // 递归检查参数化类型的原始类型
            Map<TypeVariable<?>, Type> newTypeVariableMap = getNewTypeVariableMap(typeVariableMap, paramType);
            return resolveGenericType((Class<?>) paramType.getRawType(), targetInterface, newTypeVariableMap, index);
        } else if (type instanceof Class) {
            Class<?> clazz = (Class<?>) type;
            if (clazz.equals(targetInterface)) {
                return null;
            }
            return resolveGenericType(clazz, targetInterface, typeVariableMap, index);
        }
        return null;
    }

    private static Type resolveTypeVariable(Type type, Map<TypeVariable<?>, Type> typeVariableMap) {
        if (type instanceof TypeVariable) {
            TypeVariable<?> typeVar = (TypeVariable<?>) type;
            Type resolvedType = typeVariableMap.get(typeVar);
            if (resolvedType != null) {
                return resolvedType;
            }
        }
        return type;
    }
}