package com.rick.db.service;

import com.rick.common.http.exception.Assert;
import com.rick.db.dto.BasePureEntity;
import com.rick.db.plugin.dao.core.BaseDAO;
import lombok.RequiredArgsConstructor;
import org.springframework.util.ReflectionUtils;

import javax.validation.Valid;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Rick
 * @createdAt 2022-04-09 10:10:00
 */
@RequiredArgsConstructor
public class BaseServiceImpl <D extends BaseDAO, E extends BasePureEntity> {

    protected final D baseDAO;

    /**
     * 保存或更新
     * @param e
     * @return
     */
    public E saveOrUpdate(@Valid E e) {
        Long id = (Long) getFieldValue(e, "id");
        if (Objects.isNull(id)) {
            save(e);
        } else {
            update(e);
        }
        return e;
    }

    /**
     * 保存
     * @param e
     * @return
     */
    public E save(@Valid E e) {
        baseDAO.insert(e);
        return e;
    }

    /**
     * 更新
     * @param e
     */
    public E update(@Valid E e) {
        Assert.notNull(getFieldValue(e, "id"), "id");
        int count = baseDAO.update(e);
        if (count == 0) {
            Assert.notExists("更新失败，数据");
        }
        return e;
    }

    /**
     * 根据id删除
     * @param id
     * @return
     */
    public int deleteById(Long id) {
        return baseDAO.deleteLogicallyById(id);
    }

    /**
     * 根据id查询
     * @param id
     * @return
     */
    public Optional<E> findById(Long id) {
        return baseDAO.selectById(id);
    }

    /**
     * 查询所有数据
     * @param
     * @return
     */
    public List<E> findAll() {
        return baseDAO.selectAll();
    }

    private Object getFieldValue(E e, String fieldName) {
        Method method = ReflectionUtils.findMethod(e.getClass(), "get" + (fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1)));
        Object value = ReflectionUtils.invokeMethod(method, e);
        return value;
    }
}
