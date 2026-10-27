package com.rick.db.repository.support;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.Objects;
import java.util.regex.Pattern;

import static com.rick.db.repository.support.Constants.COLUMN_NAME_SEPARATOR_REGEX;

/**
 * @author Rick.Xu
 * @date 2025/8/30 03:53
 */
@UtilityClass
public class SqlHelper {

    public static String buildSelect(String tableName, String columnNames) {
        return "SELECT " + columnNames + " FROM " + tableName;
    }

    public static String buildWhere(String condition) {
        return StringUtils.isBlank(condition) ? "" : " WHERE " + condition;
    }

    public static String buildSelectWhere(String tableName, String columnNames, String condition) {
        return buildSelect(tableName, columnNames) + buildWhere(condition);
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

    /**
     *
     * @param columnName material_code
     * @param value M01
     * @param holderParam :materialCode /  ?
     * @return
     */
    public String buildNullColumnCondition(String columnName, Object value, String holderParam) {
        return (Objects.isNull(value) ? columnName + " IS NULL" : columnName + " = " + holderParam);
    }

    /**
     * 判断 SQL 的 WHERE 条件中是否包含指定字段。
     */
    public static boolean hasWhereField(String sql, String fieldName) {
        if (sql == null || fieldName == null) {
            return false;
        }

        // 移除字符串、标识符引用和注释，避免误匹配
        String cleanedSql = sql
                .replaceAll("'([^']|'')*'", " ")
                .replaceAll("\"([^\"]|\"\")*\"", " ")
                .replaceAll("(?m)--[^\\r\\n]*", " ")
                .replaceAll("/\\*[\\s\\S]*?\\*/", " ");

        // 提取 WHERE 部分
        Pattern wherePattern = Pattern.compile(
                "(?i)\\bwhere\\b([\\s\\S]*?)(?=\\bgroup\\s+by\\b|\\border\\s+by\\b|\\blimit\\b|\\bunion\\b|$)"
        );

        java.util.regex.Matcher matcher = wherePattern.matcher(cleanedSql);

        if (!matcher.find()) {
            return false;
        }

        String where = matcher.group(1);

        Pattern fieldPattern = Pattern.compile(
                "(?i)(?<![\\w$])(?:\\w+\\s*\\.\\s*)?"
                        + Pattern.quote(fieldName)
                        + "(?![\\w$])"
        );

        return fieldPattern.matcher(where).find();
    }
}
