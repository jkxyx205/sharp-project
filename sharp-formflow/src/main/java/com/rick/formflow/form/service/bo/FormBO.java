package com.rick.formflow.form.service.bo;

import com.google.common.collect.Maps;
import com.rick.common.util.ReflectUtils;
import com.rick.formflow.form.cpn.core.CpnConfigurer;
import com.rick.formflow.form.cpn.core.Form;
import com.rick.formflow.form.valid.core.Validator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Rick
 * @createdAt 2021-11-03 17:28:00
 */
@Value
public class FormBO {

    private Form form;

    private Long instanceId;

    private List<Property> propertyList;

    @AllArgsConstructor
    @Getter
    public static class Property {

        private Long id;

        private String name;

        private CpnConfigurer configurer;

        @Setter
        private Object value;
        
        public Map<String, Object> getValidatorProperies() {
            Set<Validator> validatorList = configurer.getValidatorList();
            Map<String, Object> map = Maps.newHashMap();
            for (Validator validator : validatorList) {
                Method[] methods = validator.getClass().getMethods();
                Field[] allFields = ReflectUtils.getAllFields(validator.getClass());
                Set<String> getMethodsNames = Arrays.stream(allFields).map(field -> {
                    return (field.getType() == boolean.class ? "is" : "get") + StringUtils.capitalize(field.getName());
                }).collect(Collectors.toSet());

                for (Method method : methods) {
                    if (getMethodsNames.contains(method.getName())) {
                        try {
                            map.put(validator.getClass().getSimpleName() + "." + StringUtils.uncapitalize((method.getName().startsWith("get")) ?method.getName().substring(3) : method.getName().substring(2)), method.invoke(validator));
                        }  catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

            }
            return map;
        }

    }

    public String getActionUrl() {
        return form.getId() + (Objects.nonNull(instanceId) ? "/" + instanceId : "");
    }

    public String getMethod() {
        return Objects.nonNull(instanceId) ? "PUT" : "POST";
    }

}
