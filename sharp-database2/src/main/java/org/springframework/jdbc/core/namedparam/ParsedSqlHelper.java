package org.springframework.jdbc.core.namedparam;

import lombok.experimental.UtilityClass;

import java.util.List;

/**
 * ParsedSql只有在包org.springframework.jdbc.core.namedparam才能获取getParameterNames
 * @author Rick
 * @createdAt 2021-10-15 22:16:00
 */
@UtilityClass
public class ParsedSqlHelper {

    public static List<String> get(String sql) {
        ParsedSql parsedSql = NamedParameterUtils.parseSqlStatement(sql);
        return parsedSql.getParameterNames();
    }
}
