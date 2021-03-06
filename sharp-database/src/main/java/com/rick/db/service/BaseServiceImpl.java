package com.rick.db.service;

import com.rick.common.http.exception.Assert;
import com.rick.db.dto.BaseEntity;
import com.rick.db.plugin.dao.core.BaseDAO;
import com.rick.db.service.support.Params;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ReflectionUtils;

import javax.validation.Valid;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @author Rick
 * @createdAt 2022-04-09 10:10:00
 */
@RequiredArgsConstructor
@Slf4j
public class BaseServiceImpl<D extends BaseDAO, E extends BaseEntity> {

    protected final D baseDAO;

    @Autowired
    protected SharpService sharpService;

    /**
     * 保存或更新
     *
     * @param e
     * @return
     */
    public boolean saveOrUpdate(@Valid E e) {
        Long id = (Long) getFieldValue(e, "id");
        if (Objects.isNull(id)) {
            return save(e);
        } else {
            return update(e);
        }
    }

    /**
     * 批量保存或更新
     *
     * @param collection
     * @return
     */
    public boolean saveOrUpdate(@Valid Collection<E> collection) {
        final int[] insertCount = baseDAO.insertOrUpdate(collection);
        return insertCount.length == collection.size();
    }

    /**
     * 保存
     *
     * @param e
     * @return
     */
    public boolean save(@Valid E e) {
        return baseDAO.insert(e) > 0;
    }

    public boolean save(@Valid Collection<E> collection) {
        final int[] insertCount = baseDAO.insert(collection);
        return insertCount.length == collection.size();
    }

    /**
     * 更新
     *
     * @param e
     */
    public boolean update(@Valid E e) {
        Assert.notNull(getFieldValue(e, "id"), "id cannot be null");
        int count = baseDAO.update(e);
        if (count == 0) {
            log.warn("更新数据行数为0");
        }
        return count > 0;
    }

    /**
     * 根据id物理删除
     *
     * @param id
     * @return
     */
    public boolean deleteById(Long id) {
        return baseDAO.deleteById(id) > 0;
    }

    /**
     * 根据id逻辑删除
     *
     * @param id
     * @return
     */
    public boolean deleteLogicallyById(Long id) {
        return baseDAO.deleteLogicallyById(id) > 0;
    }


    /**
     * 根据id查询
     *
     * @param id
     * @return
     */
    public Optional<E> findById(Long id) {
        return baseDAO.selectById(id);
    }

    /**
     * 不进行关联查询获取id数据
     *
     * @param id
     * @return
     */
    public Optional<E> findByIdWithoutCascade(Long id) {
        List<E> list = findByConditionWithoutCascade(Params.builder(1)
                .pv("id", id)
                .build(), "id = :id");
        return Optional.ofNullable(list.size() == 1 ? list.get(0) : null);
    }

    /**
     * 获取所有数据
     *
     * @param
     * @return
     */
    public List<E> findAll() {
        return baseDAO.selectAll();
    }

    /**
     * 不进行关联查询获取所有数据
     *
     * @param
     * @return
     */
    public List<E> findAllWithoutCascade() {
        List<E> list = findByConditionWithoutCascade(Collections.emptyMap(), "");
        return list;
    }

    public List<E> findByConditionWithoutCascade(Map<String, ?> params, String condition) {
        List<E> list = sharpService.query(baseDAO.getSelectSQL() + (StringUtils.isBlank(condition) ? "" : " WHERE " + condition),
                params,
                baseDAO.getEntity());

        return list;
    }

    public List<E> findByConditionWithoutCascade(E e, String condition) {
        return findByConditionWithoutCascade(baseDAO.entityToMap(e), condition);
    }

    public boolean exists(E e, String conditionSQL) {
        return exists(baseDAO.entityToMap(e), conditionSQL);
    }

    public boolean exists(Map<String, ?> params, String conditionSQL) {
        return baseDAO.existsByParams(params, conditionSQL);
    }


    private Object getFieldValue(E e, String fieldName) {
        Method method = ReflectionUtils.findMethod(e.getClass(), "get" + (fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1)));
        Object value = ReflectionUtils.invokeMethod(method, e);
        return value;
    }

}
