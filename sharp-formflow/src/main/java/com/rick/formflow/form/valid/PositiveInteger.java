package com.rick.formflow.form.valid;

import com.rick.formflow.form.valid.core.AbstractValidator;
import com.rick.formflow.form.valid.core.ValidatorTypeEnum;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @author Rick
 * @createdAt 2021-11-02 21:21:00
 */
@Setter
@Getter
@Component
public class PositiveInteger extends AbstractValidator<Integer> {

    @Override
    public void valid(Integer value) {
        if (Objects.nonNull(value) && value < 0) {
            throw new IllegalArgumentException(getMessage());
        }
    }

    @Override
    public ValidatorTypeEnum getValidatorType() {
        return ValidatorTypeEnum.POSITIVE_INTEGER;
    }

    @Override
    public String getMessage() {
        return "数字必须大于等于0";
    }

}
