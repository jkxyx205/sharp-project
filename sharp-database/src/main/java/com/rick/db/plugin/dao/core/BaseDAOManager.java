package com.rick.db.plugin.dao.core;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Rick
 * @createdAt 2021-10-31 09:09:00
 */
public class BaseDAOManager {

    public static List<BaseDAO> baseDAOList;

    public static Map<String, BaseDAO> baseDAOMap;

    private static boolean hasAutowired = false;

    public static void setBaseDAOList(List<BaseDAO> baseDAOList) {
        if (!hasAutowired) {
            BaseDAOManager.baseDAOList = Collections.unmodifiableList(baseDAOList);
            BaseDAOManager.baseDAOMap = Objects.nonNull(baseDAOList) ? Collections.unmodifiableMap(BaseDAOManager.baseDAOList.stream().collect(Collectors.toMap(d -> d.getTableName(), v -> v))) : null;
            BaseDAOManager.hasAutowired = true;
        }
    }

}
