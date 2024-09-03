package com.rick.admin.module.common.service;

import com.rick.formflow.form.service.FormAdvice;
import com.rick.formflow.form.service.bo.FormBO;
import com.rick.meta.dict.service.DictService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author Rick.Xu
 * @date 2024/9/3 14:54
 */
@Component
@RequiredArgsConstructor
public class DictFormService implements FormAdvice {

    private final DictService dictService;

    @Override
    public void afterInstanceHandle(FormBO form, Long instanceId, Map<String, Object> values) {
        dictService.rebuild((String) values.get("type"));
    }
}
