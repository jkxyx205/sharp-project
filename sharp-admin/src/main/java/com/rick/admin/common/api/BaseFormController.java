package com.rick.admin.common.api;

import com.rick.common.http.model.Result;
import com.rick.common.http.model.ResultUtils;
import com.rick.common.util.ClassUtils;
import com.rick.db.dto.BaseEntity;
import com.rick.db.service.BaseServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * 表单的增删改
 * @author Rick.Xu
 * @date 2024/1/26 16:05
 */
public class BaseFormController<E extends BaseEntity, S extends BaseServiceImpl> {

    protected final S baseService;

    private final String formPage;

    private final String entityPropertyName;

    private final Class entityClass;

    public BaseFormController(S service, String formPage) {
        this.baseService = service;
        this.formPage = formPage;
        this.entityClass = ClassUtils.getClassGenericsTypes(this.getClass())[0];
        String simpleName = entityClass.getSimpleName();
        this.entityPropertyName = simpleName.substring(0, 1).toLowerCase() + simpleName.substring(1);
    }

    @GetMapping("new")
    public String gotoFormPage(Model model) {
        model.addAttribute(entityPropertyName, BeanUtils.instantiateClass(this.entityClass));
        return this.formPage;
    }

    @GetMapping("{id}")
    public String gotoFormPageById(@PathVariable  Long id, Model model) {
        model.addAttribute(entityPropertyName, baseService.findById(id).get());
        return this.formPage;
    }

    @PostMapping
    @ResponseBody
    public Result saveOrUpdate(@RequestBody E e) {
        baseService.saveOrUpdate(e);
        return ResultUtils.success(e);
    }

    @DeleteMapping("{id}")
    @ResponseBody
    public Result deleteById(@PathVariable Long id) {
        baseService.deleteLogicallyById(id);
        return ResultUtils.success();
    }
}
