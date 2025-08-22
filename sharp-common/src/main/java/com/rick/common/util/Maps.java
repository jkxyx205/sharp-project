package com.rick.common.util;

import java.util.*;

public final class Maps {

    private Maps() {}

    @SafeVarargs
    public static <K, V> Map<K, V> ofEntries(Map.Entry<? extends K, ? extends V>... entries) {
        Map<K, V> map = new LinkedHashMap<>();
        for (Map.Entry<? extends K, ? extends V> e : entries) {
            Objects.requireNonNull(e.getKey(), "null keys are not allowed");
            Objects.requireNonNull(e.getValue(), "null values are not allowed");
            if (map.put(e.getKey(), e.getValue()) != null) {
                throw new IllegalArgumentException("duplicate key: " + e.getKey());
            }
        }
        return Collections.unmodifiableMap(map);
    }

    public static <K, V> Map<K, V> of() {
        return Collections.emptyMap();
    }

    public static <K, V> Map<K, V> of(K k1, V v1) {
        return Collections.singletonMap(
                Objects.requireNonNull(k1),
                Objects.requireNonNull(v1)
        );
    }

    public static <K, V> Map<K, V> of(K k1, V v1, K k2, V v2) {
        return ofEntries(entry(k1, v1), entry(k2, v2));
    }

    public static <K, V> Map<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3) {
        return ofEntries(entry(k1, v1), entry(k2, v2), entry(k3, v3));
    }

    // 可以继续扩展到 of(k1,v1,...k10,v10) 模拟 JDK 9
    // 或者直接提供一个 varargs 版本：
    @SafeVarargs
    public static <K, V> Map<K, V> of(Object... keyValues) {
        if (keyValues.length % 2 != 0) {
            throw new IllegalArgumentException("keys and values must be paired");
        }
        Map<K, V> map = new LinkedHashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            K key = (K) Objects.requireNonNull(keyValues[i], "null keys are not allowed");
            V value = (V) Objects.requireNonNull(keyValues[i + 1], "null values are not allowed");
            if (map.put(key, value) != null) {
                throw new IllegalArgumentException("duplicate key: " + key);
            }
        }
        return Collections.unmodifiableMap(map);
    }

    public static <K, V> Map.Entry<K, V> entry(K k, V v) {
        return new AbstractMap.SimpleImmutableEntry<>(
                Objects.requireNonNull(k),
                Objects.requireNonNull(v)
        );
    }
}
