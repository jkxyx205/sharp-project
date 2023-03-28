package com.rick.formflow.form.valid;

import com.rick.formflow.form.valid.core.AbstractValidator;
import com.rick.formflow.form.valid.core.ValidatorTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * @author Rick
 * @createdAt 2021-11-02 17:39:00
 */
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Component
public class TextNumberSize extends AbstractValidator<String> {

    private int min;

    private int max;

    @Override
    public void valid(String value) {
        new NumberRegex().valid(value);
        if (StringUtils.isNotBlank(value)) {
            BigDecimal bigDecimalValue = NumberUtils.createBigDecimal(value);
            if (bigDecimalValue.compareTo(BigDecimal.valueOf(min)) == -1 || bigDecimalValue.compareTo(BigDecimal.valueOf(max)) == 1 ) {
                throw new IllegalArgumentException(getMessage());
            }
        }
    }

    @Override
    public ValidatorTypeEnum getValidatorType() {
        return ValidatorTypeEnum.TEXT_NUMBER_SIZE;
    }

    @Override
    public String getMessage() {
        return String.format("大小范围是%d - %d", min, max) ;
    }


}
