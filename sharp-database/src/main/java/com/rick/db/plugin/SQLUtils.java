package com.rick.db.plugin;

import com.google.common.collect.Maps;
import com.rick.common.http.convert.JsonStringToObjectConverterFactory;
import com.rick.common.util.EnumUtils;
import com.rick.common.util.JsonUtils;
import com.rick.db.config.SharpDatabaseProperties;
import com.rick.db.constant.SharpDbConstants;
import com.rick.db.dto.PageModel;
import com.rick.db.formatter.AbstractSqlFormatter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlTypeValue;
import org.springframework.jdbc.core.StatementCreatorUtils;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.util.Assert;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static com.rick.db.config.Constants.DB_MYSQL;
import static com.rick.db.config.Constants.GROUP_DUMMY_TABLE_NAME;

/**
 * @author Rick
 * @createdAt 2021-02-07 17:40:00
 */
@Slf4j
public final class SQLUtils {

    private static JdbcTemplate JDBC_TEMPLATE;

    private static NamedParameterJdbcTemplate NAMED_JDBC_TEMPLATE;

    private static final int IN_SIZE = 1000;

    private static final String SQL_PATH_IN = "IN";

    private static final String SQL_PATH_NOT_IN = "NOT IN";

    private static final String ASC = "ASC";

    private static SharpDatabaseProperties SHARP_DATABASE_PROPERTIES;

    public void setNamedJdbcTemplate(NamedParameterJdbcTemplate namedJdbcTemplate) {
        SQLUtils.NAMED_JDBC_TEMPLATE = namedJdbcTemplate;
        SQLUtils.JDBC_TEMPLATE = namedJdbcTemplate.getJdbcTemplate();
    }

    public void setSharpDatabaseProperties(SharpDatabaseProperties sharpDatabaseProperties) {
        SQLUtils.SHARP_DATABASE_PROPERTIES = sharpDatabaseProperties;
    }

    public static void execute(String sql) {
        JDBC_TEMPLATE.execute(sql);
    }

    public static int insert(String tableName, Map<String, Object> params) {
        return insert(tableName,
                StringUtils.join(params.keySet(), ","),
                convertToArray(params));
    }

    /**
     * 单个批量插入
     * @param tableName t_xx
     * @param columnNames id, namme
     * @return INSERT INTO t_xx(id, namme) VALUES(?, ?)
     */
    public static int insert(String tableName, String columnNames, Object[] params) {
        String insertSQL = getInsertSQL(tableName, columnNames);
        if (log.isDebugEnabled()) {
            log.debug("SQL => [{}], args:=> [{}]", insertSQL, params);
        }
        return SQLUtils.JDBC_TEMPLATE.update(insertSQL,params);
    }

    /**
     * 批量插入
     * @param tableName
     * @param columnNames
     * @param paramsList
     * @return
     */
    public static int[] insert(String tableName, String columnNames, List<Object[]> paramsList) {
        String insertSQL = getInsertSQL(tableName, columnNames);
        if (log.isDebugEnabled()) {
            log.debug("SQL => [{}], args:=> [{}]", insertSQL, paramsList);
        }
        return SQLUtils.JDBC_TEMPLATE.batchUpdate(insertSQL, paramsList);
    }

    public static int update(String tableName, Map<String, Object> params, String idColumnName) {
        Serializable id = (Serializable) params.get(idColumnName);
        params.remove(idColumnName);
        return update(tableName, StringUtils.join(params.keySet(), ","), convertToArray(params), id, idColumnName);
    }

    public static int update(String tableName, String updateColumnNames, Object[] params, Serializable id) {
        return update(tableName, updateColumnNames, params, id, "id");
    }

    /**
     * 根据id更新内容
     * @param tableName  t_xx
     * @param updateColumnNames a, b, c
     * @param params set的参数
     * @param id 1
     * @param idColumnName 一般为id
     * UPDATE t_xx SET a = ?, b = ?, c = ? WHERE id = ?
     * @return
     */
    public static int update(String tableName, String updateColumnNames, Object[] params, Serializable id, String idColumnName) {
        Assert.notNull(id, "主键不能为null");
        Object[] mergedParams = new Object[params.length + 1];
        mergedParams[params.length] = id;
        System.arraycopy(params, 0, mergedParams, 0, params.length);
        return update(tableName, updateColumnNames, mergedParams, ""+idColumnName+" = ?");
    }

    /**
     * 根据条件更新
     * @param tableName
     * @param updateColumnNames
     * @param params set和conditionSQL的参数
     * @param conditionSQL
     * @return
     */
    public static int update(String tableName, String updateColumnNames, Object[] params, String conditionSQL) {
        String updateSQL = getUpdateSQL(tableName, updateColumnNames, conditionSQL);
        if (log.isDebugEnabled()) {
            log.debug("SQL => [{}], args:=> [{}]", updateSQL, params);
        }
        return SQLUtils.JDBC_TEMPLATE.update(updateSQL, params);
    }

    /**
     * 根据条件批量更新
     * @param tableName
     * @param updateColumnNames
     * @param paramsList set和conditionSQL的参数
     * @param conditionSQL
     * @return
     */
    public static int[] update(String tableName, String updateColumnNames, List<Object[]> paramsList, String conditionSQL) {
        String updateSQL = getUpdateSQL(tableName, updateColumnNames, conditionSQL);
        if (log.isDebugEnabled()) {
            log.debug("SQL => [{}], args:=> [{}]", updateSQL, paramsList);
        }
        return SQLUtils.JDBC_TEMPLATE.batchUpdate(updateSQL, paramsList);
    }

    /**
     * 指定某字段删除数据
     *
     * @param tableName t_user
     * @param deleteValues 23,13
     * @param deleteColumn id
     * 相当于执行SQL：DELETE FROM t_user WHERE id IN(23, 13)，如果deleteValues是空，将不会删除任何数据
     */
    public static int delete(String tableName, String deleteColumn, String deleteValues) {
        return delete(tableName, deleteColumn, deleteValues, null, null);
    }

    public static int delete(String tableName, String deleteColumn, String deleteValues, Object[] conditionParams, String conditionSQL) {
        if (StringUtils.isBlank(deleteValues)) {
            return 0;
        }
        return deleteData(tableName, deleteColumn, "IN", Arrays.asList(deleteValues.split(",")), conditionParams, conditionSQL);
    }

    /**
     * 指定某字段删除数据
     * @param deleteValues [23,13]
     * @param tableName t_user
     * @param deleteColumn id
     * 相当于执行SQL：DELETE FROM t_user WHERE id IN(23, 13)
     */
    public static int delete(String tableName, String deleteColumn, Collection<?> deleteValues) {
        return delete(tableName, deleteColumn, deleteValues, null, null);
    }

    public static int delete(String tableName, String deleteColumn, Collection<?> deleteValues, Object[] conditionParams, String conditionSQL) {
        return deleteData(tableName, deleteColumn, SQL_PATH_IN, deleteValues, conditionParams, conditionSQL);
    }

    /**
     * 级联删除
     * @param masterTable t_user
     * @param refColumnName user_id
     * @param subTables t_article
     * @return
     */
    public static int deleteCascade(String masterTable, String refColumnName, Collection<?> deleteValues, String ...subTables) {
        return deleteCascade(masterTable, refColumnName, deleteValues, null, null, subTables);
    }

    public static int deleteCascade(String masterTable, String refColumnName, Collection<?> deleteValues, Object[] conditionParams, String conditionSQL, String ...subTables) {
        Assert.notNull(masterTable, "主表不能为空");
        Assert.notNull(refColumnName, "从表外键不能为空");
        Assert.notNull(deleteValues, "masterTableIds不能为空");
        Assert.notNull(subTables, "从表不能为空");

        for (String subTable : subTables) {
            delete(subTable, refColumnName, deleteValues);
        }
        return delete(masterTable, SharpDbConstants.ID_COLUMN_NAME, deleteValues, conditionParams, conditionSQL);
    }

    /**
     * 构造删除条件
     * @param tableName
     * @param params
     * @param conditionSQL
     * @return
     */
    public static int delete(String tableName, Object[] params, String conditionSQL) {
        return SQLUtils.JDBC_TEMPLATE.update("DELETE FROM " + tableName + " WHERE " + conditionSQL, params);
    }

    /**
     * 指定某字段不删除数据，其他的都删除
     * @param deleteValues ['a', 'b']数量不能大于IN_SIZE = 1000
     * @param tableName t_product
     * @param deleteColumn name
     * 相当于执行SQL：DELETE FROM t_product WHERE not id IN('a', 'b')
     */
    public static int deleteNotIn(String tableName, String deleteColumn, Collection<?> deleteValues) {
        return deleteNotIn(tableName, deleteColumn, deleteValues, null, null);
    }

    public static int deleteNotIn(String tableName, String deleteColumn, Collection<?> deleteValues, Object[] conditionParams, String conditionSQL) {
        return deleteData(tableName, deleteColumn, SQL_PATH_NOT_IN, deleteValues, conditionParams, conditionSQL);
    }

    /**
     * 全量维护关联表关系
     * updateRelationShip("t_user_role", "role_id", "user_id", roleId, userIds)：表示为角色roleId，添加用户userIds
     * @param refTableName 关联表名称 t_user_role
     * @param keyColumn 关联的对象列名 role_id
     * @param guestColumn 被关联的对象列名 user_id
     * @param keyInstance 关联的对象列值 roleId
     * @param guestInstanceIds 被关联的对象列名值 userIds
     */
    public static void updateRefTable(String refTableName, String keyColumn, String guestColumn, Object keyInstance, Collection<?> guestInstanceIds) {
        // 删除
        if (CollectionUtils.isEmpty(guestInstanceIds)) {
            String deleteSql = String.format("DELETE FROM %s WHERE %s = ?", refTableName, keyColumn);
            if (log.isDebugEnabled()) {
                log.debug("SQL => [{}], args:=> [{}, {}]", deleteSql, refTableName, keyColumn);
            }
            SQLUtils.JDBC_TEMPLATE.update(deleteSql, keyInstance);
            return;
        }

        Map<String, Object> deleteParams = Maps.newHashMapWithExpectedSize(1);
        deleteParams.put("guestInstanceIds", guestInstanceIds);
        deleteParams.put("keyInstance", keyInstance);
        // 1. 删除
        String deleteSql = String.format("DELETE FROM %s WHERE %s = :keyInstance AND %s NOT IN (:guestInstanceIds)", refTableName, keyColumn, guestColumn);
        if (log.isDebugEnabled()) {
            log.debug("SQL => [{}], args:=> [{}, {}]", deleteSql, deleteParams);
        }
        SQLUtils.NAMED_JDBC_TEMPLATE.update(deleteSql, deleteParams);

        // 2. 插入新增
        // 2.1 库中
        String queryAllGuestInstanceIdsSQL = String.format("SELECT %s FROM %s WHERE %s = ?", guestColumn, refTableName, keyColumn);
        List<?> dbGuestInstanceIds = SQLUtils.JDBC_TEMPLATE.queryForList(queryAllGuestInstanceIdsSQL, guestInstanceIds.iterator().next().getClass(), keyInstance);
        // 2.2 新增
        List<?> newGuestInstanceIds = guestInstanceIds.stream().filter(guestId -> !dbGuestInstanceIds.contains(guestId)).collect(Collectors.toList());
        if (newGuestInstanceIds.size() == 0) {
            return;
        }

        String insertSQL = String.format("INSERT INTO %s(%s, %s) VALUES(?, ?)", refTableName, keyColumn, guestColumn);
        List<Object[]> addParams = newGuestInstanceIds.stream().map(guestInstanceId -> new Object[] {keyInstance, guestInstanceId}).collect(Collectors.toList());
        if (log.isDebugEnabled()) {
            log.debug("SQL => [{}], args:=> [{}, {}]", insertSQL, addParams);
        }

        SQLUtils.JDBC_TEMPLATE.batchUpdate(insertSQL, addParams);
    }

    /**
     * 如果是Mysql，排序会带上id一起排序
     * @param pageModel
     * @param sortableColumns
     * @return
     */
    public static void setOrderParams(PageModel pageModel, String[] sortableColumns) {
        String groupBy = getOrderBy(GROUP_DUMMY_TABLE_NAME, pageModel.getSidx(), ASC.equalsIgnoreCase(pageModel.getSord()), sortableColumns);
        if (StringUtils.isNotBlank(groupBy)) {
            int blank = groupBy.indexOf(" ");
            pageModel.setSidx(groupBy.substring(0, blank));
            pageModel.setSord(groupBy.substring(blank + 1));
        } else {
            pageModel.setSidx(null);
            pageModel.setSord(null);
        }
    }

    public static String getOrderBy(String tablePrefix, String column, Boolean asc, String[] sortableColumns) {
        if (!sortable(column, sortableColumns, true)) {
            return null;
        }

        tablePrefix = StringUtils.isBlank(tablePrefix) ? "" : tablePrefix + ".";
        asc = ObjectUtils.defaultIfNull(asc, false);
        if (SQLUtils.SHARP_DATABASE_PROPERTIES.getType().equals(DB_MYSQL)) {
            return tablePrefix + column + " " + (asc ? "asc," : "desc, ") + tablePrefix + "id DESC";
        }

        return tablePrefix + column + " " + (asc ? "asc" : "desc");
    }

    /**
     * if paramSize == 4 format as (?, ?, ?, ?)
     *
     * @param paramSize
     * @return
     */
    public static String formatInSQLPlaceHolder(int paramSize) {
        if (paramSize > IN_SIZE) {
            throw new RuntimeException("SQL_PATH_NOT_IN in的个数不能超过" + IN_SIZE);
        }

        return "(" + String.join(",", Collections.nCopies(paramSize, "?")) + ")";
    }

    private static int deleteData(String tableName, String deleteColumn, String sqlPatch, Collection<?> deleteValues, Object[] conditionParams, String conditionSQL) {
        Assert.notEmpty(deleteValues, "deleteValues cannot be null");
        int size = deleteValues.size();
        // 处理单值
//        if (size == 1) {
//            if (SQL_PATH_IN.equals(sqlPatch)) {
//                return delete(tableName, deleteValues.toArray(), deleteColumn + " = ?");
//            } else if (SQL_PATH_NOT_IN.equals(sqlPatch)) {
//                return delete(tableName, deleteValues.toArray(), deleteColumn + " <> ?");
//            }
//        }

        if (SQL_PATH_NOT_IN.equals(sqlPatch) && size > IN_SIZE) {
            throw new RuntimeException("SQL_PATH_NOT_IN in的个数不能超过" + IN_SIZE);
        }

        int count;
        if (size % IN_SIZE == 0) {
            count = size / IN_SIZE;
        } else {
            count = size / IN_SIZE + 1;
        }

        Object[] valueArray = deleteValues.toArray();
        int deletedCount = 0;
        for (int i = 1; i <= count; i++) {
            int lastIndex = (i == count) ? size : i * IN_SIZE;
            Object[] currentDeleteValue = Arrays.copyOfRange(valueArray, (i - 1) * IN_SIZE, lastIndex);
            // remove old records
            String inSql = formatInSQLPlaceHolder(currentDeleteValue.length);
            String deleteSQL = String.format("DELETE FROM " + tableName + " WHERE " + deleteColumn + " " + sqlPatch + "%s" + (StringUtils.isBlank(conditionSQL) ? "" : " AND " + conditionSQL), inSql);

            Object[] mergedParams = currentDeleteValue;
            if (ArrayUtils.isNotEmpty(conditionParams)) {
                mergedParams = new Object[currentDeleteValue.length + conditionParams.length];
                System.arraycopy(currentDeleteValue, 0, mergedParams, 0, currentDeleteValue.length);
                for (int j = 0; j < conditionParams.length; j++) {
                    mergedParams[currentDeleteValue.length + j] = conditionParams[j];
                }
            }

            deletedCount += SQLUtils.JDBC_TEMPLATE.update(deleteSQL, mergedParams);
            if (log.isDebugEnabled()) {
                log.debug("SQL => [{}], args:=> [{}]", deleteSQL, mergedParams);
            }
        }

        return deletedCount;
    }

    /**
     * 是否支持排序
     * @param column
     * @param sortableColumns
     * @param ignoreCase
     * @return
     */
    private static boolean sortable(String column, String[] sortableColumns, boolean ignoreCase) {
        if (StringUtils.isBlank(column) || Objects.isNull(sortableColumns)) {
            return false;
        }

        boolean columnSortable = false;
        for (String sortableColumn : sortableColumns) {
            if (ignoreCase) {
                columnSortable = sortableColumn.equalsIgnoreCase(column);
            } else {
                columnSortable = sortableColumn.equals(column);
            }

            if (columnSortable) {
                break;
            }
        }

        if (!columnSortable) {
            return false;
        }

        return true;
    }

    /**
     *
     * @param tableName t_xx
     * @param columnNames id, namme
     * @return INSERT INTO t_xx(id, namme) VALUES(?, ?)
     */
    private static String getInsertSQL(String tableName, String columnNames) {
        return String.format("INSERT INTO %s(%s) VALUES(%s)",
                tableName,
                columnNames,
                StringUtils.join(Collections.nCopies(columnNames.split(SharpDbConstants.COLUMN_NAME_SEPARATOR_REGEX).length, "?"), ","));
    }

    private static String getUpdateSQL(String tableName, String columnNames, String conditionSQL) {
        return "UPDATE " + tableName + " SET " + StringUtils.join(columnNames.split(SharpDbConstants.COLUMN_NAME_SEPARATOR_REGEX), " = ?,") + " = ? WHERE " + conditionSQL;
    }

    /**
     * name = :name AND age = : age
     * @param sql
     * @return name = ? AND age = ?
     */
    public static String paramsHolderToQuestionHolder(String sql) {
        return sql.replaceAll(AbstractSqlFormatter.PARAM_REGEX, "?");
    }

    public static Object[] convertToArray(Map<String, ?> map) {
        return convertToArray(map, map.keySet());
    }

    public static Object[] convertToArray(Map<String, ?> map, Collection<String> columnNames) {
        Object[] params = new Object[columnNames.size()];
        Iterator<String> iterator = columnNames.iterator();
        int i = 0;
        while (iterator.hasNext()) {
            Object param = map.get(iterator.next());
            params[i++] = param;
        }

        return params;
    }

    public static Object resolveValue(Object value) {
        return resolveValue(value, null);
    }

    public static Object resolveValue(Object value, ResolveValueFunction resolveValueFn) {
        if (Objects.isNull(value)) {
            return null;
        }
        // 处理EntityDAOImpl特殊类型
        if (Enum.class.isAssignableFrom(value.getClass())) {
            return EnumUtils.getCode((Enum) value);
        } else if (value.getClass() == Instant.class) {
            return Timestamp.from((Instant) value);
        } else if (JsonStringToObjectConverterFactory.JsonValue.class.isAssignableFrom(value.getClass())) {
            return toJson(value);
        } else if (Collection.class.isAssignableFrom(value.getClass())) {
            Collection<?> coll = (Collection<?>) value;
            if (coll.size() == 0) {
                return "[]";
            } else {
                return toJson(value);
            }/*else if (JsonStringToObjectConverterFactory.JsonValue.class.isAssignableFrom(coll.iterator().next().getClass())) {
                return toJson(value);
            }*/
        } else if (Map.class.isAssignableFrom(value.getClass())) {
            Map<String, ?> map = (Map<String, ?>)value;
            if (map.size() == 0) {
                return "{}";
            } else {
                return toJson(value);
            }
        } else if (value.getClass().isArray()) {
            int length = Array.getLength(value);
            if (length == 0) {
                return null;
            }
            StringBuilder values = new StringBuilder();
            for (int i = 0; i < length; i ++) {
                Object o = Array.get(value, i);
                values.append(o).append(",");
            }

            return values.deleteCharAt(values.length() - 1);
        }

        if (resolveValueFn != null) {
            Object[] resolveValueFnValue = resolveValueFn.apply(value);
            if (resolveValueFnValue[0] == Boolean.TRUE) {
                return resolveValueFnValue[1];
            }
        }

        // JDBC 支持类型
        int sqlTypeValue = StatementCreatorUtils.javaTypeToSqlParameterType(value.getClass());

        if (SqlTypeValue.TYPE_UNKNOWN != sqlTypeValue) {
            return value;
        } else {
            return String.valueOf(value);
        }
    }

    private static String toJson(Object value) {
        try {
            return JsonUtils.toJson(value);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public interface ResolveValueFunction {
        /**
         *
         * @param value
         * @return 0 = true 1 value
         */
        Object[] apply(Object value);
    }

}
