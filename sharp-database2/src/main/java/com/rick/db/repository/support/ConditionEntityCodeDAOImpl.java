package com.rick.db.repository.support;

import com.rick.db.repository.EntityCodeDAOImpl;
import com.rick.db.repository.model.EntityIdCode;

import java.util.Map;

/**
 * 针对特定的条件操作
 * @author Rick.Xu
 * @date 2025/11/19 12:06
 */
public abstract class ConditionEntityCodeDAOImpl<T extends EntityIdCode<ID>, ID> extends EntityCodeDAOImpl<T, ID> {

    @Override
    public int delete(String condition, Object... args) {
        return super.delete(getMergeArgsCondition(condition), getMergeArgs(args));
    }

    @Override
    public int delete(String condition, Map<String, Object> paramMap) {
        return super.delete(getMergeMapCondition(condition), getMergeMap(paramMap));
    }

    @Override
    public int update(String columns, String condition, Object... args) {
        return super.update(columns, getMergeArgsCondition(condition), getMergeArgs(args));
    }

    @Override
    public int update(String columns, String condition, Map<String, Object> paramMap) {
        return super.update(columns, getMergeMapCondition(condition), getMergeMap(paramMap));
    }

    protected abstract String getMergeArgsCondition(String condition);

    protected abstract Object[] getMergeArgs(Object[] args);

    protected abstract String getMergeMapCondition(String condition);

    protected abstract Map<String, Object> getMergeMap(Map<String, Object> paramMap);
}
