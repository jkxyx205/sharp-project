package com.rick.db.repository;

import com.rick.db.repository.support.SqlHelper;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Rick.Xu
 * @date 2025/8/27 21:17
 */
public class ExtendTableDAOImpl extends TableDAOImpl implements TableDAO {

    private Map<String, EntityDAO<?, ?>> tableNameDAOMap;

    public ExtendTableDAOImpl(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        super(namedParameterJdbcTemplate);
    }

    @Override
    public int update(String tableName, String columns, String condition, Object... args) {
        columns = "update_by = ?, update_time = ?," + columns;
        return getNamedParameterJdbcTemplate().getJdbcTemplate().update("UPDATE " + tableName + " SET " + columns + SqlHelper.buildWhere(condition), ArrayUtils.addAll(new Object[]{getUserId(), LocalDateTime.now()}, args));
    }

    @Override
    public int update(String tableName, String columns, String condition, Map<String, ?> paramMap) {
        HashMap<String, Object> mergedParamMap = new HashMap<>(paramMap);
        mergedParamMap.put("baseEntityInfo.updateBy", getUserId());
        mergedParamMap.put("baseEntityInfo.updateTime", LocalDateTime.now());
        mergedParamMap.put("baseEntityInfo.deleted", false);
        return getNamedParameterJdbcTemplate().update("UPDATE " + tableName + " SET " + columns + SqlHelper.buildWhere(condition), mergedParamMap);
    }

    @Override
    public int insert(String tableName, String columnNames, Map<String, ?> paramMap) {
        return super.insert(tableName, columnNames, addInsertInfo(tableName, paramMap));
    }

    @Override
    public Number insertAndReturnKey(String tableName, String columnNames, Map<String, ?> paramMap, String... idColumnName) {
        return super.insertAndReturnKey(tableName, columnNames, addInsertInfo(tableName, paramMap), idColumnName);
    }

    @Override
    public int delete(String tableName, String condition, Object... args) {
        if (Objects.nonNull(tableNameDAOMap.get(tableName))) {
            return update(tableName, "is_deleted = ?", condition + " AND is_deleted = false", ArrayUtils.addAll(new Object[]{true}, args));
        } else {
            return super.delete(tableName, condition, args);
        }
    }

    @Override
    public int delete(String tableName, String condition, Map<String, ?> paramMap) {
        if (Objects.nonNull(tableNameDAOMap.get(tableName))) {
            Map<String, Object> mergedParamMap = new HashMap<>(paramMap);
            mergedParamMap.put("deleted", true);
            return update(tableName, "is_deleted = :deleted", condition + " AND is_deleted = false", mergedParamMap);
        } else {
            return super.delete(tableName, condition, paramMap);
        }
    }

    @Override
    public <E> List<E> select(Class<E> clazz, String sql, Object... args) {
        return super.select(clazz, isSimpleSingleTable(sql) ? addIsDeletedCondition(sql) : sql, args);
    }

    @Override
    public <E> List<E> select(String sql, Map<String, ?> paramMap, JdbcTemplateCallback<E> jdbcTemplateCallback) {
        return super.select(isSimpleSingleTable(sql) ? addIsDeletedCondition(sql) : sql, paramMap, jdbcTemplateCallback);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        tableNameDAOMap =  EntityDAOManager.getAllEntityDAO().stream().collect(Collectors.toMap(entityDAO -> entityDAO.getTableMeta().getTableName(), Function.identity()));
    }

    private Map<String, Object> addInsertInfo(String tableName, Map<String, ?> paramMap) {
        Map<String, Object> mergedParamMap = new HashMap<>(paramMap);
        if (Objects.nonNull(tableNameDAOMap.get(tableName))) {
            LocalDateTime now = LocalDateTime.now();
            long userId = getUserId();

            mergedParamMap.put("create_by", userId);
            mergedParamMap.put("create_time", now);
            mergedParamMap.put("update_by", userId);
            mergedParamMap.put("update_time", now);
        }

        mergedParamMap.put("is_deleted", false);
        return mergedParamMap;
    }

    public long getUserId() {
//        User user = UserContextHolder.get();
//        user = (user == null) ? User.builder().id(1L).build() : user;
//        return user.getId();
        return 1L;
    }


    private static final Pattern WHERE_PATTERN = Pattern.compile("\\bwhere\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern ORDER_GROUP_LIMIT_PATTERN =
            Pattern.compile("\\b(order\\s+by|group\\s+by|limit)\\b", Pattern.CASE_INSENSITIVE);

    public static String addIsDeletedCondition(String sql) {
        if (StringUtils.isBlank(sql)) {
            return sql;
        }

        String trimmedSql = sql.trim();

        Matcher whereMatcher = WHERE_PATTERN.matcher(trimmedSql);
        Matcher clauseMatcher = ORDER_GROUP_LIMIT_PATTERN.matcher(trimmedSql);

        if (whereMatcher.find()) {
            // 已有 WHERE，找到第一个 ORDER/GROUP/LIMIT 子句位置
            int insertPos = trimmedSql.length();
            if (clauseMatcher.find()) {
                insertPos = clauseMatcher.start();
            }
            // 在 WHERE 子句中加 AND is_deleted = false
            return new StringBuilder(trimmedSql)
                    .insert(insertPos, " AND is_deleted = false ")
                    .toString();
        } else {
            // 没有 WHERE，插入 WHERE is_deleted = false
            int insertPos = trimmedSql.length();
            if (clauseMatcher.find()) {
                insertPos = clauseMatcher.start();
            }
            return new StringBuilder(trimmedSql)
                    .insert(insertPos, " WHERE is_deleted = false ")
                    .toString();
        }
    }

    public static boolean isSimpleSingleTable(String sql) {
        if (sql == null || sql.trim().isEmpty()) return false;

        String normalized = sql.replaceAll("\\s+", " ").trim().toUpperCase();

        // 基本检查
        if (!normalized.startsWith("SELECT ") || !normalized.contains(" FROM ")) {
            return false;
        }

        // 排除子查询：任何包含括号的都排除
        if (sql.contains("(") || sql.contains(")")) {
            return false;
        }

        // 排除其他多表关键字
        return !normalized.matches(".*\\b(JOIN|UNION|EXISTS)\\b.*");
    }
}
