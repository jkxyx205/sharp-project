package com.rick.db.plugin.dao.core;

import com.rick.db.dto.BaseEntity;
import com.rick.db.service.support.Params;
import lombok.experimental.UtilityClass;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Rick.Xu
 * @date 2024/1/26 21:50
 */
@UtilityClass
public class EntityDAOHelper {

    /**
     * 查找唯一行， 填充id
     * @param entityDAO
     * @param entities
     * @param uniqueColumnName
     * @param refColumnName
     * @param refValue
     * @param <T>
     */
    public  <T extends BaseEntity> void fillEntityIdsByUniqueColumnName(EntityDAO entityDAO, Collection<T> entities, String uniqueColumnName, String refColumnName, Object refValue) {
        if (CollectionUtils.isNotEmpty(entities)) {
            Set<String> emptyIdUniqueColumnNameSet = entities.stream().filter(t -> Objects.isNull(t.getId())).map(t -> (String)EntityDAOManager.getPropertyValue(t, uniqueColumnName)).collect(Collectors.toSet());
            if (CollectionUtils.isNotEmpty(emptyIdUniqueColumnNameSet)) {
                Map<String, Long> codeIdMap = entityDAO.selectByParamsAsMap(Params.builder(1).pv("uniqueColumnName", emptyIdUniqueColumnNameSet).pv("refColumnName", refValue).build(),
                        ""+uniqueColumnName+", id", ""+uniqueColumnName+" IN (:uniqueColumnName)" + (StringUtils.isBlank(refColumnName) ? "" : (" AND " + refColumnName + " = :refColumnName")));

                for (T t : entities) {
                    // fillIds
                    if (codeIdMap.containsKey(EntityDAOManager.getPropertyValue(t, uniqueColumnName))) {
                        t.setId(codeIdMap.get(EntityDAOManager.getPropertyValue(t, uniqueColumnName)));
                    }
                }
            }
        }
    }

    public String getComputedProperty(String methodName) {
        return Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
    }
}
