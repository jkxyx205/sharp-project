package com.rick.formflow.form.controller;

import com.rick.common.http.model.Result;
import com.rick.common.http.model.ResultUtils;
import com.rick.formflow.form.cpn.core.CpnConfigurer;
import com.rick.formflow.form.cpn.core.Form;
import com.rick.formflow.form.service.FormCpnService;
import com.rick.formflow.form.service.FormService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * @author Rick
 * @createdAt 2021-11-07 09:46:00
 */
@RestController
@RequestMapping("forms")
@RequiredArgsConstructor
public class FormController {

    private final FormService FormService;

    private final FormCpnService formCpnService;

    @PostMapping
    public Result save(@RequestBody Form form) {
        return ResultUtils.success(FormService.save(form));
    }

    @PostMapping("configs")
    public Result formCpnMapping(@RequestBody @Validated FormConfig formConfig) {
        formCpnService.saveOrUpdateByConfigurer(formConfig.getForm(), formConfig.getConfigs());
        return ResultUtils.success(formConfig.getForm().getId());
    }

    @PostMapping("{formId}")
    public Result formIdCpnMapping(@RequestBody List<CpnConfigurer> cpnConfigurerList, @PathVariable Long formId) {
        formCpnService.saveOrUpdateByConfigurer(formId, cpnConfigurerList);
        return ResultUtils.success();
    }

    @PostMapping("{formId}/configs")
    public Result formIdConfigIdsMapping(@RequestBody Long[] configIds, @PathVariable Long formId) {
        formCpnService.saveOrUpdateByConfigIds(formId, configIds);
        return ResultUtils.success();
    }

    @Data
    public static class FormConfig {

        @Valid
        private Form form;

        @Valid
        private List<CpnConfigurer> configs;

    }

}
