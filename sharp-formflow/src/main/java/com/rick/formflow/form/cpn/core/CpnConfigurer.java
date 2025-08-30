package com.rick.formflow.form.cpn.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.rick.common.util.JsonUtils;
import com.rick.common.util.ReflectUtils;
import com.rick.db.repository.Column;
import com.rick.db.repository.Table;
import com.rick.db.repository.Transient;
import com.rick.db.repository.model.BaseEntity;
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
import java.io.Serializable;
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
public class CpnConfigurer extends BaseEntity<Long> {

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
    private List<Validator> validatorList;

    /**
     * 手动装配validatorsMap到validatorList
     */
    @Column(value = "validators", columnDefinition = "varchar(512)")
    private Set<Map<String, ?>> validators;

    @Column("options")
    private List<CpnOption> options;

    @Column("data_source")
    private String datasource;

    @Column("default_value")
    private String defaultValue;

    @Column("placeholder")
    private String placeholder;

    @Column("is_disabled")
    private Boolean disabled;

    private String cpnValueConverterName;

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

    public List<Validator> getValidatorList() {
        if (Objects.isNull(validatorList) && Objects.nonNull(validators)) {
            Cpn cpnByType = CpnManager.getCpnByType(cpnType);
            validatorList = Lists.newArrayListWithExpectedSize(validators.size() + cpnByType.cpnValidators().size());
            validatorList.addAll(cpnByType.cpnValidators());

            for (Map<String, ?> validatorInfo : validators) {
                Class<? extends Validator> validatorType = ValidatorManager.getValidatorClassByType(ValidatorTypeEnum.valueOfCode((String) validatorInfo.get("validatorType")));
                validatorList.add(JsonUtils.toObject(JsonUtils.toJson(validatorInfo), validatorType));
            }
        }

        return Objects.isNull(validatorList) ? Collections.emptyList() : validatorList;
    }

    public Map<String, Object> getValidatorProperties() {
        List<Validator> validatorList = getValidatorList();
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

    public Map<String, String> getOptionMap() {
        if (options == null) {
            return Collections.emptyMap();
        }

        return options.stream().collect(Collectors.toMap(CpnOption::getName, CpnOption::getLabel));
    }

    @Getter
    @NoArgsConstructor
    public static class CpnOption implements Serializable {

        private String name;

        private String label;

        public CpnOption(String label) {
            this(label, label);
        }

        public CpnOption(String name, String label) {
            this.name = name;
            this.label = label;
        }

        public String getName() {
            return StringUtils.defaultString(name, label);
        }
    }
}
