package com.rick.meta.config.validator;

import com.rick.db.repository.TableDAO;
import com.rick.meta.dict.entity.Dict;
import com.rick.meta.dict.model.DictType;
import com.rick.meta.dict.service.DictService;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorContextImpl;

import javax.validation.ConstraintValidatorContext;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;


/**
 * @author Rick.Xu
 * @date 2025/11/21 09:29
 */
@AllArgsConstructor
public class AbstractDictValidator {

    private DictService dictService;

    private TableDAO tableDAO;

    public boolean isValid(ConstraintValidatorContext constraintValidatorContext, String code, Consumer<String> labelConsumer) {
        constraintValidatorContext.disableDefaultConstraintViolation();
        ConstraintValidatorContextImpl impl = (ConstraintValidatorContextImpl) constraintValidatorContext;

        DictType dictType = (DictType) impl.getConstraintDescriptor().getAnnotation();

        constraintValidatorContext.buildConstraintViolationWithTemplate(String.format(dictType.message(), code))
                .addConstraintViolation();

        String type = dictType.type();
        if (StringUtils.isNotBlank(type)) {
            Optional<Dict> optional = dictService.getDictByTypeAndName(type, code);
            if (optional.isPresent()) {
                if (Objects.nonNull(labelConsumer))
                    labelConsumer.accept(optional.get().getLabel());
                return true;
            }
        } else{
            List<Map<String, Object>> list = tableDAO.select(dictType.sql(), new Object[]{code});
            if (list.size() == 1) {
                if (Objects.nonNull(labelConsumer))
                    labelConsumer.accept((String) list.get(0).get("label"));
                return true;
            }
        }

        return false;
    }

}
