package com.rick.common.util;

import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @author Rick
 * @createdAt 2021-10-27 09:48:00
 */
public abstract class Assert {

    public Assert() {
    }

    public static void state(boolean expression, String message) {
        if (!expression) {
            throw new IllegalStateException(message);
        }
    }

    public static void state(boolean expression, Supplier<String> messageSupplier) {
        if (!expression) {
            throw new IllegalStateException(nullSafeGet(messageSupplier));
        }
    }

    public static void isTrue(boolean expression, String message) {
        if (!expression) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void isTrue(boolean expression, Supplier<String> messageSupplier) {
        if (!expression) {
            throw new IllegalArgumentException(nullSafeGet(messageSupplier));
        }
    }

    public static void isNull(Object object, String message) {
        if (object != null) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void isNull(Object object, Supplier<String> messageSupplier) {
        if (object != null) {
            throw new IllegalArgumentException(nullSafeGet(messageSupplier));
        }
    }

    public static void notNull(Object object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void notNull(Object object, Supplier<String> messageSupplier) {
        if (object == null) {
            throw new IllegalArgumentException(nullSafeGet(messageSupplier));
        }
    }

    public static void hasLength(String text, String message) {
        if (!org.springframework.util.StringUtils.hasLength(text)) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void hasLength(String text, Supplier<String> messageSupplier) {
        if (!org.springframework.util.StringUtils.hasLength(text)) {
            throw new IllegalArgumentException(nullSafeGet(messageSupplier));
        }
    }

    public static void hasText(String text, String message) {
        if (!org.springframework.util.StringUtils.hasText(text)) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void hasText(String text, Supplier<String> messageSupplier) {
        if (!org.springframework.util.StringUtils.hasText(text)) {
            throw new IllegalArgumentException(nullSafeGet(messageSupplier));
        }
    }

    public static void doesNotContain(String textToSearch, String substring, String message) {
        if (org.springframework.util.StringUtils.hasLength(textToSearch) && org.springframework.util.StringUtils.hasLength(substring) && textToSearch.contains(substring)) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void doesNotContain(String textToSearch, String substring, Supplier<String> messageSupplier) {
        if (org.springframework.util.StringUtils.hasLength(textToSearch) && org.springframework.util.StringUtils.hasLength(substring) && textToSearch.contains(substring)) {
            throw new IllegalArgumentException(nullSafeGet(messageSupplier));
        }
    }

    public static void notEmpty(Object[] array, String message) {
        if (ObjectUtils.isEmpty(array)) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void notEmpty(Object[] array, Supplier<String> messageSupplier) {
        if (ObjectUtils.isEmpty(array)) {
            throw new IllegalArgumentException(nullSafeGet(messageSupplier));
        }
    }

    public static void noNullElements(Object[] array, String message) {
        if (array != null) {
            Object[] var2 = array;
            int var3 = array.length;

            for(int var4 = 0; var4 < var3; ++var4) {
                Object element = var2[var4];
                if (element == null) {
                    throw new IllegalArgumentException(message);
                }
            }
        }

    }

    public static void noNullElements(Object[] array, Supplier<String> messageSupplier) {
        if (array != null) {
            Object[] var2 = array;
            int var3 = array.length;

            for(int var4 = 0; var4 < var3; ++var4) {
                Object element = var2[var4];
                if (element == null) {
                    throw new IllegalArgumentException(nullSafeGet(messageSupplier));
                }
            }
        }

    }

    public static void notEmpty(Collection<?> collection, String message) {
        if (CollectionUtils.isEmpty(collection)) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void notEmpty(Collection<?> collection, Supplier<String> messageSupplier) {
        if (CollectionUtils.isEmpty(collection)) {
            throw new IllegalArgumentException(nullSafeGet(messageSupplier));
        }
    }

    public static void noNullElements(Collection<?> collection, String message) {
        if (collection != null) {
            Iterator var2 = collection.iterator();

            while(var2.hasNext()) {
                Object element = var2.next();
                if (element == null) {
                    throw new IllegalArgumentException(message);
                }
            }
        }

    }

    public static void noNullElements(Collection<?> collection, Supplier<String> messageSupplier) {
        if (collection != null) {
            Iterator var2 = collection.iterator();

            while(var2.hasNext()) {
                Object element = var2.next();
                if (element == null) {
                    throw new IllegalArgumentException(nullSafeGet(messageSupplier));
                }
            }
        }

    }

    public static void notEmpty(Map<?, ?> map, String message) {
        if (CollectionUtils.isEmpty(map)) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void notEmpty(Map<?, ?> map, Supplier<String> messageSupplier) {
        if (CollectionUtils.isEmpty(map)) {
            throw new IllegalArgumentException(nullSafeGet(messageSupplier));
        }
    }

    public static void isInstanceOf(Class<?> type, Object obj, String message) {
        notNull(type, (String)"Type to check against must not be null");
        if (!type.isInstance(obj)) {
            instanceCheckFailed(type, obj, message);
        }

    }

    public static void isInstanceOf(Class<?> type, Object obj, Supplier<String> messageSupplier) {
        notNull(type, (String)"Type to check against must not be null");
        if (!type.isInstance(obj)) {
            instanceCheckFailed(type, obj, nullSafeGet(messageSupplier));
        }

    }

    public static void isInstanceOf(Class<?> type, Object obj) {
        isInstanceOf(type, obj, "");
    }

    public static void isAssignable(Class<?> superType, Class<?> subType, String message) {
        notNull(superType, (String)"Super type to check against must not be null");
        if (subType == null || !superType.isAssignableFrom(subType)) {
            assignableCheckFailed(superType, subType, message);
        }

    }

    public static void isAssignable(Class<?> superType, Class<?> subType, Supplier<String> messageSupplier) {
        notNull(superType, (String)"Super type to check against must not be null");
        if (subType == null || !superType.isAssignableFrom(subType)) {
            assignableCheckFailed(superType, subType, nullSafeGet(messageSupplier));
        }

    }

    public static void isAssignable(Class<?> superType, Class<?> subType) {
        isAssignable(superType, subType, "");
    }

    private static void instanceCheckFailed(Class<?> type, Object obj, String msg) {
        String className = obj != null ? obj.getClass().getName() : "null";
        String result = "";
        boolean defaultMessage = true;
        if (org.springframework.util.StringUtils.hasLength(msg)) {
            if (endsWithSeparator(msg)) {
                result = msg + " ";
            } else {
                result = messageWithTypeName(msg, className);
                defaultMessage = false;
            }
        }

        if (defaultMessage) {
            result = result + "Object of class [" + className + "] must be an instance of " + type;
        }

        throw new IllegalArgumentException(result);
    }

    private static void assignableCheckFailed(Class<?> superType, Class<?> subType, String msg) {
        String result = "";
        boolean defaultMessage = true;
        if (StringUtils.hasLength(msg)) {
            if (endsWithSeparator(msg)) {
                result = msg + " ";
            } else {
                result = messageWithTypeName(msg, subType);
                defaultMessage = false;
            }
        }

        if (defaultMessage) {
            result = result + subType + " is not assignable to " + superType;
        }

        throw new IllegalArgumentException(result);
    }

    private static boolean endsWithSeparator(String msg) {
        return msg.endsWith(":") || msg.endsWith(";") || msg.endsWith(",") || msg.endsWith(".");
    }

    private static String messageWithTypeName(String msg, Object typeName) {
        return msg + (msg.endsWith(" ") ? "" : ": ") + typeName;
    }

    private static String nullSafeGet(Supplier<String> messageSupplier) {
        return messageSupplier != null ? (String)messageSupplier.get() : null;
    }

}
