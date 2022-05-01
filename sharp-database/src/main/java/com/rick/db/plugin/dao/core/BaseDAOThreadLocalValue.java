package com.rick.db.plugin.dao.core;

import lombok.experimental.UtilityClass;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author Rick
 * @createdAt 2022-04-15 14:51:00
 */
@UtilityClass
public class BaseDAOThreadLocalValue {

    private static ThreadLocal<Set<String>> threadLocalContainer = ThreadLocal.withInitial(() -> new CopyOnWriteArraySet<>());

    public static void add(String ignoreMapping) {
        threadLocalContainer.get().add(ignoreMapping);
    }

    public static boolean remove(String ignoreMapping) {
        return threadLocalContainer.get().remove(ignoreMapping);
    }

    public static void removeByTableName(String tableName) {
        for (String ignoreMapping : threadLocalContainer.get()) {
            if (ignoreMapping.startsWith(tableName)) {
                remove(ignoreMapping);
            }
        }
    }

}
