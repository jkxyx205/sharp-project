package com.rick.demo.module.info.service;

import com.rick.demo.module.info.dao.InfoDAO;
import com.rick.demo.module.info.entity.Info;
import com.rick.formflow.form.service.FormAdvice;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * @author Rick
 * @createdAt 2023-03-09 16:09:00
 */
@RequiredArgsConstructor
@Component("info")
public class InfoFormAdvice implements FormAdvice {

    private final InfoDAO infoDAO;

    @Override
    public void afterInstanceSave(Long formId, Long instanceId, Map<String, Object> values) {
        infoDAO.insertOrUpdate(values);
    }

    @Override
    public Map<String, Object> getValue(Long formId, Long instanceId) {
        Optional<Info> infoOptional = infoDAO.selectById(instanceId);
        if (infoOptional.isPresent()) {
            return infoDAO.entityToMap(infoOptional.get());
        }

        return Collections.emptyMap();
    }
}
