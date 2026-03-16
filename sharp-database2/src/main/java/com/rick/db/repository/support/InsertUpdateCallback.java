package com.rick.db.repository.support;

import com.rick.db.repository.model.EntityId;

import java.util.Map;

/**
 * @author Rick.Xu
 * @date 2025/11/17 13:16
 */
@FunctionalInterface
public interface InsertUpdateCallback<T extends EntityId<ID>, ID> {
    void handler(boolean insert, T t, Map<String, Object> args);
}
