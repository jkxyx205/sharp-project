package com.rick.formflow.form.service;

import com.rick.formflow.form.service.model.FormCache;
import lombok.experimental.UtilityClass;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Rick.Xu
 * @date 2024/8/25 17:47
 */
@UtilityClass
public class FormUtils {

    public static Map<Long, FormCache> formCacheMap = new HashMap<>();

    FormCache getFormCacheById(Long id) {
        return formCacheMap.get(id);
    }

    void update(Long id, FormCache formCache) {
        formCacheMap.put(id, formCache);
    }

}
