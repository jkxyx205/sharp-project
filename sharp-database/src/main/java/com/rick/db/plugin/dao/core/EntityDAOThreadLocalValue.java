package com.rick.db.plugin.dao.core;

import lombok.experimental.UtilityClass;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * @author Rick
 * @createdAt 2022-04-15 14:51:00
 */
@UtilityClass
public class EntityDAOThreadLocalValue {

    private static ThreadLocal<Set<String>> threadLocalContainer = ThreadLocal.withInitial(() -> new HashSet<>());

    public static void add(String ignoreMapping) {
        threadLocalContainer.get().add(ignoreMapping);
    }

    public static boolean remove(String ignoreMapping) {
        return threadLocalContainer.get().remove(ignoreMapping);
    }

    public static void removeAll() {
        threadLocalContainer.remove();
    }

    public static void removeByTableName(String tableName) {
        Iterator<String> iterator = threadLocalContainer.get().iterator();
        while (iterator.hasNext()) {
            String storeKey = iterator.next();
            if (storeKey.startsWith(tableName)) {
                iterator.remove();
            }
        }
    }
}
