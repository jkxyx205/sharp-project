package com.rick.admin.common.api;

import com.rick.admin.common.exception.ResourceNotFoundException;
import com.rick.common.http.HttpServletRequestUtils;
import com.rick.common.http.model.Result;
import com.rick.common.http.model.ResultUtils;
import com.rick.db.plugin.BaseServiceImpl;
import com.rick.db.plugin.page.Grid;
import com.rick.db.plugin.page.GridUtils;
import com.rick.db.repository.EntityDAO;
import com.rick.db.repository.TableDAO;
import com.rick.db.repository.model.EntityId;
import com.rick.db.repository.support.SQLParamCleaner;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author Rick.Xu
 * @date 2023/6/14 00:12
 */
public class BaseApi<S extends BaseServiceImpl<? extends EntityDAO<T, ID>, T, ID>, T extends EntityId<ID>, ID> {

    protected final EntityDAO<T, ID> entityDAO;

    protected final S baseService;

    private final Class<T> entityClass;

    @Resource
    protected TableDAO tableDAO;

    public BaseApi(S baseService) {
        this.baseService = baseService;
        this.entityDAO = baseService.getBaseDAO();
        this.entityClass = entityDAO.getTableMeta().getEntityClass();
    }

    @GetMapping
    public Grid<Map<String, Object>> list(HttpServletRequest request) {
        Map<String, Object> params = HttpServletRequestUtils.getParameterMap(request);
        return GridUtils.list(SQLParamCleaner.formatSql(entityDAO.getTableMeta().getSelectConditionSQL(), params, new HashMap<>()), params);
    }

    @GetMapping("one")
    public Map<String, Object> one(HttpServletRequest request) {
        Map<String, Object> params = HttpServletRequestUtils.getParameterMap(request);
        return tableDAO.selectForObject(SQLParamCleaner.formatSql(entityDAO.getTableMeta().getSelectConditionSQL(), params, new HashMap<>()), params).orElseThrow(() -> getResourceNotFoundException(null));
    }

//    @PostMapping
//    public EntityId save(@RequestBody T t) {
//        baseService.save(t);
//        return EntityId.builder().id(t.getId()).build();
//    }

//    @PutMapping("{id}")
//    public EntityId update(@RequestBody T t, @PathVariable Long id) {
//        t.setId(id);
//        return this.update(t);
//    }
//
//    @PutMapping
//    public EntityId update(@RequestBody T t) {
//        baseService.update(t);
//        return t;
//    }

    @GetMapping("new")
    public T newEntity() {
        return BeanUtils.instantiateClass(entityClass);
    }

    @GetMapping("{id}")
    public T findById(@PathVariable ID id) {
        return getEntityFromOptional(baseService.selectById(id), id);
    }

    @PutMapping("{id}")
    public EntityId update(@PathVariable ID id, @RequestBody T t) {
        t.setId(id);
        baseService.update(t);
        return t;
    }

    @PostMapping
    public EntityId saveOrUpdate(@RequestBody T t) {
        baseService.insertOrUpdate(t);
        return t;
    }

    @DeleteMapping("{id}")
    public Result<?> deleteById(@PathVariable ID id) {
        return ResultUtils.success(baseService.deleteById(id));
    }

    protected T getEntityFromOptional(Optional<T> optional, Object key) {
        return optional.orElseThrow(() -> getResourceNotFoundException(key));
    }

    protected ResourceNotFoundException getResourceNotFoundException(Object key) {
        return new ResourceNotFoundException(comment() + " id = " + key);
    }

    protected String comment() {
        return entityDAO.getTableMeta().getTable().comment();
    }
}
