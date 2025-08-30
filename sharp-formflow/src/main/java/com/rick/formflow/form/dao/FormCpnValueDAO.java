package com.rick.formflow.form.dao;

import com.rick.db.repository.EntityDAOImpl;
import com.rick.formflow.form.cpn.core.FormCpnValue;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Rick
 * @createdAt 2021-11-03 18:10:00
 */

@Repository
public class FormCpnValueDAO extends EntityDAOImpl<FormCpnValue, Long> {

    public int deleteByInstanceId(Long instanceId) {
        Assert.notNull(instanceId, "instanceId cannot be null");
        return delete("instance_id", Arrays.asList(instanceId));
    }

    /**
     *
     * @param instanceId
     * @return key: formCpnId
     */
    public Map<Long, FormCpnValue> selectByInstanceIdAsMap(Long instanceId) {
        return select(FormCpnValue.builder().instanceId(instanceId).build())
                .stream().collect(Collectors.toMap(FormCpnValue::getFormCpnId, v-> v));
    }
}