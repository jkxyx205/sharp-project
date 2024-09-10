package com.rick.formflow.form.service.bo;

import com.rick.formflow.form.cpn.core.CpnConfigurer;
import com.rick.formflow.form.cpn.core.Form;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

    private  Map<String, Object> data;

    @AllArgsConstructor
    @Getter
    public static class Property {

        private Long id;

        private String name;

        private CpnConfigurer configurer;

        @Setter
        private Object value;

    }

    public Map<String, Property> getPropertyMap() {
        return propertyList.stream().collect(Collectors.toMap(Property::getName, p -> p));
    }

    public String getActionUrl() {
        return form.getId() + (Objects.nonNull(instanceId) ? "/" + instanceId : "");
    }

    public String getMethod() {
        return Objects.nonNull(instanceId) ? "PUT" : "POST";
    }

    public Map<String, Object> getData() {
        return data;
    }

    public Map<String, Object> getPropertyData() {
        return propertyList.stream()
                .collect(HashMap::new, (m, v) -> m.put(v.name, v.value), HashMap::putAll);
    }

}
