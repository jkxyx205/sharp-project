package com.rick.db.repository;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Rick.Xu
 * @date 2025/8/18 11:00
 */
public class EntityDAOManager {

    private static Map<Class<?>, EntityDAO<?, ?>> map = new HashMap<>();

    static void register(Class<?> entityClass, EntityDAO entityDAO) {
        map.put(entityClass, entityDAO);
    }

    static <T> EntityDAO<T, ?> getDAO(Class<T> entityClass) {
        return (EntityDAO<T, ?>) map.get(entityClass);
    }

}
