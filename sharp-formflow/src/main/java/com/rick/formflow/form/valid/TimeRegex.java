package com.rick.formflow.form.valid;

import com.rick.formflow.form.valid.core.AbstractValidator;
import com.rick.formflow.form.valid.core.ValidatorTypeEnum;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * @author Rick
 * @createdAt 2021-11-02 17:13:00
 */
@Setter
@Getter
@Component
public class TimeRegex extends AbstractValidator<String> {

    private static final String regex = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$";

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
        return ValidatorTypeEnum.TIME;
    }

    @Override
    public String getMessage() {
        return String.format("时间格式不正确，正确的格式是HH:mm") ;
    }

}
