package com.rick.db.repository.support.baseinfo;

import com.rick.db.repository.*;
import com.rick.db.repository.support.SqlHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.rick.db.repository.support.Constants.*;

/**
 * @author Rick.Xu
 * @date 2025/8/27 21:17
 */
@Slf4j
public class ExtendTableDAOImpl extends TableDAOImpl implements TableDAO {

    private Map<String, EntityDAO<?, ?>> tableNameDAOMap;

    private boolean trackIfHasUpdate = true;

    public ExtendTableDAOImpl(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        super(namedParameterJdbcTemplate);
    }

    @Override
    public int update(String tableName, String columnsCondition, String condition, Object... args) {
        // 判断是否字段发生更新
        if (!hasUpdate(tableName, columnsCondition, condition, args)) {
            return 0;
        }

        columnsCondition = "update_by = ?, update_time = ?," + columnsCondition;
        // return getNamedParameterJdbcTemplate().getJdbcTemplate().update("UPDATE " + tableName + " SET " + columnsCondition + SqlHelper.buildWhere(condition), ArrayUtils.addAll(new Object[]{getUserId(), LocalDateTime.now()}, args));
        return super.update(tableName, columnsCondition, condition, ArrayUtils.addAll(new Object[]{getUserId(), LocalDateTime.now()}, args));
    }

    @Override
    public int update(String tableName, String columnsCondition, String condition, Map<String, Object> paramMap) {
        paramMap.put("baseEntityInfo.updateBy", getUserId());
        paramMap.put("baseEntityInfo.updateTime", LocalDateTime.now());
        paramMap.put("baseEntityInfo.deleted", false);

        // 判断是否字段发生更新
        if (!hasUpdate(tableName, columnsCondition, condition, paramMap)) {
            return 0;
        }

//        return getNamedParameterJdbcTemplate().update("UPDATE " + tableName + " SET " + columnsCondition + SqlHelper.buildWhere(condition), paramMap);
        return super.update(tableName, columnsCondition, condition, paramMap);
    }

    private boolean hasUpdate(String tableName, String columnsCondition, String condition, Object... args) {
        if (!trackIfHasUpdate) {
            return true;
        }

        String columnsConditionFieldNames = extractFieldNames(columnsCondition);
        String selectSQL = SqlHelper.buildSelectWhere(tableName, columnsConditionFieldNames, condition);

        int columnsConditionFieldNamesArgsLength = columnsConditionFieldNames.split(COLUMN_NAME_SEPARATOR_REGEX).length;
        List<Map<String, Object>> dbMapList = select(selectSQL, Arrays.copyOfRange(args, args.length - columnsConditionFieldNamesArgsLength, args.length));
        if (CollectionUtils.isEmpty(dbMapList)) {
            return true;
        }

        List<String> columnNameList = extractFieldArray(columnsCondition);

        for (Map<String, Object> dbMap : dbMapList) {
            for (int i = 0; i < columnNameList.size(); i++) {
                String columnName = columnNameList.get(i);
                if (Objects.equals(columnName, ID_COLUMN_NAME)
                        || Objects.equals(columnName, CREATE_ID_COLUMN_NAME)
                        || Objects.equals(columnName, CREATE_TIME_COLUMN_NAME)
                        || Objects.equals(columnName, UPDATE_ID_COLUMN_NAME)
                        || Objects.equals(columnName, UPDATE_TIME_COLUMN_NAME)) {
                    continue;
                }

                Object paramValue = args[i];
                if (checkIfChanged(dbMap, columnName, paramValue)) return true;
            }
        }

        return false;
    }

    private boolean hasUpdate(String tableName, String columnsCondition, String condition, Map<String, Object> paramMap) {
        if (!trackIfHasUpdate) {
            return true;
        }

        String selectSQL = SqlHelper.buildSelectWhere(tableName, extractFieldNames(columnsCondition), condition);
        List<Map<String, Object>> dbMapList = select(selectSQL, paramMap);

//        if (CollectionUtils.isEmpty(dbMapList) && Objects.nonNull(paramMap.get(ID_COLUMN_NAME)) && condition.indexOf(":" + ID_COLUMN_NAME) > -1) {
//            // 处理 Id.GenerationType.ASSIGN
//            return true;
//        }
        if (CollectionUtils.isEmpty(dbMapList)) {
            return true;
        }

        Map<String, String> columnNameParamMap = extractFieldMap(columnsCondition);

        for (Map<String, Object> dbMap : dbMapList) {
            for (Map.Entry<String, String> columnNameEntry : columnNameParamMap.entrySet()) {
                String columnName = columnNameEntry.getKey();
                if (Objects.equals(columnName, ID_COLUMN_NAME)
                        || Objects.equals(columnName, CREATE_ID_COLUMN_NAME)
                        || Objects.equals(columnName, CREATE_TIME_COLUMN_NAME)
                        || Objects.equals(columnName, UPDATE_ID_COLUMN_NAME)
                        || Objects.equals(columnName, UPDATE_TIME_COLUMN_NAME)) {
                    continue;
                }

                Object paramValue = paramMap.get(columnNameParamMap.get(columnName));

                if (checkIfChanged(dbMap, columnName, paramValue)) return true;

//                if (isSameOrRelated(paramValue, dbValue)) {
//                    if (compareSameOrRelated(paramValue, dbValue) == 0) {
//                        continue;
//                    }
//                }
//
//                paramValue = ObjectUtils.toString(paramValue);
//                dbValue = ObjectUtils.toString(dbValue);
//
//                if (!Objects.equals(dbValue, paramValue)) {
//                    return true;
//                }
            }
        }

        return false;
    }

    private boolean checkIfChanged(Map<String, Object> dbMap, String columnName, Object paramValue) {
        Object dbValue = dbMap.get(columnName);

        if (isSameOrRelated(paramValue, dbValue) && compareSameOrRelated(paramValue, dbValue) == 0) {
            // 同类型且相等，跳过
        } else if (!Objects.equals(ObjectUtils.toString(dbValue), ObjectUtils.toString(paramValue))) {
            if (log.isDebugEnabled()) {
                log.debug("checkIfChanged: columnName【{}】changed from 【{}】to【{}】", columnName, dbValue, paramValue);
            }
            return true;
        }
        return false;
    }

    @Override
    public int insert(String tableName, String columnNames, Map<String, Object> paramMap) {
        return super.insert(tableName, columnNames, addInsertInfo(tableName, paramMap));
    }

    @Override
    public Number insertAndReturnKey(String tableName, String columnNames, Map<String, Object> paramMap, String... idColumnName) {
        return super.insertAndReturnKey(tableName, columnNames, addInsertInfo(tableName, paramMap), idColumnName);
    }

    @Override
    public List<Object> batchInsert(String tableName, String columnsName, String varCondition, List<Object[]> paramsList) {
//        因为参数是 Object[] ExtendTableDAOImpl 没有办法保证 addInsertInfo，需要在应用层传入
//        LocalDateTime now = LocalDateTime.now();
//        for (Object[] params : paramsList) {
//            params[2] = 1;
//            params[3] = now;
//            params[4] = 1;
//            params[5] = now;
//            params[6] = false;
//        }
        return super.batchInsert(tableName, columnsName, varCondition, paramsList);
    }

    @Override
    public int delete(String tableName, String condition, Object... args) {
        if (Objects.nonNull(tableNameDAOMap.get(tableName))) {
            return update(tableName, LOGIC_DELETE_COLUMN_NAME + " = ?", condition + " AND "+LOGIC_DELETE_COLUMN_NAME+" = false", ArrayUtils.addAll(new Object[]{true}, args));
        } else {
            return super.delete(tableName, condition, args);
        }
    }

    @Override
    public int delete(String tableName, String condition, Map<String, Object> paramMap) {
        if (Objects.nonNull(tableNameDAOMap.get(tableName))) {
            Map<String, Object> mergedParamMap = new HashMap<>(paramMap);
            mergedParamMap.put("deleted", true);
            return update(tableName, LOGIC_DELETE_COLUMN_NAME + " = :deleted", condition + " AND "+LOGIC_DELETE_COLUMN_NAME+" = false", mergedParamMap);
        } else {
            return super.delete(tableName, condition, paramMap);
        }
    }

    @Override
    public <E> List<E> select(Class<E> clazz, String sql, Object... args) {
        return super.select(clazz, addIsDeletedCondition(sql), args);
    }

    @Override
    public <E> List<E> select(String sql, Map<String, Object> paramMap, JdbcTemplateCallback<E> jdbcTemplateCallback) {
        return super.select(addIsDeletedCondition(sql), paramMap, jdbcTemplateCallback);
    }

    @Override
    public List<Map<String, Object>> select(String sql, Object... args) {
        return super.select(addIsDeletedCondition(sql), args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        tableNameDAOMap =  EntityDAOManager.getAllEntityDAO().stream().collect(Collectors.toMap(entityDAO -> entityDAO.getTableMeta().getTableName(), Function.identity()));
    }

    private Map<String, Object> addInsertInfo(String tableName, Map<String, Object> paramMap) {
        if (Objects.nonNull(tableNameDAOMap.get(tableName))) {
            LocalDateTime now = LocalDateTime.now();
            long userId = getUserId();

            paramMap.put(CREATE_ID_COLUMN_NAME, userId);
            paramMap.put(CREATE_TIME_COLUMN_NAME, now);
            paramMap.put(UPDATE_ID_COLUMN_NAME, userId);
            paramMap.put(UPDATE_TIME_COLUMN_NAME, now);
        }

        paramMap.put(LOGIC_DELETE_COLUMN_NAME, false);
        addInsertInfo(paramMap);
        return paramMap;
    }

    protected long getUserId() {
        return 1L;
    }

    protected void addInsertInfo(Map<String, Object> paramMap) {

    }

    private static final Pattern WHERE_PATTERN = Pattern.compile("\\bwhere\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern ORDER_GROUP_LIMIT_PATTERN =
            Pattern.compile("\\b(order\\s+by|group\\s+by|limit)\\b", Pattern.CASE_INSENSITIVE);

    public String addIsDeletedCondition(String sql) {
        if (StringUtils.isBlank(sql) || hasIsDeletedCondition(sql)) {
            return sql;
        }

        String trimmedSql = sql.trim();

        if (!SqlSingleTableChecker.isSingleTableQuery(sql)) {
            return sql;
        }

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
                    .insert(insertPos, " AND "+LOGIC_DELETE_COLUMN_NAME+" = false ")
                    .toString();
        } else {
            // 没有 WHERE，插入 WHERE is_deleted = false
            int insertPos = trimmedSql.length();
            if (clauseMatcher.find()) {
                insertPos = clauseMatcher.start();
            }
            return new StringBuilder(trimmedSql)
                    .insert(insertPos, " WHERE "+LOGIC_DELETE_COLUMN_NAME+" = false ")
                    .toString();
        }
    }

    /**
     * 判断客户端是否已经对 is_deleted 条件做了设置
     * @param sql
     * @return
     */
    private boolean hasIsDeletedCondition(String sql) {
        if (sql == null || sql.isEmpty()) return false;
        String regex = "(?i)\\bis_deleted\\s*(=|<>|!=|>=|<=|>|<)|(?i)\\bis_deleted\\s+(is|in|like)\\s+";
        return Pattern.compile(regex).matcher(sql).find();
    }

    private String extractFieldNames(String sqlUpdateParams) {
        String[] pairs = sqlUpdateParams.split(",");
        return Arrays.stream(pairs)
                .map(pair -> pair.split("=")[0].trim())
                .collect(Collectors.joining(", "));
    }

    private Map<String, String> extractFieldMap(String sqlUpdateParams) {
        return Arrays.stream(sqlUpdateParams.split(","))
                .map(pair -> pair.split("=", 2))
                .collect(Collectors.toMap(
                        parts -> parts[0].trim(),
                        parts -> parts[1].trim()
                                .replaceAll("^:", "")        // 去掉前缀 :
                                .replaceAll("::[a-zA-Z]+$", "") // 去掉 postgres 类型转换 ::json 等
                                .trim(),
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
    }

    private List<String> extractFieldArray(String sqlUpdateParams) {
        return Arrays.stream(sqlUpdateParams.split(","))
                .map(pair -> pair.split("=", 2)[0].trim())
                .collect(Collectors.toList());
    }

    private boolean isSameOrRelated(Object obj1, Object obj2) {
        if (obj1 == null || obj2 == null) return false;

        Class<?> class1 = obj1.getClass();
        Class<?> class2 = obj2.getClass();

        // 同一个类
        if (class1 == class2) return true;

        // 判断继承关系
        return class1.isAssignableFrom(class2) || class2.isAssignableFrom(class1);
    }

    private int compareSameOrRelated(Object paramValue, Object dbValue) {
        if (paramValue == null && dbValue == null) return 0;
        if (paramValue == null) return -1;
        if (dbValue == null) return 1;

        // 两者都实现了 Comparable
        if (paramValue instanceof Comparable && dbValue instanceof Comparable) {
            // 同类型直接比较
            if (paramValue.getClass() == dbValue.getClass()) {
                return ((Comparable) paramValue).compareTo(dbValue);
            }

            // 跨类型：统一转为 String 比较
            return paramValue.toString().compareTo(dbValue.toString());

        }

        return -1;
    }

//    private int compare(Object paramValue, Object dbValue) {
//        if (paramValue == null && dbValue == null) return 0;
//        if (paramValue == null) return -1;
//        if (dbValue == null) return 1;
//
//        // 两者都实现了 Comparable
//        if (paramValue instanceof Comparable && dbValue instanceof Comparable) {
//            // 同类型直接比较
//            if (paramValue.getClass() == dbValue.getClass()) {
//                return ((Comparable) paramValue).compareTo(dbValue);
//            }
//
//            // 跨类型：统一转为 String 比较
//            return paramValue.toString().compareTo(dbValue.toString());
//        }
//
//        throw new IllegalArgumentException("对象未实现 Comparable 接口，无法比较: "
//                + paramValue.getClass() + " vs " + dbValue.getClass());
//    }

}
