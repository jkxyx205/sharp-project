package com.rick.formflow.form.cpn.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Sets;
import com.rick.common.util.JsonUtils;
import com.rick.db.dto.BasePureEntity;
import com.rick.db.plugin.dao.annotation.ColumnName;
import com.rick.db.plugin.dao.annotation.TableName;
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
import javax.validation.constraints.NotNull;
import java.io.IOException;
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
@TableName(value = "sys_form_configurer")
public class CpnConfigurer extends BasePureEntity {

    @NotBlank
    private String label;

    @ColumnName("type")
    @NotNull
    private CpnTypeEnum cpnType;

    @JsonIgnore
    @Transient
    private Set<Validator> validatorList;

    /**
     * 手动装配validatorsMap到validatorList
     */
    @ColumnName("validators")
    private Set<Map<String, ?>> validators;

    @ColumnName("options")
    private String[] options;

    @ColumnName("default_value")
    private String defaultValue;

    @ColumnName("placeholder")
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
        return validators;
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

        return validatorList;
    }
}
