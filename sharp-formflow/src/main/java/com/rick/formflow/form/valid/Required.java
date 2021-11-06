package com.rick.formflow.form.valid;

import com.rick.formflow.form.valid.core.AbstractValidator;
import com.rick.formflow.form.valid.core.ValidatorTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Objects;

/**
 * @author Rick
 * @createdAt 2021-11-02 17:12:00
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Component
public class Required extends AbstractValidator<Object> {

    private boolean required = true;

    @Override
    public void valid(Object o) {
        if (required && ((Objects.isNull(o)) ||
                (Collection.class.isAssignableFrom(o.getClass()) && ((Collection)o).size() == 0)||
                o instanceof CharSequence && StringUtils.isBlank((CharSequence) o))) {
            throw new IllegalArgumentException(getMessage());
        }
    }

    @Override
    public ValidatorTypeEnum getValidatorType() {
        return ValidatorTypeEnum.REQUIRED;
    }

    @Override
    public String getMessage() {
        return "必填项";
    }
}
