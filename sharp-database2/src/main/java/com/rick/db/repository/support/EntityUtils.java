package com.rick.db.repository.support;

import com.rick.db.repository.model.BaseEntityInfoGetter;
import com.rick.db.repository.model.EntityId;
import lombok.experimental.UtilityClass;
import org.springframework.beans.BeanUtils;

/**
 * @author Rick.Xu
 * @date 2023/4/7 21:17
 */
@UtilityClass
public class EntityUtils {

    public void resetAdditionalFields(EntityId entity) {
        entity.setId(null);

        if (entity instanceof BaseEntityInfoGetter) {
            ((BaseEntityInfoGetter)entity).getBaseEntityInfo().setCreateBy(null);
            ((BaseEntityInfoGetter)entity).getBaseEntityInfo().setCreateTime(null);
            ((BaseEntityInfoGetter)entity).getBaseEntityInfo().setUpdateBy(null);
            ((BaseEntityInfoGetter)entity).getBaseEntityInfo().setUpdateTime(null);
            ((BaseEntityInfoGetter)entity).getBaseEntityInfo().setDeleted(null);
        }
    }

    public void copyPropertiesAndResetAdditionalFields(Object source, EntityId target) {
        copyPropertiesAndResetAdditionalFields(source, target, (String[])null);
    }

    public void copyPropertiesAndResetAdditionalFields(Object source, EntityId target, String... ignoreProperties) {
        BeanUtils.copyProperties(source, target, ignoreProperties);
        EntityUtils.resetAdditionalFields(target);
    }

    public boolean isEntityClass(Class<?> clazz) {
        return EntityId.class.isAssignableFrom(clazz);
    }
}