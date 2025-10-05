package io.github.qoggy.extension.core.util;

import java.util.Collection;

/**
 * @author yutianhong
 * @version 1.0
 * @since 2025/10/4 22:26
 */
public class CollectionUtils {
    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    public static boolean isNotEmpty(Collection<?> collection) {
        return !isEmpty(collection);
    }

    private CollectionUtils() {
    }
}
