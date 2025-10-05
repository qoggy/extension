package io.github.qoggy.extension.core.util;

import java.lang.reflect.Array;
import java.util.*;

/**
 * Helper class for creating toString() representations
 *
 * @author yutianhong
 * @version 1.0
 * @since 2025/10/5 15:02
 */
public class ToStringHelper {
    private final String className;
    private final List<Entry> entries = new ArrayList<>();
    private boolean omitNullValues = false;
    private boolean omitEmptyValues = false;

    private ToStringHelper(String className) {
        this.className = Objects.requireNonNull(className, "Class name must not be null");
    }

    /**
     * Creates a ToStringHelper instance for the given object
     */
    public static ToStringHelper toStringHelper(Object obj) {
        Objects.requireNonNull(obj, "Object must not be null");
        return new ToStringHelper(obj.getClass().getSimpleName());
    }

    /**
     * Creates a ToStringHelper instance for the given class name
     */
    public static ToStringHelper toStringHelper(String className) {
        return new ToStringHelper(className);
    }

    /**
     * Adds a name/value pair to the formatted output
     */
    public ToStringHelper add(String name, Object value) {
        Objects.requireNonNull(name, "Name must not be null");
        entries.add(new Entry(name, value));
        return this;
    }

    /**
     * Configures the helper to ignore properties with null values
     */
    public ToStringHelper omitNullValues() {
        this.omitNullValues = true;
        return this;
    }

    /**
     * Configures the helper to ignore properties with empty values
     */
    public ToStringHelper omitEmptyValues() {
        this.omitEmptyValues = true;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(32).append(className).append('{');
        boolean first = true;

        for (Entry entry : entries) {
            Object value = entry.value;

            // Skip null values if configured
            if (value == null && omitNullValues) {
                continue;
            }

            // Skip empty values if configured
            if (value != null && omitEmptyValues && isEmpty(value)) {
                continue;
            }

            if (!first) {
                builder.append(", ");
            }
            first = false;

            builder.append(entry.name).append('=');

            if (value != null && value.getClass().isArray()) {
                Object[] objectArray = {value};
                String arrayString = Arrays.deepToString(objectArray);
                builder.append(arrayString, 1, arrayString.length() - 1);
            } else {
                builder.append(value);
            }
        }

        return builder.append('}').toString();
    }

    /**
     * Checks if the given value is considered empty
     */
    private static boolean isEmpty(Object value) {
        if (value instanceof CharSequence) {
            return ((CharSequence) value).length() == 0;
        } else if (value instanceof Collection) {
            return ((Collection<?>) value).isEmpty();
        } else if (value instanceof Map) {
            return ((Map<?, ?>) value).isEmpty();
        } else if (value instanceof Optional) {
            return !((Optional<?>) value).isPresent();
        } else if (value instanceof OptionalInt) {
            return !((OptionalInt) value).isPresent();
        } else if (value instanceof OptionalLong) {
            return !((OptionalLong) value).isPresent();
        } else if (value instanceof OptionalDouble) {
            return !((OptionalDouble) value).isPresent();
        } else if (value.getClass().isArray()) {
            return Array.getLength(value) == 0;
        }
        return false;
    }

    /**
     * Internal class to hold name-value pairs
     */
    private static class Entry {
        final String name;
        final Object value;

        Entry(String name, Object value) {
            this.name = name;
            this.value = value;
        }
    }
}
