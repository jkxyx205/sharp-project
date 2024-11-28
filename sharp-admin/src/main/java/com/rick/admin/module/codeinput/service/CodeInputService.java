package com.rick.admin.module.codeinput.service;

import com.google.common.collect.Lists;
import com.rick.admin.common.exception.ResourceNotFoundException;
import com.rick.db.dto.Grid;
import com.rick.db.plugin.GridUtils;
import com.rick.db.service.support.Params;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Rick.Xu
 * @date 2024/9/26 17:35
 */
@Service
public class CodeInputService {

    private static final Map<String, CodeInputService.CodeInputContext> SQL_MAPPING = new HashMap<>();

    private static final String TYPE = "searchType";

    {
        // 用户信息
        String userSql = "select id, code, name from sys_user where code like :code OR name like :code";
        SQL_MAPPING.put("users",
                new CodeInputService.CodeInputContext(Arrays.asList("id", "编号", "姓名"), Arrays.asList("id", "code", "name"),
                        userSql)
        );
        SQL_MAPPING.put("users_dialog",
                new CodeInputService.CodeInputContext(Arrays.asList("id", "编号", "姓名"), Arrays.asList("id", "code", "name"),
                        userSql)
        );
    }

    /**
     * 获取 columnProperties 信息
     * @param key
     * @return
     */
    public Map<String, Object> codeSearchResult(String key) {
        CodeInputService.CodeInputContext context = SQL_MAPPING.get(key);
        List<Map<String, Object>> list = Collections.emptyList();
        return Params.builder(2)
                .pv("columnProperties", handlerData(context, list))
                .pv("data", list)
                .build();
    }

    public Map<String, Object> codeSearchResult(String key, String code, Map<String, Object> params) {
        CodeInputService.CodeInputContext context = SQL_MAPPING.get(key);

        if (Objects.nonNull(code)) {
            code = code.trim();
        }

        if (Objects.isNull(context)) {
            throw new ResourceNotFoundException(key);
        }

        if (params == null) {
            params = new HashMap<>();
        }

        params.put("code", code);

        List<Map<String, Object>> list= GridUtils.list(context.sql, params).getRows();
        List<Map<String, Object>> columnProperties = handlerData(context, list);

        return Params.builder(2)
                .pv("columnProperties", columnProperties)
                .pv("data", list)
                .build();
    }

    public Map<String, Object> dialogSearchResult(String key, Map<String, Object> params) {
        Object type = params.get(TYPE);
        CodeInputService.CodeInputContext context = SQL_MAPPING.get(key+ "_" + ObjectUtils.defaultIfNull(type, "dialog"));

        if (Objects.isNull(context)) {
            throw new ResourceNotFoundException(key);
        }

        Grid<Map<String, Object>> grid = GridUtils.list(context.sql, params);
        List<Map<String, Object>> columnProperties = handlerData(context, grid.getRows());

        return Params.builder(2)
                .pv("columnProperties", columnProperties)
                .pv("data", grid)
                .build();
    }

    class CodeInputContext {

        List<String> columnLabels;

        List<String> columnNames;

        String sql;

        CodeInputService.RowAfterSearchHandler rowAfterSearchHandler;

        public CodeInputContext(List<String> columnLabels, List<String> columnNames, String sql) {
            this(columnLabels, columnNames,  sql, null);
        }

        public CodeInputContext(List<String> columnLabels, List<String> columnNames, String sql, CodeInputService.RowAfterSearchHandler rowAfterSearchHandler) {
            this(columnLabels, columnNames, sql, null, rowAfterSearchHandler, null);
        }

        public CodeInputContext(List<String> columnLabels, List<String> columnNames, String sql, String masterTableName, CodeInputService.RowAfterSearchHandler rowAfterSearchHandler) {
            this(columnLabels, columnNames, sql, masterTableName, rowAfterSearchHandler, null);
        }

        public CodeInputContext(List<String> columnLabels, List<String> columnNames, String sql, String masterTableName, CodeInputService.RowAfterSearchHandler rowAfterSearchHandler, Function<String, String> sqlFunction) {
            this.columnLabels = columnLabels;
            this.columnNames = columnNames;
//            masterTableName = StringUtils.defaultString(masterTableName, StringUtils.substringBetween(sql.toUpperCase(), "FROM ", " ").trim());
            masterTableName = StringUtils.defaultString(masterTableName, getTableNameBySelectSql(sql).trim());
            this.sql = sql + " AND "+masterTableName+".is_deleted = 0";
            this.rowAfterSearchHandler = rowAfterSearchHandler;
            if (sqlFunction != null) {
                this.sql = sqlFunction.apply(this.sql);
            }
        }
    }

    interface RowAfterSearchHandler {
        List<Map<String, Object>> handler(List<Map<String, Object>> list);
    }

    private static List<Map<String, Object>> handlerData(CodeInputService.CodeInputContext context, List<Map<String, Object>> list) {
        if (context.rowAfterSearchHandler != null && list.size() > 0) {
            context.rowAfterSearchHandler.handler(list);
        }

        for (Map<String, Object> row : list) {
            if (CollectionUtils.isNotEmpty(context.columnNames)) {
                Iterator<Map.Entry<String, Object>> iterator = row.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, Object> next = iterator.next();
                    if (!context.columnNames.contains(next.getKey())) {
                        iterator.remove();
                    }
                }
            }
        }

        List<Map<String,Object>> columnProperties = Lists.newArrayListWithExpectedSize(context.columnLabels.size());

        for (int i = 0; i < context.columnLabels.size(); i++) {
            columnProperties.add(Params.builder(1)
                    .pv("name", context.columnNames.get(i))
                    .pv("label", context.columnLabels.get(i))
                    .build());
        }

        return columnProperties;
    }

    private String getTableNameBySelectSql(String selectSql) {
        Pattern pattern = Pattern.compile("from\\s+([a-zA-Z][a-zA-Z0-9_]{0,63})\\s+where", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(selectSql);
        if (matcher.find()) {
            return matcher.group(0).split("\\s+")[1];
        }

        return null;
    }
}
