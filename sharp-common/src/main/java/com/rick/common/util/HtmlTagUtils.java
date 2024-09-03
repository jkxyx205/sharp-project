package com.rick.common.util;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * @author Rick.Xu
 * @date 2024/9/3 09:47
 */
@UtilityClass
public class HtmlTagUtils {

    /**
     * 标签参数 比如 <input readonly> 判断 readonly true or false
     * @param attrMap
     * @param key
     * @return
     */
    public boolean isTagPropertyTrueAndPut(Map<String, String> attrMap, String key) {
        boolean result;
        if (!attrMap.containsKey(key)) {
            result = false;
        } else {
            String value = attrMap.get(key);
            if (StringUtils.isBlank(value)) {
                result =  true;
            } else if (key.equals(value)) {
                result =  true;
            } else {
                result =  Boolean.valueOf(value);
            }
        }
        attrMap.put(key, String.valueOf(result));
        return result;
    }
}
