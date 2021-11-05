package com.rick.formflow.form.valid;

import com.rick.formflow.form.valid.core.AbstractValidator;
import com.rick.formflow.form.valid.core.ValidatorTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * @author Rick
 * @createdAt 2021-11-02 17:39:00
 */
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Component
public class Size extends AbstractValidator<Number> {

    private int min;

    private int max;

    @Override
    public void valid(Number value) {
        if (Objects.nonNull(value)) {
            BigDecimal bigDecimalValue = NumberUtils.createBigDecimal(String.valueOf(value));

            if (bigDecimalValue.compareTo(BigDecimal.valueOf(min)) == -1 || bigDecimalValue.compareTo(BigDecimal.valueOf(max)) == 1 ) {
                throw new IllegalArgumentException(getMessage());
            }
        }
    }

    @Override
    public ValidatorTypeEnum getValidatorType() {
        return ValidatorTypeEnum.SIZE;
    }

    @Override
    public String getMessage() {
        return String.format("大小范围是%d - %d", min, max) ;
    }


}
