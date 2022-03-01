package com.rick.db.service.support;

import com.google.common.collect.Maps;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Rick
 * @createdAt 2021-10-27 10:14:00
 */
public class Params {

    public static ParamsBuilder builder(int expectedSize) {
        Map<String, Object> params = Maps.newHashMapWithExpectedSize(expectedSize);
        return new ParamsBuilder(params);
    }

    public static ParamsBuilder builder() {
        Map<String, Object> params = new HashMap<>();
        return new ParamsBuilder(params);
    }

    public static class ParamsBuilder {

        private Map<String, Object> params;

        private ParamsBuilder(Map<String, Object> params) {
            this.params = params;
        }

        public ParamsBuilder pv(String paramName, Object value) {
            params.put(paramName, value);
            return this;
        }

        public ParamsBuilder pvAll(Map<String, Object> map) {
            params.putAll(map);
            return this;
        }

        public Map<String, Object> build() {
            return params;
        }

    }

}
