package com.rick.formflow.form.controller;

import com.rick.common.http.model.Result;
import com.rick.common.http.model.ResultUtils;
import com.rick.formflow.form.cpn.core.CpnConfigurer;
import com.rick.formflow.form.service.CpnConfigurerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Rick
 * @createdAt 2021-11-07 09:46:00
 */
@RestController
@RequestMapping("forms/configurers")
@RequiredArgsConstructor
public class CpnConfigurerController {

    private final CpnConfigurerService cpnConfigurerService;

    @PostMapping
    public Result save(@RequestBody List<CpnConfigurer> cpnConfigurerList) {
        cpnConfigurerService.saveOrUpdate(cpnConfigurerList);
        return ResultUtils.success(cpnConfigurerList.stream().map(CpnConfigurer::getId).toArray());
    }

}
