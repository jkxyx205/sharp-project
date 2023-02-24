package com.rick.formflow.form.valid;

import com.rick.formflow.form.valid.core.AbstractValidator;
import com.rick.formflow.form.valid.core.ValidatorTypeEnum;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * @author Rick
 * @createdAt 2021-11-02 21:21:00
 */
@Setter
@Getter
@Component
public class StringIntegerNumber extends AbstractValidator<String> {

    private static final String INTEGER_REGEX = "\\d+";

    @Override
    public void valid(String value) {
        if (StringUtils.isNotBlank(value) && !value.matches(INTEGER_REGEX)) {
            throw new IllegalArgumentException(getMessage());
        }
    }

    @Override
    public ValidatorTypeEnum getValidatorType() {
        return ValidatorTypeEnum.POSITIVE_INTEGER;
    }

    @Override
    public String getMessage() {
        return "不正确的数字格式";
    }

}
