package com.rick.db.service;

import com.google.common.base.CaseFormat;
import com.google.common.base.Converter;
import com.rick.common.http.convert.*;
import com.rick.db.formatter.AbstractSqlFormatter;
import com.rick.db.plugin.dao.support.IdToEntityConverterFactory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.util.CollectionUtils;

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
    protected AbstractSqlFormatter sqlFormatter;

    private static final Converter<String, String> converter = CaseFormat.LOWER_UNDERSCORE.converterTo(CaseFormat.LOWER_CAMEL);

    @Autowired(required = false)
    private List<ConverterFactory> converterFactories = Collections.emptyList();

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
            log.debug("SQL=> [{}], args:=> [{}]", sqlFormatter.formatSql, sqlFormatter.paramMap);
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
            log.debug("SQL=> [{}], args:=> [{}]", sqlFormatter.formatSql, sqlFormatter.paramMap);
        }
        return namedJdbcTemplate.update(sqlFormatter.formatSql, sqlFormatter.paramMap);
    }

    private Optional handleResult(List<?> list) {
        if (CollectionUtils.isEmpty(list)) {
            return Optional.empty();
        }

        int size = list.size();
        if (size > 1) {
            throw new IncorrectResultSizeDataAccessException(1, size);
        }

        return Optional.of(list.get(0));
    }

    protected <T> List<T> toClass(NamedParameterJdbcTemplate jdbcTemplate, String sql, Map<String, ?> paramMap, Class<T> clazz) {
        if (clazz == String.class || Number.class.isAssignableFrom(clazz) || Character.class == clazz || Boolean.class == clazz) {
            return jdbcTemplate.queryForList(sql, paramMap, clazz);
        }

        if (Map.class.isAssignableFrom(clazz)) {
            return (List<T>) query(sql, paramMap);
        }

        // 添加嵌套查询
        NestedRowMapper<T> beanPropertyRowMapper = new NestedRowMapper<>(clazz);

//        BeanPropertyRowMapper<T> beanPropertyRowMapper = new BeanPropertyRowMapper<T>(clazz);
//        DefaultConversionService defaultConversionService = (DefaultConversionService) beanPropertyRowMapper.getConversionService();
//        customerConversion(defaultConversionService);

        List<T> query = jdbcTemplate.query(sql, paramMap, beanPropertyRowMapper);
        return query;
    }

    private class NestedRowMapper<T> implements RowMapper<T> {

        private Class<T> mappedClass;



        public NestedRowMapper(Class<T> mappedClass) {
           this.mappedClass = mappedClass;
        }

        @Override
        public T mapRow(ResultSet rs, int rowNum) throws SQLException {
            T mappedObject = BeanUtils.instantiateClass(this.mappedClass);
            BeanWrapper bw = PropertyAccessorFactory.forBeanPropertyAccess(mappedObject);
            bw.setAutoGrowNestedPaths(true);
            bw.setConversionService(DefaultConversionService.getSharedInstance());
            customerConversion((DefaultConversionService)bw.getConversionService());

            ResultSetMetaData rsmd = rs.getMetaData();
            int columnCount = rsmd.getColumnCount();

            for (int index = 1; index <= columnCount; index++) {
                String column = JdbcUtils.lookupColumnName(rsmd, index);
                try {
                    Object value = JdbcUtils.getResultSetValue(rs, index, Class.forName(rsmd.getColumnClassName(index)));
                    bw.setPropertyValue(camelCaseName(column), value);
                } catch (TypeMismatchException | NotWritablePropertyException | ClassNotFoundException e) {
//                    log.warn("Unable to map column '" + column + "' to property");
                    throw new DataRetrievalFailureException("Unable to map column '" + column + "' to property", e);
                }
            }

            return mappedObject;
        }

        private String camelCaseName(String name) {
            return name.indexOf("_") > -1 ? converter.convert(name) : name;
        }
    }

    protected List<Map<String, Object>> toMap(NamedParameterJdbcTemplate jdbcTemplate, String sql, Map<String, ?> paramMap) {
        return jdbcTemplate.queryForList(sql, paramMap);
    }

    private SqlFormatter getSQLFormatter(String sql, Map<String, ?> params) {
        Map<String, Object> paramMap = new HashMap<>();
        String formatSql = sqlFormatter.formatSql(sql, params, paramMap);
        return new SqlFormatter(formatSql, paramMap);
    }

    private void customerConversion(DefaultConversionService defaultConversionService) {
        for (ConverterFactory converterFactory : converterFactories) {
            defaultConversionService.addConverterFactory(converterFactory);
        }

        // 内置转换器
        defaultConversionService.addConverterFactory(new CodeToEnumConverterFactory());
        defaultConversionService.addConverter(new JsonStringToListMapConverter());
        defaultConversionService.addConverterFactory(new JsonStringToObjectConverterFactory());
        defaultConversionService.addConverterFactory(new JsonStringToMapConverterFactory());
        defaultConversionService.addConverter(new JsonStringToCollectionConverter());
        defaultConversionService.addConverter(new JsonStringToSetMapConverter());
        defaultConversionService.addConverterFactory(new IdToEntityConverterFactory());
    }

    @AllArgsConstructor
    @Getter
    private class SqlFormatter {

        private String formatSql;

        private Map<String, ?> paramMap;
    }

}
