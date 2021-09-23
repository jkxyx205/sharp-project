package com.rick.db.service;

import com.google.common.base.CaseFormat;
import com.google.common.collect.Maps;
import com.rick.common.util.ClassUtils;
import com.rick.db.plugin.SQLUtils;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author Rick
 * @createdAt 2021-09-23 16:41:00
 */
public class TableServiceImpl<T> {

    private final SharpService sharpService;

    private final JdbcTemplate jdbcTemplate;

    private final String tableName;

    /**
     * column1, column2, column3
     */
    private final String columnNames;

    private final String[] columnArray;

    public TableServiceImpl(SharpService sharpService, String tableName, String columnNames) {
        this.sharpService = sharpService;
        this.jdbcTemplate = sharpService.getNamedJdbcTemplate().getJdbcTemplate();
        this.tableName = tableName;
        this.columnNames = columnNames;
        this.columnArray = columnNames.split(",\\s*");
    }

    public void save(Object[] params) {
        SQLUtils.insert(tableName, columnNames, params);
    }

    /**
     * 批量持久化
     */
    public void saveAll(List<Object[]> paramsList) {
        SQLUtils.insert(tableName, columnNames, paramsList);
    }

    /**
     * 通过主鍵刪除
     *
     * @param id
     */
    public void deleteById(Serializable id) {
        SQLUtils.deleteById(tableName, id);
    }

    /**
     * 批量刪除 eg：ids -> “1,2,3,4”
     *
     * @param ids
     */
    public void deleteByIds(String ids) {
        SQLUtils.deleteByIn(tableName, "id", ids);
    }

    public void deleteByIds(Collection<?> ids) {
        SQLUtils.deleteByIn(tableName, "id", ids);
    }

    /**
     * 更新所有字段
     * @param params
     * @param id
     */
    public void update(Object[] params, Serializable id) {
        SQLUtils.update(tableName, columnNames, params, id);
    }

    /**
     * 指定更新字段
     * @param params
     * @param id
     */
    public void update(String updateColumnNames, Object[] params, Serializable id) {
        SQLUtils.update(tableName, updateColumnNames, params, id);
    }

    /**
     * 通过ID查找
     *
     * @param id
     * @return
     */
    public Optional<T> findById(Serializable id) {
        List<T> list = (List<T>) jdbcTemplate.query(getSelectSQL() + " WHERE id = ?",
                new BeanPropertyRowMapper<>(getActualTypeArgument()), new Object[]{id});
        return Optional.ofNullable(list.size() == 1 ? list.get(0) : null);
    }

    /**
     * 通过多个ID查找//eg：ids -> “1,2,3,4”
     *
     * @param ids
     * @return
     */
    public List<T> findByIds(String ids) {
        Map<String, Object> params = Maps.newHashMapWithExpectedSize(1);
        params.put("ids", ids);
        return findByIds(params);
    }

    public List<T> findByIds(Collection<?> ids) {
        Map<String, Object> params = Maps.newHashMapWithExpectedSize(1);
        params.put("ids", ids);
        return findByIds(params);
    }

    /**
     * 根据条件查找
     *
     * @param
     * @return
     */
    public List<T> findByParams(Map<String, Object> params) {
        return (List<T>) sharpService.query(getSelectSQL() + " WHERE " + getConditionSQL(),
                params,
                getActualTypeArgument());
    }

    /**
     * 获取所有
     *
     * @return
     */
    public List<T> findAll() {
        return (List<T>) jdbcTemplate.query(getSelectSQL(), new BeanPropertyRowMapper<>(getActualTypeArgument()));
    }

    private Class<?> getActualTypeArgument() {
        return ClassUtils.getActualTypeArgument(this.getClass())[0];
    }

    private String getSelectSQL() {
        return "SELECT " + columnNames + " FROM " + tableName;
    }

    private String getConditionSQL() {
        StringBuilder sb = new StringBuilder();
        for (String columnName : columnArray) {
            sb.append(columnName).append(" = :").append(CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, columnName)).append(" AND ");
        }

        return sb.substring(0, sb.length() - 5);
    }

    private List<T> findByIds(Map<String, Object> params) {
        return (List<T>) sharpService.query(getSelectSQL() + " WHERE id IN(:ids)",
                params,
                getActualTypeArgument());
    }
}
