package com.rick.db.repository;

import com.rick.common.util.ClassUtils;
import com.rick.common.util.ObjectUtils;
import com.rick.db.util.OperatorUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.beans.*;
import org.springframework.jdbc.core.*;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import java.beans.PropertyDescriptor;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Rick.Xu
 * @date 2025/8/20 10:55
 */
@Repository
@RequiredArgsConstructor
public class TableDAOImpl implements TableDAO {

    @Getter
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    public Boolean exists(String sql, Object... args) {
        return select(String.class, sql + " LIMIT 1", args).size() > 0 ? true : false;
    }

    @Override
    public Boolean exists(String sql, Map<String, ?> paramMap) {
        return select(String.class, sql + " LIMIT 1", paramMap).size() > 0 ? true : false;
    }

    @Override
    public int update(String tableName, String columns, String condition, Object... args) {
        return namedParameterJdbcTemplate.getJdbcTemplate().update("UPDATE " + tableName + " SET " + columns + (StringUtils.isBlank(condition) ? "" : " WHERE " + condition), args);
    }

    @Override
    public int update(String tableName, String columns, String condition, Map<String, ?> paramMap) {
        return namedParameterJdbcTemplate.update("UPDATE " + tableName + " SET " + columns + (StringUtils.isBlank(condition) ? "" : " WHERE " + condition), paramMap);
    }

    @Override
    public int delete(String tableName, String condition, Object... args) {
        return namedParameterJdbcTemplate.getJdbcTemplate().update("DELETE FROM "+ tableName + (StringUtils.isBlank(condition) ? "" : " WHERE " + condition), args);
    }

    @Override
    public int delete(String tableName, String condition, Map<String, ?> paramMap) {
        return namedParameterJdbcTemplate.update("DELETE FROM "+ tableName + (StringUtils.isBlank(condition) ? "" : " WHERE " + condition), paramMap);
    }

    @Override
    public int insert(String tableName, Map<String, Object> paramMap) {
        return new SimpleJdbcInsert(namedParameterJdbcTemplate.getJdbcTemplate()).withTableName(tableName)
                .execute(paramMap);
    }

    @Override
    public Number insertAndReturnKey(String tableName, Map<String, Object> params, String... idColumnName) {
        return new SimpleJdbcInsert(namedParameterJdbcTemplate.getJdbcTemplate()).withTableName(tableName)
                .usingGeneratedKeyColumns(idColumnName)
                .executeAndReturnKey(params);
    }

    @Override
    public void execute(String sql) {
        namedParameterJdbcTemplate.getJdbcTemplate().execute(sql);
    }

    @Override
    public <E> List<E> select(Class<E> clazz, String sql, Object... args) {
        return namedParameterJdbcTemplate.getJdbcTemplate().query(sql, determineRowMapper(clazz), args);
    }

    @Override
    public <E> List<E> select(Class<E> clazz, String sql, Map<String, ?> paramMap) {
        return select(sql, paramMap, (jdbcTemplate, sql2, paramMap2) -> namedParameterJdbcTemplate.query(sql2, paramMap2, determineRowMapper(clazz)));
    }

    @Override
    public List<Map<String, Object>> select(String sql, Map<String, ?> paramMap) {
        return select(sql, paramMap, (jdbcTemplate, sql2, paramMap2) -> namedParameterJdbcTemplate.query(sql2, paramMap2, new ColumnMapRowMapper()));
    }

    @Override
    public <E> List<E> select(String sql, Map<String, ?> paramMap, JdbcTemplateCallback<E> jdbcTemplateCallback) {
        return jdbcTemplateCallback.select(namedParameterJdbcTemplate, sql, paramMap);
    }

    @Override
    public <K, V> Map<K, V> selectForKeyValue(String sql, Map<String, ?> params) {
        final Map<K, V> m = new LinkedHashMap();
        select(sql, params, (jdbcTemplate, sql2, args) -> {
            jdbcTemplate.query(sql2, args, rs -> {
                m.put((K) rs.getObject(1), (V) rs.getObject(2));
            });

            return null;
        });

        return m;
    }

    @Override
    public Optional<Map<String, Object>> selectForObject(String sql, Map<String, ?> paramMap) {
        return OperatorUtils.expectedAsOptional(select(sql, paramMap));
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

            ResultSetMetaData rsmd = rs.getMetaData();
            int columnCount = rsmd.getColumnCount();

            for (int index = 1; index <= columnCount; index++) {
                String propertyName = org.springframework.jdbc.support.JdbcUtils.lookupColumnName(rsmd, index);
                PropertyDescriptor pd = (this.mappedFields != null ? this.mappedFields.get(lowerCaseName(propertyName)) : null);

                if (pd != null) {
                    Object value;
                    try {
                        value = org.springframework.jdbc.support.JdbcUtils.getResultSetValue(rs, index, pd.getPropertyType()) ;

                        if (List.class.isAssignableFrom(pd.getPropertyType())) {
                            List<String> items = convertStringToList((String) value);

                            if (CollectionUtils.isEmpty(items)) {
                                value = Collections.emptyList();
                            } else {
                                Class<?> clazz = ClassUtils.getFieldGenericClass(FieldUtils.getDeclaredField(mappedClass, propertyName, true))[0];
                                if (clazz.isEnum()) {
                                    value = items.stream().map(v -> getEnum(clazz, v)).collect(Collectors.toList());
                                } else {
                                    value = items;
                                }
                            }
                        }

                        EntityDAO entityDAO = EntityDAOManager.getDAO(pd.getPropertyType());
                        if (Objects.nonNull(entityDAO)) {
                            bw.setPropertyValue(propertyName + "." + entityDAO.getTableMeta().getIdMeta().getIdPropertyName(), value);
                        } else {
                            bw.setPropertyValue(propertyName, value);
                        }
                    } catch (TypeMismatchException | NotWritablePropertyException e) {
                        throw e;
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
