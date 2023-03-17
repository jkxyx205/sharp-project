package com.rick.demo.module.form;

import com.rick.formflow.form.service.FormAdvice;
import com.rick.formflow.form.service.bo.FormBO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author Rick
 * @createdAt 2021-11-07 09:19:00
 */
@Component("project")
@Slf4j
public class ProjectFormSaveAdvice implements FormAdvice {

    @Override
    public void beforeInstanceHandle(FormBO form, Long instanceId, Map<String, Object> values) {

    }

    @Override
    public void afterInstanceHandle(FormBO form, Long instanceId, Map<String, Object> values) {
        log.info("formId: {}, instance: {}, \r\nvalues:{}", form.getForm().getId(), instanceId, values);
    }

}
