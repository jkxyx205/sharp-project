package com.rick.formflow.form.valid;

import com.rick.formflow.form.valid.core.AbstractValidator;
import com.rick.formflow.form.valid.core.ValidatorTypeEnum;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * @author Rick
 * @createdAt 2021-11-02 17:13:00
 */
@Setter
@Getter
@Component
public class NumberRegex extends AbstractValidator<String> {

    private static final Pattern PATTERN = Pattern.compile("-?\\d+(\\.\\d+)?");

    @Override
    public void valid(String value) {
        if (StringUtils.isNotBlank(value)) {
            if (!PATTERN.matcher(value).matches()) {
                throw new IllegalArgumentException(getMessage());
            }
        }
    }

    @Override
    public ValidatorTypeEnum getValidatorType() {
        return ValidatorTypeEnum.NUMBER;
    }

    @Override
    public String getMessage() {
        return String.format("数字格式不正确") ;
    }

}
