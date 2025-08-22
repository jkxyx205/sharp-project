package com.rick.db.repository.support;

import com.rick.db.repository.model.AdditionalInfoGetter;
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

        if (entity instanceof AdditionalInfoGetter) {
            ((AdditionalInfoGetter)entity).getAdditionalInfo().setCreateBy(null);
            ((AdditionalInfoGetter)entity).getAdditionalInfo().setCreateTime(null);
            ((AdditionalInfoGetter)entity).getAdditionalInfo().setUpdateBy(null);
            ((AdditionalInfoGetter)entity).getAdditionalInfo().setUpdateTime(null);
            ((AdditionalInfoGetter)entity).getAdditionalInfo().setDeleted(null);
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