package com.rick.formflow.form.controller;


import com.rick.common.http.model.Result;
import com.rick.common.http.model.ResultUtils;
import com.rick.formflow.form.service.FormService;
import com.rick.formflow.form.service.bo.FormBO;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author Rick
 * @createdAt 2021-11-03 18:04:00
 */
@RestController
@RequestMapping("form/ajax")
@RequiredArgsConstructor
public class AjaxFormController {

    private final FormService formService;

    @GetMapping("{formId}")
    public FormBO get(@PathVariable Long formId) {
        return formService.getFormBOById(formId);
    }

    @GetMapping("{formId}/{instanceId}")
    public FormBO get(@PathVariable Long formId, @PathVariable Long instanceId) {
        return formService.getFormBOByIdAndInstanceId(formId, instanceId);
    }

    @PostMapping("{formId}")
    public Result save(@RequestBody Map<String, Object> values, @PathVariable Long formId) throws BindException {
        formService.post(formId, values);
        return ResultUtils.success();
    }

    @PutMapping("{formId}/{instanceId}")
    @PostMapping("{formId}/{instanceId}")
    public Result update(@RequestBody Map<String, Object> values, @PathVariable Long formId, @PathVariable Long instanceId) throws BindException {
        formService.post(formId, instanceId, values);
        return ResultUtils.success();
    }

    @DeleteMapping("{formId}/{instanceId}")
    public Result delete(@PathVariable Long instanceId) {
        return ResultUtils.success(formService.delete(instanceId));
    }
}
