package com.rick.db.plugin.dao.support;

import com.rick.db.constant.EntityConstants;
import com.rick.db.service.support.Params;

import java.util.Map;

/**
 * @author Rick
 * @createdAt 2021-10-28 07:21:00
 */
public class DefaultConditionAdvice implements ConditionAdvice {

    @Override
    public Map<String, Object> getCondition() {
        return Params.builder().pv(EntityConstants.LOGIC_DELETE_COLUMN_NAME, false).build();
    }
}
