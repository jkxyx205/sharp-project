package com.rick.meta.config.validator;

import com.rick.db.repository.TableDAO;
import com.rick.meta.dict.model.DictType;
import com.rick.meta.dict.model.DictValue;
import com.rick.meta.dict.service.DictService;
import org.apache.commons.collections4.CollectionUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;

/**
 * @author Rick.Xu
 * @date 2024/8/19 01:56
 */
public class DictDictValueListValidator extends AbstractDictValidator implements ConstraintValidator<DictType, List<DictValue>> {

    public DictDictValueListValidator(DictService dictService, TableDAO tableDAO) {
        super(dictService, tableDAO);
    }

    @Override
    public boolean isValid(List<DictValue> dictValueList, ConstraintValidatorContext constraintValidatorContext) {
        if (CollectionUtils.isNotEmpty(dictValueList)) {
            for (DictValue dictValue : dictValueList) {
                boolean valid = isValid(constraintValidatorContext, dictValue.getCode(), label -> {
                    dictValue.setLabel(label);
                });

                if (!valid) {
                    return false;
                }
            }
        }

        return true;
    }
}
