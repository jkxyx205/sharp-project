package com.rick.meta.config.validator;

import com.rick.meta.dict.model.DictType;
import com.rick.meta.dict.model.DictValue;
import com.rick.meta.dict.service.DictService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Objects;

/**
 * @author Rick.Xu
 * @date 2024/8/19 01:56
 */
public class DictDictValueValidator extends AbstractDictValidator implements ConstraintValidator<DictType, DictValue> {

    public DictDictValueValidator(DictService dictService, JdbcTemplate jdbcTemplate) {
        super(dictService, jdbcTemplate);
    }

    @Override
    public boolean isValid(DictValue dictValue, ConstraintValidatorContext constraintValidatorContext) {
        if (Objects.nonNull(dictValue) && StringUtils.isNotBlank(dictValue.getCode())) {
            return isValid(constraintValidatorContext, dictValue.getCode(), label -> {
                dictValue.setLabel(label);
            });
        }

        return true;
    }
}
