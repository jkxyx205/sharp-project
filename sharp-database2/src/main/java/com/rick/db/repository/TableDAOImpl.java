package com.rick.db.repository;

import com.rick.common.util.JsonUtils;
import com.rick.common.util.ObjectUtils;
import com.rick.db.util.OperatorUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.convert.ConversionService;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.jdbc.core.*;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
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

    @Resource
    @Qualifier("dbConversionService")
    private ConversionService conversionService;

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
                        value = JdbcUtils.getResultSetValue(rs, index, pd.getPropertyType()) ;
                        if (pd.getPropertyType() == List.class && value == null) {
                            value = Collections.emptyList();
                        } else if (value.getClass().getName().equals("org.postgresql.util.PGobject")) {
                            try {
                                Method getValue = value.getClass().getMethod("getValue");
                                value = getValue.invoke(value);
                            } catch (Exception e) {
                                throw new RuntimeException("Failed to get PGobject value", e);
                            }
                        }/*else if (value instanceof PGobject) {
                            value = ((PGobject)value).getValue();
                        }*/

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
