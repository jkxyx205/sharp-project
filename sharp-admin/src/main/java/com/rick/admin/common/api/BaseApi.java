package com.rick.admin.common.api;

import com.rick.admin.common.exception.ResourceNotFoundException;
import com.rick.common.http.HttpServletRequestUtils;
import com.rick.common.http.model.Result;
import com.rick.common.http.model.ResultUtils;
import com.rick.db.dto.BaseEntity;
import com.rick.db.dto.Grid;
import com.rick.db.dto.SimpleEntity;
import com.rick.db.plugin.GridUtils;
import com.rick.db.plugin.dao.core.EntityDAOImpl;
import com.rick.db.plugin.dao.core.EntityDAOManager;
import com.rick.db.service.SharpService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Optional;

/**
 * @author Rick.Xu
 * @date 2023/6/14 00:12
 */
@RequiredArgsConstructor
public class BaseApi<T extends BaseEntity, ID> {

    protected final EntityDAOImpl<T, ID> entityDAO;

    @Resource
    protected SharpService sharpService;

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

    @PostMapping
    public SimpleEntity save(@RequestBody T t) {
        entityDAO.insert(t);
        return SimpleEntity.builder().id(t.getId()).build();
    }

    @PutMapping("{id}")
    public SimpleEntity update(@RequestBody T t, @PathVariable Long id) {
        t.setId(id);
        return this.update(t);
    }

    @PutMapping
    public SimpleEntity update(@RequestBody T t) {
        int affectRow = entityDAO.update(t);
        if (affectRow == 0) {
            throw getResourceNotFoundException(t.getId());
        }

        return SimpleEntity.builder().id(t.getId()).build();
    }

    @GetMapping("{id}")
    public T findById(@PathVariable ID id) {
        return getEntityFromOptional(entityDAO.selectById(id), id);
    }

    @DeleteMapping("{id}")
    public Result<?> deleteById(@PathVariable ID id) {
        return ResultUtils.success(entityDAO.deleteById(id));
    }

    protected T getEntityFromOptional(Optional<T> optional, Object key) {
        return optional.orElseThrow(() -> getResourceNotFoundException(key));
    }

    protected ResourceNotFoundException getResourceNotFoundException(Object key) {
        return new ResourceNotFoundException(comment());
    }

    protected String comment() {
        return EntityDAOManager.getTableMeta(entityDAO.getEntityClass()).getTable().comment();
    }
}
