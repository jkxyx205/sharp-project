package com.rick.db.repository.support;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;

import static com.rick.db.repository.support.Constants.COLUMN_NAME_SEPARATOR_REGEX;

/**
 * @author Rick.Xu
 * @date 2025/8/30 03:53
 */
@UtilityClass
public class SqlHelper {

    public static String buildWhere(String condition) {
        return StringUtils.isBlank(condition) ? "" : " WHERE " + condition;
    }

    public static String getInsertSQL(String tableName, String columnNames) {
        return String.format("INSERT INTO %s (%s) VALUES(%s)",
                tableName,
                columnNames,
                StringUtils.join(Collections.nCopies(columnNames.split(COLUMN_NAME_SEPARATOR_REGEX).length, "?"), ", "));
    }

    public static String getInsertSQL(String tableName, String columnNames, String columnsCondition) {
        return String.format("INSERT INTO %s (%s) VALUES(%s)",
                tableName,
                columnNames,
                columnsCondition);
    }
}
