package com.rick.formflow.form.cpn.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.rick.common.util.JsonUtils;
import com.rick.common.util.ReflectUtils;
import com.rick.db.dto.BaseEntity;
import com.rick.db.plugin.dao.annotation.Column;
import com.rick.db.plugin.dao.annotation.Table;
import com.rick.db.plugin.dao.annotation.Transient;
import com.rick.formflow.form.valid.core.Validator;
import com.rick.formflow.form.valid.core.ValidatorManager;
import com.rick.formflow.form.valid.core.ValidatorTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Rick
 * @createdAt 2021-11-02 20:50:00
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table(value = "sys_form_configurer")
public class CpnConfigurer extends BaseEntity {

    @NotEmpty
    @Column(nullable = false, updatable = false)
    private String name;

    @NotBlank
    private String label;

    @NotNull
    @Column("type")
    private CpnTypeEnum cpnType;

    @JsonIgnore
    @Transient
    private Set<Validator> validatorList;

    /**
     * 手动装配validatorsMap到validatorList
     */
    @Column("validators")
    private Set<Map<String, ?>> validators;

    @Column("options")
    private List<CpnOption> options;

    @Column("default_value")
    private String defaultValue;

    @Column("placeholder")
    private String placeholder;

    private Map<String, Object> additionalInfo;

    public Set<Map<String, ?>> getValidators() {
        if (Objects.isNull(validators) && Objects.nonNull(validatorList)) {
            validators = Sets.newHashSetWithExpectedSize(validatorList.size());
            for (Validator validator : validatorList) {
                validators.add(JsonUtils.objectToMap(validator));
            }

            return validators;
        }
        return Objects.isNull(validators) ? Collections.emptySet() : validators;
    }

    public Set<Validator> getValidatorList() {
        if (Objects.isNull(validatorList) && Objects.nonNull(validators)) {
            validatorList = Sets.newHashSetWithExpectedSize(validators.size());
            try {
                for (Map<String, ?> validatorInfo : validators) {
                    Class<? extends Validator> validatorType = ValidatorManager.getValidatorByType(ValidatorTypeEnum.valueOfCode((String) validatorInfo.get("validator_type")));
                    validatorList.add(JsonUtils.toObject(JsonUtils.toJson(validatorInfo), validatorType));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return Objects.isNull(validatorList) ? Collections.emptySet() : validatorList;
    }

    public Map<String, Object> getValidatorProperties() {
        Set<Validator> validatorList = getValidatorList();
        Map<String, Object> map = Maps.newHashMap();
        for (Validator validator : validatorList) {
            Method[] methods = validator.getClass().getMethods();
            Field[] allFields = ReflectUtils.getAllFields(validator.getClass());
            Set<String> getMethodsNames = Arrays.stream(allFields).map(field -> (field.getType() == boolean.class ? "is" : "get") + StringUtils.capitalize(field.getName())).collect(Collectors.toSet());

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

    @Getter
    @NoArgsConstructor
    public static class CpnOption {

        private String name;

        private String label;

        public CpnOption(String label) {
            this(label, label);
        }

        public CpnOption(String name, String label) {
            this.name = name;
            this.label = label;
        }
    }
}
