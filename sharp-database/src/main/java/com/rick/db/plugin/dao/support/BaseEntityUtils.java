package com.rick.db.plugin.dao.support;

import com.rick.db.dto.BaseEntity;
import lombok.experimental.UtilityClass;
import org.springframework.beans.BeanUtils;

/**
 * @author Rick.Xu
 * @date 2023/4/7 21:17
 */
@UtilityClass
public class BaseEntityUtils {

    public void resetAdditionalFields(BaseEntity entity) {
        entity.setId(null);
        entity.setUpdatedAt(null);
        entity.setUpdatedBy(null);
        entity.setUpdatedAt(null);
        entity.setUpdatedAt(null);
        entity.setDeleted(null);
    }

    public void copyProperties(Object source, BaseEntity target) {
        copyProperties(source, target, (String[])null);
    }

    public void copyProperties(Object source, BaseEntity target, String... ignoreProperties) {
        BeanUtils.copyProperties(source, target, ignoreProperties);
        BaseEntityUtils.resetAdditionalFields(target);
    }
}