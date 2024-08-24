package com.rick.meta.config.validator;

import com.rick.meta.dict.model.DictValue;
import com.rick.meta.dict.service.DictService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorContextImpl;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Objects;

/**
 * @author Rick.Xu
 * @date 2024/8/19 01:56
 */
@Component
@RequiredArgsConstructor
public class DictValidator implements ConstraintValidator<DictValueCheck, DictValue> {

    private final DictService dictService;

    @Override
    public boolean isValid(DictValue dictValue, ConstraintValidatorContext constraintValidatorContext) {
        if (Objects.nonNull(dictValue) && StringUtils.isNotBlank(dictValue.getCode())) {
            ConstraintValidatorContextImpl impl = (ConstraintValidatorContextImpl) constraintValidatorContext;

            DictValueCheck dictValueCheck = (DictValueCheck) impl.getConstraintDescriptor().getAnnotation();
            String type = dictValueCheck.type();
            return dictService.getDictByTypeAndName(type, dictValue.getCode()).isPresent();
        }

        return true;
    }
}
