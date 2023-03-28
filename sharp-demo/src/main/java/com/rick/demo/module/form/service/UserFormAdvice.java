package com.rick.demo.module.form.service;

import com.rick.formflow.form.service.FormAdvice;
import com.rick.formflow.form.service.bo.FormBO;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;

/**
 * @author Rick
 * @createdAt 2023-03-17 10:29:00
 */
@Component("userFormAdvice")
public class UserFormAdvice implements FormAdvice {

    @Override
    public void beforeInstanceHandle(FormBO form, Long instanceId, Map<String, Object> values) {
        // checkbox List => boolean
        Object marriage = values.get("marriage");
        if (marriage instanceof Collection) {
            if (CollectionUtils.isEmpty((Collection<?>) marriage)) {
                values.put("marriage", false);
            } else {
                values.put("marriage", true);
            }
        } else if (marriage == null){
            values.put("marriage", false);
        } else {
            values.put("marriage", true);
        }
    }

    @Override
    public void afterInstanceHandle(FormBO form, Long instanceId, Map<String, Object> values) {

    }
}
