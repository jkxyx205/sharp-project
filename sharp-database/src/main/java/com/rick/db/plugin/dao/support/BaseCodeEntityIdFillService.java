package com.rick.db.plugin.dao.support;

import com.rick.db.dto.BaseCodeEntity;
import com.rick.db.plugin.dao.core.BaseCodeDAOImpl;
import com.rick.db.plugin.dao.core.BaseDAOManager;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Rick
 * @createdAt 2023-03-08 22:17:00
 */
public class BaseCodeEntityIdFillService {

    public <T extends BaseCodeEntity> void fill(T t) {
        if (t == null || t.getId() != null || StringUtils.isBlank(t.getCode())) {
            return;
        }

        t.setId(fill(t.getClass(), t.getId(), t.getCode()));
    }

    public <T extends BaseCodeEntity> T fill(Class<T> clazz, String code) {
        try {
            return fill(clazz, clazz.newInstance(), code);
        } catch (Exception e) {
            return null;
        }
    }

    public <T extends BaseCodeEntity> T fill(Class<T> clazz, T t, String code) {
        if (StringUtils.isBlank(code)) {
            return  t;
        }

        if (t == null) {
            try {
                t = clazz.newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        if (t.getId() != null ) {
            return t;
        }

        t.setCode(code);
        t.setId(fill(clazz, t.getId(), t.getCode()));
        return t;
    }

    public <T extends BaseCodeEntity> Long fill(Class<T> clazz, Long id, String code) {
        if (id != null || StringUtils.isBlank(code)) {
            return id;
        }

        return ((BaseCodeDAOImpl) BaseDAOManager.baseDAOEntityMap.get(clazz)).selectIdByCodeOrThrowException(code);
    }

}
