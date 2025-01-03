package com.rick.admin.common;

import com.rick.admin.sys.user.entity.User;
import com.rick.common.util.ClassUtils;
import com.rick.common.util.StringUtils;
import com.rick.db.dto.BaseEntity;
import com.rick.db.plugin.dao.annotation.Sql;
import com.rick.db.plugin.dao.support.EntityCodeIdFillService;
import com.rick.db.plugin.model.IdCodeValue;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 根据 code 自动填充 id
 * @author Rick.Xu
 * @date 2024/11/19 12:46
 */
@Service
@RequiredArgsConstructor
public class EntityFieldsFillService {

    private final EntityCodeIdFillService entityIdFillService;

    private final static Map<String, Class> idFieldClassMap = new HashMap<>();

    static {
        idFieldClassMap.put("userId", User.class);
//        idFieldClassMap.put("plantId", Plant.class);
//        idFieldClassMap.put("storageLocationId", StorageLocation.class);
    }

    public void fill(Object object) {
        if (Objects.isNull(object)) {
            return;
        }

        if (Collection.class.isAssignableFrom(object.getClass())) {
            // 如果是集合
            for (Object item : (Collection)object) {
                fill(item);
            }
        }

        Class clazz = object.getClass();
        Field[] allFields = ClassUtils.getAllFields(clazz);

        Map<String, Field> allFieldMap = Arrays.stream(allFields).collect(Collectors.toMap(field -> field.getName(), Function.identity()));

        for (Field field : allFields) {
            field.setAccessible(true);
            Object value = ReflectionUtils.getField(field, object);
            if (Objects.isNull(value)) {
                continue;
            }

            if (Collection.class.isAssignableFrom(field.getType())) {
                fill(value);
            } else if (BaseEntity.class.isAssignableFrom(field.getType())) {
                fill(value);
            } if (field.getType() == IdCodeValue.class) {
                IdCodeValue realTypeValue = (IdCodeValue)value;

                if (Objects.nonNull(value)) {
                    Sql sql = field.getAnnotation(Sql.class);
                    if (sql.value().contains("sys_user")) {
//                       SELECT id, code, name as description from sys_user WHERE id = :id
                        Long id = entityIdFillService.fill(User.class, realTypeValue.getId(), realTypeValue.getCode());
                        realTypeValue.setId(id);
                    }

//                    if (sql.value().contains("core_group")) {
////                    SELECT id, code, description from core_group WHERE id = :id group_type = 'MATERIAL'
//                        Long id = entityIdFillService.fillObjectGroup(realTypeValue.getId(), realTypeValue.getCode(), groupType(sql.value()));
//                        realTypeValue.setId(id);
//                    } else if (sql.value().contains("core_unit")) {
//                        // SELECT id, code, description from core_unit WHERE id = :id
//                        Long id = entityIdFillService.fill(Unit.class, realTypeValue.getId(), realTypeValue.getCode());
//                        realTypeValue.setId(id);
//                    } else if (sql.value().contains("mm_material")) {
//                        // SELECT id, code, description from mm_material WHERE id = :id
//                        Long id = entityIdFillService.fill(Material.class, realTypeValue.getId(), realTypeValue.getCode());
//                        realTypeValue.setId(id);
//                    } else if (sql.value().contains("core_plant_storage_location")) {
//                        String plantCode;
//                        try {
//                            plantCode = getPlantCode(allFieldMap, clazz, object);
//                        } catch (NoSuchFieldException e) {
//                            throw new RuntimeException(e);
//                        }
//                        Long id = entityIdFillService.fillStorageLocation(realTypeValue.getId(), plantCode, realTypeValue.getCode());
//                        realTypeValue.setId(id);
//                    } else if (sql.value().contains("core_plant")) {
//                        // SELECT id, code, description from core_plant WHERE id = :id
//                        Long id = entityIdFillService.fill(Plant.class, realTypeValue.getId(), realTypeValue.getCode());
//                        realTypeValue.setId(id);
//                    }
                }
            } else if (field.getName().endsWith("Code")) {
                invokeMethod(allFieldMap, clazz, object, (String) value, field.getName());
            }
        }
    }

    private void invokeMethod(Map<String, Field> allFieldMap, Class clazz, Object object, String value, String fieldName) {
        String objFieldName = fieldName.replaceAll("Code$", "");

        try {
            Field objField = allFieldMap.get(objFieldName);
            if (Objects.isNull(objField)) {
                objFieldName = objFieldName + "Id";
                objField = allFieldMap.get(objFieldName);
                if (Objects.isNull(objField)) {
                    return;
                }
            }

            String objSetFieldName = StringUtils.setMethodName(objFieldName);
            Class entityClass = objField.getType();

            if (entityClass == Long.class) {
                // code Id
                entityClass = idFieldClassMap.get(objFieldName);
                Field declaredField = clazz.getDeclaredField(objFieldName);
                declaredField.setAccessible(true);

//                if (objFieldName.equals("storageLocationId")) {
//                    String plantCode = getPlantCode(allFieldMap, clazz, object);
//                    ReflectionUtils.invokeMethod(clazz.getMethod(objSetFieldName, Long.class), object, entityIdFillService.fillStorageLocation(entityIdFillService.fillStorageLocation((Long) ReflectionUtils.getField(declaredField, object), plantCode, value), plantCode, value));
//                } else {
                    ReflectionUtils.invokeMethod(clazz.getMethod(objSetFieldName, Long.class), object, entityIdFillService.fill(entityClass, (Long) ReflectionUtils.getField(declaredField, object), value));
                }
//            } else {
                // @ManyToOne
                ReflectionUtils.invokeMethod(clazz.getMethod(objSetFieldName, entityClass), object, entityIdFillService.fill(entityClass, value));
//            }

        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

//    private ObjectGroup.GroupTypeEnum groupType(String sql) {
//        String regex = "group_type\\s*=\\s*'(\\w+)'";
//        Pattern pattern = Pattern.compile(regex);
//        Matcher matcher = pattern.matcher(sql);
//
//        if (matcher.find()) {
//            String code = matcher.group(1);
//            return ObjectGroup.GroupTypeEnum.valueOfCode(code);
//        }
//
//        return null;
//    }

//    private String getPlantCode(Map<String, Field> allFieldMap, Class clazz, Object object) throws NoSuchFieldException {
//        String plantCode = null;
//
//        if (allFieldMap.keySet().contains("plantCode")) {
//            Field plantCodeField = clazz.getDeclaredField("plantCode");
//            plantCodeField.setAccessible(true);
//            plantCode = (String) ReflectionUtils.getField(plantCodeField, object);
//        } else if (allFieldMap.keySet().contains("plant")) {
//            Field plantField = clazz.getDeclaredField("plant");
//            plantField.setAccessible(true);
//
//            if (plantField.getType() == IdCodeValue.class) {
//                plantCode = ((IdCodeValue) ReflectionUtils.getField(plantField, object)).getCode();
//            } else if (plantField.getType() == Plant.class) {
//                plantCode = ((Plant) ReflectionUtils.getField(plantField, object)).getCode();
//            }
//        }
//
//        return plantCode;
//    }

}
