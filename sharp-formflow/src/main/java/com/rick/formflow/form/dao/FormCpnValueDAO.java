package com.rick.formflow.form.dao;

import com.rick.db.plugin.dao.core.BaseDAOImpl;
import com.rick.formflow.form.cpn.core.FormCpnValue;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Rick
 * @createdAt 2021-11-03 18:10:00
 */

@Repository
public class FormCpnValueDAO extends BaseDAOImpl<FormCpnValue, Long> {

    public int deleteByInstanceId(Long instanceId) {
        return delete("instance_id", Arrays.asList(instanceId));
    }

    /**
     *
     * @param instanceId
     * @return key: formCpnId
     */
    public Map<Long, FormCpnValue> selectByInstanceIdAsMap(Long instanceId) {
        return selectByParams(FormCpnValue.builder().instanceId(instanceId).build())
                .stream().collect(Collectors.toMap(FormCpnValue::getFormCpnId, v-> v));
    }
}