package com.rick.db.repository;

import com.rick.common.util.JsonUtils;
import com.rick.common.util.Maps;
import com.rick.common.util.ObjectUtils;
import com.rick.db.repository.support.SqlHelper;
import com.rick.db.util.OperatorUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.convert.ConversionService;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.jdbc.core.*;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static com.rick.db.repository.support.Constants.COLUMN_NAME_SEPARATOR_REGEX;

/**
 * @author Rick.Xu
 * @date 2025/8/20 10:55
 */
@Repository
@RequiredArgsConstructor
public class TableDAOImpl implements TableDAO {

    @Getter
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Resource
    @Qualifier("dbConversionService")
    private ConversionService conversionService;

    @Override
    public boolean exists(String sql, Object... args) {
        return select(sql + " LIMIT 1", args).size() > 0;
    }

    @Override
    public boolean exists(String sql, Map<String, Object> paramMap) {
        return select(sql + " LIMIT 1", paramMap).size() > 0;
    }

    @Override
    public int update(String tableName, String columns, String condition, Object... args) {
        return namedParameterJdbcTemplate.getJdbcTemplate().update("UPDATE " + tableName + " SET " + columns + SqlHelper.buildWhere(condition), args);
    }

    @Override
    public int update(String tableName, String columns, String condition, Map<String, Object> paramMap) {
        return namedParameterJdbcTemplate.update("UPDATE " + tableName + " SET " + columns + SqlHelper.buildWhere(condition), paramMap);
    }

    @Override
    public int deleteIn(String tableName, String deleteColumn, Collection<?> deleteValues) {
        return delete(tableName, deleteColumn+ " IN(:params)", Maps.of("params", deleteValues));
    }

    @Override
    public int deleteNotIn(String tableName, String deleteColumn, Collection<?> deleteValues) {
        return delete(tableName, deleteColumn+ " NOT IN(:params)", Maps.of("params", deleteValues));
    }

    @Override
    public int delete(String tableName, String condition, Object... args) {
        return namedParameterJdbcTemplate.getJdbcTemplate().update("DELETE FROM "+ tableName + SqlHelper.buildWhere(condition), args);
    }

    @Override
    public int delete(String tableName, String condition, Map<String, Object> paramMap) {
        return namedParameterJdbcTemplate.update("DELETE FROM "+ tableName + SqlHelper.buildWhere(condition), paramMap);
    }

    @Override
    public int insert(String tableName, String columnNames, Object... args) {
        String[] columns = columnNames.split("\\s*,\\s*"); // 按逗号分隔并去掉空格

        if (columns.length != args.length) {
            throw new IllegalArgumentException("列名数量与参数数量不一致");
        }

        Map<String, Object> paramMap = new HashMap<>();
        for (int i = 0; i < columns.length; i++) {
            paramMap.put(columns[i], args[i]);
        }

        return insert(tableName, columnNames, paramMap);
    }

    @Override
    public int insert(String tableName, String columnNames, Map<String, Object> paramMap) {
//        String[] columnNameArr = columnNames.split(COLUMN_NAME_SEPARATOR_REGEX);
//        Object[] params = new Object[columnNameArr.length];
//
//        for (int i = 0; i < columnNameArr.length; i++) {
//            params[i] = paramMap.get(columnNameArr[i]);
//        }
//
//        String insertSQL = getInsertSQL(tableName, columnNames);
//
//        return namedParameterJdbcTemplate.getJdbcTemplate().update(insertSQL, params);
        return new SimpleJdbcInsert(namedParameterJdbcTemplate.getJdbcTemplate()).withTableName(tableName)
                .usingColumns(columnNames.split(COLUMN_NAME_SEPARATOR_REGEX))
                .execute(paramMap);
    }

//    /**
//     *
//     * @param tableName t_xx
//     * @param columnNames id, namme
//     * @return INSERT INTO t_xx(id, namme) VALUES(?, ?)
//     */
//    private static String getInsertSQL(String tableName, String columnNames) {
//        return String.format("INSERT INTO %s(%s) VALUES(%s)",
//                tableName,
//                columnNames,
//                StringUtils.join(Collections.nCopies(columnNames.split(COLUMN_NAME_SEPARATOR_REGEX).length, "?"), ","));
//    }

    @Override
    public Number insertAndReturnKey(String tableName, String columnNames, Map<String, Object> params, String... idColumnName) {
        Set<String> toRemove = new HashSet<>(Arrays.asList(idColumnName));
        return new SimpleJdbcInsert(namedParameterJdbcTemplate.getJdbcTemplate()).withTableName(tableName)
                .usingGeneratedKeyColumns(idColumnName)
                .usingColumns(Arrays.stream(columnNames.split(COLUMN_NAME_SEPARATOR_REGEX))
                        .filter(s -> !toRemove.contains(s))
                        .toArray(String[]::new))
                .executeAndReturnKey(params);
    }

    @Override
    public void execute(String sql) {
        namedParameterJdbcTemplate.getJdbcTemplate().execute(sql);
    }

    /**
     * 获取新的 connection 连接
     * @param action
     * @return
     * @param <T>
     */
    @Override
    public <T> T execute(ConnectionCallback<T> action) {
        DataSource dataSource = namedParameterJdbcTemplate.getJdbcTemplate().getDataSource();
        Connection con = null;
        try {
//            con = DataSourceUtils.getConnection(dataSource); // 从 spring 上下文中获取连接
            con = dataSource.getConnection();
            return action.doInConnection(con);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (con != null) {
                DataSourceUtils.releaseConnection(con, dataSource);
            }
        }

        return null;
    }

    @Override
    public List<Map<String, Object>> select(String sql, Object... args) {
        return namedParameterJdbcTemplate.getJdbcTemplate().query(sql, new ColumnMapRowMapper(), args);
    }

    @Override
    public <E> List<E> select(Class<E> clazz, String sql, Object... args) {
        return namedParameterJdbcTemplate.getJdbcTemplate().query(sql, determineRowMapper(clazz), args);
    }

    @Override
    public <E> List<E> select(Class<E> clazz, String sql, Map<String, Object> paramMap) {
        return select(sql, paramMap, (jdbcTemplate, sql2, paramMap2) -> namedParameterJdbcTemplate.query(sql2, paramMap2, determineRowMapper(clazz)));
    }

    @Override
    public <K, V> Map<K, V> selectForKeyValue(String sql, Object... args) {
        List<Map<String, Object>> list = select(sql, args);
        final Map<K, V> m = new LinkedHashMap();
        if (CollectionUtils.isNotEmpty(list)) {
            for (Map<String, Object> map : list) {
                m.putAll((Map<? extends K, ? extends V>) map);
            }
        }
        return m;
    }

    @Override
    public List<Map<String, Object>> select(String sql, Map<String, Object> paramMap) {
        return select(sql, paramMap, (jdbcTemplate, sql2, paramMap2) -> namedParameterJdbcTemplate.query(sql2, paramMap2, new ColumnMapRowMapper()));
    }

    @Override
    public <E> List<E> select(String sql, Map<String, Object> paramMap, JdbcTemplateCallback<E> jdbcTemplateCallback) {
        return jdbcTemplateCallback.select(namedParameterJdbcTemplate, sql, paramMap);
    }

    @Override
    public <K, V> Map<K, V> selectForKeyValue(String sql, Map<String, Object> paramMap) {
        final Map<K, V> m = new LinkedHashMap();
        select(sql, paramMap, (jdbcTemplate, sql2, args) -> {
            jdbcTemplate.query(sql2, args, rs -> {
                m.put((K) rs.getObject(1), (V) rs.getObject(2));
            });

            return null;
        });

        return m;
    }

    @Override
    public Optional<Map<String, Object>> selectForObject(String sql, Object... args) {
        return OperatorUtils.expectedAsOptional(select(sql, args));
    }

    @Override
    public Optional<Map<String, Object>> selectForObject(String sql, Map<String, Object> paramMap) {
        return OperatorUtils.expectedAsOptional(select(sql, paramMap));
    }

    @Override
    public void updateRefTable(String refTableName, String keyColumn, String guestColumn, Object keyInstance, Collection<?> guestInstanceIds) {
        // 删除
        if (CollectionUtils.isEmpty(guestInstanceIds)) {
            String deleteSql = String.format("DELETE FROM %s WHERE %s = ?", refTableName, keyColumn);
            namedParameterJdbcTemplate.getJdbcTemplate().update(deleteSql, keyInstance);
            return;
        }

        Map<String, Object> deleteParams = com.google.common.collect.Maps.newHashMapWithExpectedSize(1);
        deleteParams.put("guestInstanceIds", guestInstanceIds);
        deleteParams.put("keyInstance", keyInstance);
        // 1. 删除
        String deleteSql = String.format("DELETE FROM %s WHERE %s = :keyInstance AND %s NOT IN (:guestInstanceIds)", refTableName, keyColumn, guestColumn);
        namedParameterJdbcTemplate.update(deleteSql, deleteParams);

        // 2. 插入新增
        // 2.1 库中
        String queryAllGuestInstanceIdsSQL = String.format("SELECT %s FROM %s WHERE %s = ?", guestColumn, refTableName, keyColumn);
        List<?> dbGuestInstanceIds = namedParameterJdbcTemplate.getJdbcTemplate().queryForList(queryAllGuestInstanceIdsSQL, guestInstanceIds.iterator().next().getClass(), keyInstance);
        // 2.2 新增
        List<?> newGuestInstanceIds = guestInstanceIds.stream().filter(guestId -> !dbGuestInstanceIds.contains(guestId)).collect(Collectors.toList());
        if (newGuestInstanceIds.size() == 0) {
            return;
        }

        String insertSQL = String.format("INSERT INTO %s(%s, %s) VALUES(?, ?)", refTableName, keyColumn, guestColumn);
        List<Object[]> addParams = newGuestInstanceIds.stream().map(guestInstanceId -> new Object[] {keyInstance, guestInstanceId}).collect(Collectors.toList());

        namedParameterJdbcTemplate.getJdbcTemplate().batchUpdate(insertSQL, addParams);
    }

    private <E> RowMapper determineRowMapper(Class<E> requiredType) {
        if (Map.class.isAssignableFrom(requiredType)) {
            return new ColumnMapRowMapper();
        }
        return ObjectUtils.mayPureObject(requiredType) ? new NestedRowMapper<>(requiredType) : new SingleColumnRowMapper(requiredType);
    }

    private <T extends Enum<T>> T getEnum(Class<?> enumClass, String name) {
        if (enumClass == null || name == null) {
            throw new IllegalArgumentException("Enum class and name cannot be null.");
        }
        Class<T> enumType = (Class<T>) enumClass;
        return Enum.valueOf(enumType, name);
    }

    private List<String> convertStringToList(String inputString) {
        // Handle null or empty input
        if (inputString == null || inputString.isEmpty() || !inputString.contains("[") || !inputString.contains("]")) {
            return Collections.emptyList();
        }

        // 1. Remove the brackets
        String content = inputString.substring(1, inputString.length() - 1);

        // Handle case where string is just "[]"
        if (content.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. Split by comma and an optional space
        // 3. Trim whitespace from each element
        // 4. Collect into a List
        return Arrays.stream(content.split(","))
                .map(String::trim)
                .collect(Collectors.toList());
    }

    private class NestedRowMapper<T> implements RowMapper<T> {

        private Class<T> mappedClass;

        private Map<String, PropertyDescriptor> mappedFields;

        public NestedRowMapper(Class<T> mappedClass) {
            this.mappedClass = mappedClass;
            this.mappedFields = new HashMap<>();
            List<String> propertyNames = new ArrayList<>();
            initMappedValues(mappedClass, null, propertyNames);
        }

        private void initMappedValues(Class<?> mappedClass, String propertyPrefix, List<String> propertyNames) {
            for (PropertyDescriptor pd : BeanUtils.getPropertyDescriptors(mappedClass)) {
                if (pd.getWriteMethod() != null) {
                    String propertyName = lowerCaseName((propertyPrefix == null ? "" : propertyPrefix + ".") + pd.getName());
                    this.mappedFields.put(propertyName, pd);
                    if (SqlTypeValue.TYPE_UNKNOWN == StatementCreatorUtils.javaTypeToSqlParameterType(pd.getPropertyType())) {
                        if (!propertyNames.contains(propertyName)) {
                            propertyNames.add(propertyName);
                            initMappedValues(pd.getPropertyType(), pd.getName(), propertyNames);
                        }
                    }
                }
            }
        }

        @Override
        public T mapRow(ResultSet rs, int rowNum) throws SQLException {
            T mappedObject = BeanUtils.instantiateClass(this.mappedClass);
            BeanWrapper bw = PropertyAccessorFactory.forBeanPropertyAccess(mappedObject);
            bw.setAutoGrowNestedPaths(true);
            bw.setConversionService(conversionService);

            ResultSetMetaData rsmd = rs.getMetaData();
            int columnCount = rsmd.getColumnCount();

            for (int index = 1; index <= columnCount; index++) {
                String column = JdbcUtils.lookupColumnName(rsmd, index);
                String propertyName = com.rick.common.util.StringUtils.stringToCamel(column);
                PropertyDescriptor pd = (this.mappedFields != null ? this.mappedFields.get(lowerCaseName(propertyName)) : null);

                if (pd != null) {
                    Object value = null;
                    try {
                        value = JdbcUtils.getResultSetValue(rs, index, pd.getPropertyType());
                        if (pd.getPropertyType() == List.class && value == null) {
                            value = Collections.emptyList();
                        }

                        if (Objects.nonNull(value)) {
                            if (value.getClass().getName().equals("org.postgresql.util.PGobject")) {
                                try {
                                    Method getValue = value.getClass().getMethod("getValue");
                                    value = getValue.invoke(value);
                                } catch (Exception e) {
                                    throw new RuntimeException("Failed to get PGobject value", e);
                                }
                            }/*else if (value instanceof PGobject) {
                            value = ((PGobject)value).getValue();
                             }*/
                        }

                        bw.setPropertyValue(propertyName, value);
                    } catch (TypeMismatchException | NotWritablePropertyException e) {
                        if (ObjectUtils.mayPureObject(pd.getPropertyType())) {
                            if (value != null && value instanceof String) {
                                bw.setPropertyValue(propertyName, JsonUtils.toObject((String) value, pd.getPropertyType()));
                            }
                        } else {
                            throw new DataRetrievalFailureException("Unable to map column '" + column + "' to property " + pd.getName(), e);
                        }
                    }
                }
            }

            return mappedObject;
        }

        protected String lowerCaseName(String name) {
            return name.toLowerCase(Locale.US);
        }
    }
}
