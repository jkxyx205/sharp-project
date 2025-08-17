package com.rick.db.service;

import com.rick.db.dto.BaseCodeEntity;
import com.rick.db.dto.SimpleEntity;
import com.rick.db.plugin.dao.core.EntityCodeDAO;
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
public class BaseServiceImpl<D extends EntityDAO<T, ID>, T extends SimpleEntity<ID>, ID> {

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
    public T save(@Valid T e) {
        baseDAO.insert(e);
        return e;
    }

    public Collection<T> save(@Valid Collection<T> collection) {
        baseDAO.insert(collection);
        return collection;
    }

    /**
     * 保存或更新
     *
     * @param e
     * @return
     */
    public T saveOrUpdate(@Valid T e) {
        if (e instanceof BaseCodeEntity && e.getId() == null) {
            ((EntityCodeDAO)baseDAO).assertCodeNotExists(((BaseCodeEntity) e).getCode());
        }

        baseDAO.insertOrUpdate(e);
        return e;
    }

    /**
     * 批量保存或更新
     *
     * @param collection
     * @return
     */
    public Collection<T> saveOrUpdate(@Valid Collection<T> collection) {
        baseDAO.insertOrUpdate(collection);
        return collection;
    }

    /**
     * 更新
     *
     * @param e
     */
    public boolean update(@Valid T e) {
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
    public boolean deleteById(ID id) {
        return baseDAO.deleteById(id) > 0;
    }

    /**
     * 根据id逻辑删除
     *
     * @param id
     * @return
     */
    public boolean deleteLogicallyById(ID id) {
        return baseDAO.deleteLogicallyById(id) > 0;
    }


    /**
     * 根据id查询
     *
     * @param id
     * @return
     */
    public Optional<T> findById(ID id) {
        return baseDAO.selectById(id);
    }

    /**
     * 不进行关联查询获取id数据
     *
     * @param id
     * @return
     */
    public Optional<T> findByIdWithoutCascade(ID id) {
        List<T> list = findByConditionWithoutCascade(Params.builder(1)
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
    public List<T> findAllWithoutCascade() {
        List<T> list = findByConditionWithoutCascade(Collections.emptyMap(), "");
        return list;
    }

    public List<T> findByConditionWithoutCascade(Map<String, ?> params, String condition) {
        List<T> list = sharpService.query(baseDAO.getSelectSQL() + (StringUtils.isBlank(condition) ? "" : " WHERE " + condition),
                params,
                baseDAO.getEntityClass());

        return list;
    }

    public List<T> findByConditionWithoutCascade(T e, String condition) {
        return findByConditionWithoutCascade(baseDAO.entityToMap(e), condition);
    }

    /**
     * 获取所有数据
     *
     * @param
     * @return
     */
    public List<T> findAll() {
        return baseDAO.selectAll();
    }

    public boolean exists(T e, String conditionSQL) {
        return exists(baseDAO.entityToMap(e), conditionSQL);
    }

    public boolean exists(Map<String, ?> params, String conditionSQL) {
        return baseDAO.existsByParams(params, conditionSQL);
    }

}
