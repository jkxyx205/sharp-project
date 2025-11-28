package com.rick.meta.config.validator;

import com.rick.meta.dict.model.DictType;
import com.rick.meta.dict.service.DictService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;

/**
 * @author Rick.Xu
 * @date 2024/8/19 01:56
 */
public class DictStringListValidator extends AbstractDictValidator implements ConstraintValidator<DictType, List<String>> {

    public DictStringListValidator(DictService dictService, JdbcTemplate jdbcTemplate) {
        super(dictService, jdbcTemplate);
    }

    @Override
    public boolean isValid(List<String> dictCodes, ConstraintValidatorContext constraintValidatorContext) {
        if (CollectionUtils.isNotEmpty(dictCodes)) {
            for (String dictCode : dictCodes) {
                boolean valid = isValid(constraintValidatorContext, dictCode, null);
                if (!valid) {
                    return false;
                }
            }

        }

        return true;
    }
}
