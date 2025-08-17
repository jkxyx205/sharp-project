package com.rick.admin.common.api;

import com.rick.admin.common.exception.ResourceNotFoundException;
import com.rick.common.http.HttpServletRequestUtils;
import com.rick.common.http.model.Result;
import com.rick.common.http.model.ResultUtils;
import com.rick.db.dto.Grid;
import com.rick.db.dto.SimpleEntity;
import com.rick.db.plugin.GridUtils;
import com.rick.db.plugin.dao.core.EntityDAO;
import com.rick.db.plugin.dao.core.EntityDAOManager;
import com.rick.db.service.BaseServiceImpl;
import com.rick.db.service.SharpService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Optional;

/**
 * @author Rick.Xu
 * @date 2023/6/14 00:12
 */
public class BaseApi<S extends BaseServiceImpl<? extends EntityDAO<T, ID>, T, ID>, T extends SimpleEntity<ID>, ID> {

    protected final EntityDAO<T, ID> entityDAO;

    protected final S baseService;

    private final Class<T> entityClass;

    @Resource
    protected SharpService sharpService;

    public BaseApi(S baseService) {
        this.baseService = baseService;
        this.entityDAO = baseService.getBaseDAO();
        this.entityClass = entityDAO.getEntityClass();
    }

    @GetMapping
    public Grid<Map<String, Object>> list(HttpServletRequest request) {
        Map<String, Object> params = HttpServletRequestUtils.getParameterMap(request);
        return GridUtils.list(entityDAO.getSelectConditionSQL(params), params);
    }

    @GetMapping("one")
    public Map<String, Object> one(HttpServletRequest request) {
        Map<String, Object> params = HttpServletRequestUtils.getParameterMap(request);
        return sharpService.queryForObject(entityDAO.getSelectConditionSQL(params), params).orElseThrow(() -> getResourceNotFoundException(null));
    }

//    @PostMapping
//    public SimpleEntity save(@RequestBody T t) {
//        baseService.save(t);
//        return SimpleEntity.builder().id(t.getId()).build();
//    }

//    @PutMapping("{id}")
//    public SimpleEntity update(@RequestBody T t, @PathVariable Long id) {
//        t.setId(id);
//        return this.update(t);
//    }
//
//    @PutMapping
//    public SimpleEntity update(@RequestBody T t) {
//        baseService.update(t);
//        return t;
//    }

    @GetMapping("new")
    public T newEntity() {
        return BeanUtils.instantiateClass(entityClass);
    }

    @GetMapping("{id}")
    public T findById(@PathVariable ID id) {
        return getEntityFromOptional(baseService.findById(id), id);
    }

    @PutMapping("{id}")
    public SimpleEntity update(@PathVariable ID id, @RequestBody T t) {
        t.setId(id);
        baseService.update(t);
        return t;
    }

    @PostMapping
    public SimpleEntity saveOrUpdate(@RequestBody T t) {
        baseService.saveOrUpdate(t);
        return t;
    }

    @DeleteMapping("{id}")
    public Result<?> deleteById(@PathVariable ID id) {
        return ResultUtils.success(baseService.deleteLogicallyById(id));
    }

    protected T getEntityFromOptional(Optional<T> optional, Object key) {
        return optional.orElseThrow(() -> getResourceNotFoundException(key));
    }

    protected ResourceNotFoundException getResourceNotFoundException(Object key) {
        return new ResourceNotFoundException(comment() + " id = " + key + "不存在");
    }

    protected String comment() {
        return EntityDAOManager.getTableMeta(entityDAO.getEntityClass()).getTable().comment();
    }
}
