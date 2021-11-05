package com.rick.formflow.form.valid;

import com.rick.formflow.form.valid.core.AbstractValidator;
import com.rick.formflow.form.valid.core.ValidatorTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * @author Rick
 * @createdAt 2021-11-02 17:13:00
 */
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Component
public class Length extends AbstractValidator<String> {

    private int min;

    private int max;

    public Length(int max) {
        this(0, max);
    }

    @Override
    public void valid(String value) {
        if (StringUtils.isNotBlank(value)) {
            if (value.length() < min || value.length() > max) {
                throw new IllegalArgumentException(getMessage());
            }
        }
    }

    @Override
    public ValidatorTypeEnum getValidatorType() {
        return ValidatorTypeEnum.LENGTH;
    }

    @Override
    public String getMessage() {
        return String.format("长度范围 %d - %d 个字符", min, max) ;
    }

}
