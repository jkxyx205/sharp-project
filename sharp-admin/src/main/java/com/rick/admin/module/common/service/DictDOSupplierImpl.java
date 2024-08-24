package com.rick.admin.module.common.service;

import com.rick.admin.module.common.entity.CodeDescription;
import com.rick.admin.module.student.entity.Student;
import com.rick.meta.dict.entity.Dict;
import com.rick.meta.dict.service.DictDOSupplier;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 注册 枚举 和 CodeDescription
 * @author Rick.Xu
 * @date 2024/8/18 02:40
 */
@Component
@RequiredArgsConstructor
public class DictDOSupplierImpl implements DictDOSupplier {

    private final CodeDescriptionService codeDescriptionService;

    @Override
    public List<Dict> get() {
        List<Dict> dictList = new ArrayList<>();
        // 1. 注册枚举值
        try {

            registerEnum(dictList,
                    CodeDescription.CategoryEnum.class,
                    Student.GenderEnum.class,
                    Student.HobbyEnum.class
                    );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // CodeDescription注册
        for (CodeDescription codeDescription : codeDescriptionService.findAll()) {
            dictList.add(new Dict(codeDescription.getCategory().name(), codeDescription.getCode(), codeDescription.getDescription(), codeDescription.getSort()));
        }
        return dictList;
    }

    private void registerEnum(List<Dict> dictList, Class<? extends Enum> ... enums) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // 将枚举类型注册到字典
        if (ArrayUtils.isNotEmpty(enums)) {
            for (Class<? extends Enum> clazz : enums) {
                for (Enum value : clazz.getEnumConstants()) {
                    Method getLabelMethod = clazz.getMethod("getLabel");
                    String label = (String) getLabelMethod.invoke(value);
                    dictList.add(new Dict(clazz.getSimpleName(), value.name(), label, value.ordinal()));
                }
            }
        }

    }
}
