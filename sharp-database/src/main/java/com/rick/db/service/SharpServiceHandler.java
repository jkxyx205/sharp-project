package com.rick.db.service;

import java.util.Map;

/**
 * @author Rick
 * @version 1.0.0
 * @Description SharpService回调接口
 * @createTime 2020-12-23- 13:51:00
 */
public interface SharpServiceHandler<T> {
    T handle(SharpService sharpService, String sql, Map<String, Object> params);
}
