package com.rick.db.service;

import com.rick.common.util.JsonUtils;
import com.rick.common.util.ObjectUtils;
import com.rick.common.util.StringUtils;
import com.rick.db.formatter.AbstractSqlFormatter;
import com.rick.db.util.OptionalUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.convert.ConversionService;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlTypeValue;
import org.springframework.jdbc.core.StatementCreatorUtils;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.JdbcUtils;

import java.beans.PropertyDescriptor;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

/**
 *
 * @author rick
 * @date 2018/3/16
 */
@Slf4j
public class SharpService {

    @Autowired
    @Getter
    private NamedParameterJdbcTemplate namedJdbcTemplate;

    @Autowired
    @Getter
    private AbstractSqlFormatter sqlFormatter;

    @Autowired
    @Qualifier("dbConversionService")
    private ConversionService conversionService;

    public interface JdbcTemplateCallback<T> {
        List<T> query(NamedParameterJdbcTemplate jdbcTemplate, String sql, Map<String, ?> paramMap);
    }

    public List<Map<String, Object>> query(String sql, Map<String, ?> params) {
        return query(sql, params, (jdbcTemplate, sql2, paramMap) -> toMap(jdbcTemplate, sql2, paramMap));
    }

    public <T> List<T> query(String sql, Map<String, ?> params, Class<T> clazz) {
        return query(sql, params, (jdbcTemplate, sql2, paramMap) -> toClass(jdbcTemplate, sql2, paramMap, clazz));
    }

    public <T> List<T> query(String sql, Map<String, ?> params, JdbcTemplateCallback<T> jdbcTemplateCallback) {
        //忽略空值参数，由SharpService处理  否则会报org.springframework.dao.InvalidDataAccessApiUsageException: No value supplied for the SQL parameter
        SqlFormatter sqlFormatter = getSQLFormatter(sql, params);

        if (log.isDebugEnabled()) {
            log.debug("SQL => [{}], args:=> [{}]", sqlFormatter.formatSql, sqlFormatter.paramMap);
        }

        return jdbcTemplateCallback.query(namedJdbcTemplate, sqlFormatter.formatSql, sqlFormatter.paramMap);
    }

    /**
     * 取前两列，忽略其他列
     * 第一列做key
     * 第二列做Value
     * @param sql
     * @param params
     * @return
     */
    public <K, V> Map<K, V> queryForKeyValue(String sql, Map<String, ?> params) {
        final Map<K, V> m = new LinkedHashMap();
        query(sql, params, (jdbcTemplate, sql2, args) -> {
            jdbcTemplate.query(sql2, args, rs -> {
                m.put((K) rs.getObject(1), (V) rs.getObject(2));
            });

            return null;
        });

        return m;
    }

    public Optional<Map<String, Object>> queryForObject(String sql, Map<String, ?> params) {
        List<Map<String, Object>> list = query(sql, params);
        return handleResult(list);
    }

    /**
     * 查找单个对象
     *
     * @param sql
     * @param params
     * @param clazz  POJO Map 基本数据类型(String, Integer, Long...)
     * @return
     * @throws IncorrectResultSizeDataAccessException 结果大于一个抛出异常
     */
    public <T> Optional<T> queryForObject(String sql, Map<String, ?> params, Class<T> clazz) {
        List<T> list = query(sql, params, clazz);
        return handleResult(list);
    }

    /**
     * 根据查询sql获取count
     * @param querySql
     * @param params
     * @return
     */
    public long queryCountFromQuerySql(String querySql, Map<String, ?> params) {
        return query(sqlFormatter.formatSqlCount(querySql), params, Long.class).get(0);
    }

    public int update(String sql, Map<String, ?> params) {
        SqlFormatter sqlFormatter = getSQLFormatter(sql, params);
        if (log.isDebugEnabled()) {
            log.debug("SQL => [{}], args:=> [{}]", sqlFormatter.formatSql, sqlFormatter.paramMap);
        }
        return namedJdbcTemplate.update(sqlFormatter.formatSql, sqlFormatter.paramMap);
    }

    private Optional handleResult(List<?> list) {
        return OptionalUtils.expectedAsOptional(list);
    }

    <T> List<T> toClass(NamedParameterJdbcTemplate jdbcTemplate, String sql, Map<String, ?> paramMap, Class<T> clazz) {
        if (clazz == String.class || Number.class.isAssignableFrom(clazz) || Character.class == clazz || Boolean.class == clazz || clazz.isEnum()) {
            return jdbcTemplate.queryForList(sql, paramMap, clazz);
        }

        if (Map.class.isAssignableFrom(clazz)) {
            return (List<T>) query(sql, paramMap);
        }

        // 添加嵌套查询
        RowMapper<T> beanPropertyRowMapper = new NestedRowMapper<>(clazz);
        List<T> query = jdbcTemplate.query(sql, paramMap, beanPropertyRowMapper);
        return query;
    }

    List<Map<String, Object>> toMap(NamedParameterJdbcTemplate jdbcTemplate, String sql, Map<String, ?> paramMap) {
        return jdbcTemplate.queryForList(sql, paramMap);
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
                String propertyName = StringUtils.stringToCamel(column);
                PropertyDescriptor pd = (this.mappedFields != null ? this.mappedFields.get(lowerCaseName(propertyName)) : null);

                if (pd != null) {
                    Object value = null;
                    try {
                        value = JdbcUtils.getResultSetValue(rs, index, pd.getPropertyType()) ;
                        if (pd.getPropertyType() == List.class && value == null) {
                            value = Collections.emptyList();
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

    private SqlFormatter getSQLFormatter(String sql, Map<String, ?> params) {
        Map<String, Object> paramMap = new HashMap<>();
        String formatSql = sqlFormatter.formatSql(sql, params, paramMap);
        return new SqlFormatter(formatSql, paramMap);
    }

    @AllArgsConstructor
    @Getter
    private class SqlFormatter {

        private String formatSql;

        private Map<String, ?> paramMap;
    }

}
