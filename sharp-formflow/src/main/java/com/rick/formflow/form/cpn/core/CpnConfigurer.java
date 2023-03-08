package com.rick.formflow.form.cpn.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Sets;
import com.rick.common.util.JsonUtils;
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

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

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
    private String[] options;

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
                    Class<? extends Validator> validatorType = ValidatorManager.getValidatorByType(ValidatorTypeEnum.valueOfCode((String) validatorInfo.get("validatorType")));
                    validatorList.add(JsonUtils.toObject(JsonUtils.toJson(validatorInfo), validatorType));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return Objects.isNull(validatorList) ? Collections.emptySet() : validatorList;
    }
}
