package com.rick.db.plugin;

import com.rick.db.repository.EntityDAO;
import com.rick.db.repository.TableDAO;
import com.rick.db.repository.model.EntityId;
import com.rick.db.repository.support.TableMeta;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author Rick.Xu
 * @date 2025/8/15 19:21
 */
@RequiredArgsConstructor
public class BaseServiceImpl<D extends EntityDAO<T, ID>, T extends EntityId<ID>, ID> implements EntityDAO<T, ID> {

    @Getter
    protected final D baseDAO;


    @Override
    public Optional<T> selectById(ID id) {
        return baseDAO.selectById(id);
    }

    @Override
    public List<T> selectByIds(Collection<ID> ids) {
        return baseDAO.selectByIds(ids);
    }

    @Override
    public List<T> selectAll() {
        return baseDAO.selectAll();
    }

    @Override
    public <K, V> Map<K, V> selectForKeyValue(String columns, String condition, Map<String, ?> paramMap) {
        return baseDAO.selectForKeyValue(columns, condition, paramMap);
    }

    @Override
    public List<T> select(String condition, Object... args) {
        return baseDAO.select(condition, args);
    }

    @Override
    public List<T> select(Map<String, ?> paramMap) {
        return baseDAO.select(paramMap);
    }

    @Override
    public List<T> select(String condition, Map<String, ?> paramMap) {
        return baseDAO.select(condition, paramMap);
    }

    @Override
    public List<T> selectWithColumns(String columns, String condition, Object... args) {
        return baseDAO.selectWithColumns(columns, condition, args);
    }

    @Override
    public <E> List<E> select(Class<E> clazz, String columns, String condition, Object... args) {
        return baseDAO.select(clazz, columns, condition, args);
    }

    @Override
    public <E> List<E> selectWithoutCascadeSelect(Class<E> clazz, String columns, String condition, Object... args) {
        return baseDAO.selectWithoutCascadeSelect(clazz, columns, condition, args);
    }

    @Override
    public List<T> select(String columns, String condition, Map<String, ?> paramMap) {
        return baseDAO.select(columns, condition, paramMap);
    }

    @Override
    public List<T> select(T example) {
        return baseDAO.select(example);
    }

    @Override
    public List<T> select(String condition, T example) {
        return baseDAO.select(condition, example);
    }

    @Override
    public List<T> select(String columns, String condition, T example) {
        return baseDAO.select(columns, condition, example);
    }

    @Override
    public <E> Optional<E> selectOne(Class<E> clazz, String columns, String condition, T example) {
        return baseDAO.selectOne(clazz, columns, condition, example);
    }

    @Override
    public <E> List<E> select(Class<E> clazz, String columns, String condition, T example) {
        return baseDAO.select(clazz, columns, condition, example);
    }

    @Override
    public <E> List<E> select(Class<E> clazz, String columns, String condition, Map<String, ?> paramMap) {
        return baseDAO.select(clazz, columns, condition, paramMap);
    }

    @Override
    public <E> List<E> selectWithoutCascadeSelect(Class<E> clazz, String columns, String condition, Map<String, ?> paramMap) {
        return baseDAO.selectWithoutCascadeSelect(clazz, columns, condition, paramMap);
    }

    @Override
    public <K, V> Map<K, V> selectWithoutCascadeSelect(String columns, String condition, Map<String, ?> paramMap) {
        return baseDAO.selectWithoutCascadeSelect(columns, condition, paramMap);
    }

    @Override
    public Boolean exists(String condition, Object... args) {
        return baseDAO.exists(condition, args);
    }

    @Override
    public Boolean exists(String condition, T example) {
        return baseDAO.exists(condition, example);
    }

    @Override
    public int deleteById(ID id) {
        return baseDAO.deleteById(id);
    }

    @Override
    public int deleteByIds(Collection<ID> ids) {
        return baseDAO.deleteByIds(ids);
    }

    @Override
    public int deleteAll() {
        return baseDAO.deleteAll();
    }

    @Override
    public int delete(String condition, Object... args) {
        return baseDAO.delete(condition, args);
    }

    @Override
    public int delete(String condition, Map<String, ?> paramMap) {
        return baseDAO.delete(condition, paramMap);
    }

    @Override
    public T insert(T entity) {
        return baseDAO.insert(entity);
    }

    @Override
    public T insertOrUpdate(Map<String, Object> paramMap) {
        return baseDAO.insertOrUpdate(paramMap);
    }

    @Override
    public T update(T entity) {
        return baseDAO.update(entity);
    }

    @Override
    public T insertOrUpdate(T entity) {
        return baseDAO.insertOrUpdate(entity);
    }

    @Override
    public Collection<T> insertOrUpdate(Collection<T> entityList) {
        return baseDAO.insertOrUpdate(entityList);
    }

    @Override
    public Collection<T> insertOrUpdate(Collection<T> entityList, String refColumnName, Object refValue) {
        return baseDAO.insertOrUpdate(entityList, refColumnName, refValue);
    }

    @Override
    public int updateById(String columns, ID id, Object... args) {
        return baseDAO.updateById(columns, id, args);
    }

    @Override
    public int update(String columns, String condition, Object... args) {
        return baseDAO.update(columns, condition, args);
    }

    @Override
    public int update(String columns, String condition, Map<String, ?> paramMap) {
        return baseDAO.update(columns, condition, paramMap);
    }

    @Override
    public int updateById(String columns, ID id, Map<String, ?> paramMap) {
        return baseDAO.updateById(columns, id, paramMap);
    }

    @Override
    public int updateByIds(String columns, Collection<ID> ids, Map<String, ?> paramMap) {
        return baseDAO.updateByIds(columns, ids, paramMap);
    }

    @Override
    public TableMeta getTableMeta() {
        return baseDAO.getTableMeta();
    }

    @Override
    public TableDAO getTableDAO() {
        return baseDAO.getTableDAO();
    }

    @Override
    public Map<String, Object> entityToMap(T entity) {
        return baseDAO.entityToMap(entity);
    }
}
