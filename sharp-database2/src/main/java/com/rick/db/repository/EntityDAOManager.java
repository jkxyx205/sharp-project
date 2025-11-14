package com.rick.db.repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Rick.Xu
 * @date 2025/8/18 11:00
 */
public class EntityDAOManager {

    private static Map<Class<?>, EntityDAO<?, ?>> map = new HashMap<>();

    public static void register(Class<?> entityClass, EntityDAO entityDAO) {
        map.put(entityClass, entityDAO);
    }

    public static <T> EntityDAO<T, ?> getDAO(Class<T> entityClass) {
        return (EntityDAO<T, ?>) map.get(entityClass);
    }

    public static Collection<EntityDAO<?, ?>> getAllEntityDAO() {
        return map.values();
    }

    static final ThreadLocal<Collection<Object>> threadLocalEntity = ThreadLocal.withInitial(ArrayList::new);

    static final ThreadLocal<Integer> localStack = ThreadLocal.withInitial(() -> 0);

}
