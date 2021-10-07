package com.rick.db.plugin;

import com.google.common.collect.Maps;
import com.rick.db.config.SharpDatabaseProperties;
import com.rick.db.dto.PageModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.io.Serializable;
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

    /**
     * 单个批量插入
     * @param tableName t_xx
     * @param columnNames id, namme
     * @return INSERT INTO t_xx(id, namme) VALUES(?, ?)
     */
    public static int insert(String tableName, String columnNames, Object[] params) {
        return SQLUtils.JDBC_TEMPLATE.update(getInsertSQL(tableName, columnNames),params);
    }

    /**
     * 批量插入
     * @param tableName
     * @param columnNames
     * @param paramsList
     * @return
     */
    public static int[] insert(String tableName, String columnNames, List<Object[]> paramsList) {
        return SQLUtils.JDBC_TEMPLATE.batchUpdate(getInsertSQL(tableName, columnNames), paramsList);
    }

    /**
     * 根据id更新内容
     * @param tableName  t_xx
     * @param updateColumnNames a, b, c
     * @param params set的参数
     * @param id 1
     * UPDATE t_xx SET a = ?, b = ?, c = ? WHERE id = ?
     * @return
     */
    public static int update(String tableName, String updateColumnNames, Object[] params, Serializable id) {
        if (Objects.isNull(id)) {
            throw new RuntimeException("主键不能为null");
        }
        Object[] mergedParams = new Object[params.length + 1];
        mergedParams[params.length] = id;
        System.arraycopy(params, 0, mergedParams, 0, params.length);
        return update(tableName, updateColumnNames, mergedParams, "id = ?");
    }

    /**
     *
     * @param tableName
     * @param updateColumnNames
     * @param params set和conditionSQL的参数
     * @param conditionSQL
     * @return
     */
    public static int update(String tableName, String updateColumnNames, Object[] params, String conditionSQL) {
        return SQLUtils.JDBC_TEMPLATE.update(getUpdateSQL(tableName, updateColumnNames, conditionSQL), params);
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
    public static void updateRefTable(String refTableName, String keyColumn, String guestColumn, long keyInstance, Collection<?> guestInstanceIds) {
        // 删除
        if (CollectionUtils.isEmpty(guestInstanceIds)) {
            SQLUtils.JDBC_TEMPLATE.update(String.format("DELETE FROM %s WHERE %s = ?", refTableName, keyColumn), keyInstance);
            return;
        }

        Map<String, Object> deleteParams = Maps.newHashMapWithExpectedSize(1);
        deleteParams.put("guestInstanceIds", guestInstanceIds);
        deleteParams.put("keyInstance", keyInstance);
        // 1. 删除
        SQLUtils.NAMED_JDBC_TEMPLATE.update(String.format("DELETE FROM %s WHERE %s = :keyInstance AND %s NOT IN (:guestInstanceIds)", refTableName, keyColumn, guestColumn), deleteParams);

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
        SQLUtils.JDBC_TEMPLATE.batchUpdate(insertSQL, addParams);
    }

    /**
     * 指定某字段删除数据
     * @param deleteValues 23,13
     * @param tableName t_user
     * @param deleteColumn id
     * 相当于执行SQL：DELETE FROM t_user WHERE id IN(23, 13)，如果deleteValues是空，将不会删除任何数据
     */
    public static int delete(String tableName, String deleteColumn, String deleteValues) {
        if (StringUtils.isBlank(deleteValues)) {
            return 0;
        }
        return deleteData(tableName, deleteColumn, "IN", Arrays.asList(deleteValues.split(",")));
    }

    /**
     * 指定某字段删除数据
     * @param deleteValues [23,13]
     * @param tableName t_user
     * @param deleteColumn id
     * 相当于执行SQL：DELETE FROM t_user WHERE id IN(23, 13)
     */
    public static int delete(String tableName, String deleteColumn, Collection<?> deleteValues) {
        return deleteData(tableName, deleteColumn, SQL_PATH_IN, deleteValues);
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
        return deleteData(tableName, deleteColumn, SQL_PATH_NOT_IN, deleteValues);
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

    private static int deleteData(String tableName, String deleteColumn, String sqlPatch, Collection<?> deleteValues) {
        if (CollectionUtils.isEmpty(deleteValues)) {
            return 0;
        }

        int size = deleteValues.size();
        // 处理单值
        if (size == 1) {
            if (SQL_PATH_IN.equals(sqlPatch)) {
                return delete(tableName, deleteValues.toArray(), deleteColumn + " = ?");
            } else if (SQL_PATH_NOT_IN.equals(sqlPatch)) {
                return delete(tableName, deleteValues.toArray(), deleteColumn + " <> ?");
            }
        }

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
            String deleteSQL = String.format("DELETE FROM " + tableName + " WHERE " + deleteColumn + " " + sqlPatch + "%s", inSql);
            deletedCount += SQLUtils.JDBC_TEMPLATE.update(deleteSQL, currentDeleteValue);
            if (log.isDebugEnabled()) {
                log.debug("SQL=> [{}], args:=> [{}]", deleteSQL, currentDeleteValue);
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
                StringUtils.join(Collections.nCopies(columnNames.split("\\s*,\\s*").length, "?"), ","));
    }

    private static String getUpdateSQL(String tableName, String columnNames, String conditionSQL) {
        return "UPDATE " + tableName + " SET " + StringUtils.join(columnNames.split(",\\s*"), " = ?,") + " = ? WHERE " + conditionSQL;
    }

}
