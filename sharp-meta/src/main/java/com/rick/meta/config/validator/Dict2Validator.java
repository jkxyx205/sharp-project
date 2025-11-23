package com.rick.meta.config.validator;

import com.rick.db.repository.TableDAO;
import com.rick.meta.dict.model.DictType;
import com.rick.meta.dict.service.DictService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * @author Rick.Xu
 * @date 2024/8/19 01:56
 */
@Component
public class Dict2Validator extends AbstractDictValidator implements ConstraintValidator<DictType, String> {

    public Dict2Validator(DictService dictService, TableDAO tableDAO) {
        super(dictService, tableDAO);
    }

    @Override
    public boolean isValid(String dictCode, ConstraintValidatorContext constraintValidatorContext) {
        if (StringUtils.isNotBlank(dictCode)) {
            return isValid(constraintValidatorContext, dictCode, null);
        }

        return true;
    }
}
