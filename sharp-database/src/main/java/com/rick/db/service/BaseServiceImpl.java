package com.rick.db.service;

import com.rick.db.dto.SimpleEntity;
import com.rick.db.plugin.dao.core.EntityDAO;
import com.rick.db.service.support.Params;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.Valid;
import java.util.*;

/**
 * Service不需要继承任何父类
 * @author Rick
 * @createdAt 2022-04-09 10:10:00
 */
@RequiredArgsConstructor
@Slf4j
public class BaseServiceImpl<D extends EntityDAO, E extends SimpleEntity> {

    @Getter
    protected final D baseDAO;

    @Autowired
    protected SharpService sharpService;

    /**
     * 保存
     *
     * @param e
     * @return
     */
    public E save(@Valid E e) {
        baseDAO.insert(e);
        return e;
    }

    public Collection<E> save(@Valid Collection<E> collection) {
        baseDAO.insert(collection);
        return collection;
    }

    /**
     * 保存或更新
     *
     * @param e
     * @return
     */
    public E saveOrUpdate(@Valid E e) {
        baseDAO.insertOrUpdate(e);
        return e;
    }

    /**
     * 批量保存或更新
     *
     * @param collection
     * @return
     */
    public Collection<E> saveOrUpdate(@Valid Collection<E> collection) {
        baseDAO.insertOrUpdate(collection);
        return collection;
    }

    /**
     * 更新
     *
     * @param e
     */
    public boolean update(@Valid E e) {
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
                baseDAO.getEntityClass());

        return list;
    }

    public List<E> findByConditionWithoutCascade(E e, String condition) {
        return findByConditionWithoutCascade(baseDAO.entityToMap(e), condition);
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

    public boolean exists(E e, String conditionSQL) {
        return exists(baseDAO.entityToMap(e), conditionSQL);
    }

    public boolean exists(Map<String, ?> params, String conditionSQL) {
        return baseDAO.existsByParams(params, conditionSQL);
    }

}
