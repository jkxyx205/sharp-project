package com.rick.db.repository.support;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Rick.Xu
 * @date 2025/8/30 03:53
 */
@UtilityClass
public class SqlHelper {

    public static String buildWhere(String condition) {
        return StringUtils.isBlank(condition) ? "" : " WHERE " + condition;
    }

}
