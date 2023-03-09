package com.rick.demo.module.form;

import com.rick.formflow.form.service.FormAdvice;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Rick
 * @createdAt 2021-11-07 09:19:00
 */
@Component("project")
@Slf4j
public class ProjectFormSaveAdvice implements FormAdvice {

    @Override
    public void afterInstanceSave(Long formId, Long instanceId, Map<String, Object> values) {
        log.info("formId: {}, instance: {}, \r\nvalues:{}", formId, instanceId, values);
    }

    @Override
    public Map<String, Object> getValue(Long formId, Long instanceId) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "RickFromTable");
        return map;
    }
}
