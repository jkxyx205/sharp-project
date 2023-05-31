package com.rick.formflow.form.valid;

import com.rick.formflow.form.valid.core.AbstractValidator;
import com.rick.formflow.form.valid.core.ValidatorTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Rick
 * @createdAt 2021-11-02 17:13:00
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CustomizeRegex extends AbstractValidator<String> {

    private String regex;

    private String message;

    @Override
    public void valid(String value) {
        if (StringUtils.isNotBlank(value)) {
            if (!value.matches(regex)) {
                throw new IllegalArgumentException(getMessage());
            }
        }
    }

    @Override
    public ValidatorTypeEnum getValidatorType() {
        return ValidatorTypeEnum.REGEX;
    }

    @Override
    public String getMessage() {
        return message;
    }

}
