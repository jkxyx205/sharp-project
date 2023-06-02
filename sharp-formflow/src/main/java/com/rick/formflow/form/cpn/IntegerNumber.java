package com.rick.formflow.form.cpn;

import com.google.common.collect.Sets;
import com.rick.formflow.form.cpn.core.AbstractCpn;
import com.rick.formflow.form.cpn.core.CpnTypeEnum;
import com.rick.formflow.form.valid.core.ValidatorTypeEnum;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * @author Rick
 * @createdAt 2023-02-24 15:05:00
 */
@Component
public class IntegerNumber extends AbstractCpn<Integer> {

    @Override
    public CpnTypeEnum getCpnType() {
        return CpnTypeEnum.INTEGER_NUMBER;
    }

    @Override
    public Integer parseValue(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof String) {
            if (StringUtils.isBlank((CharSequence) value)) {
                return null;
            }

            return Integer.parseInt((String) value);
        }
        return (Integer) value;
    }

    @Override
    public Integer httpConverter(Object value) {
        return parseValue(value);
    }

    @Override
    protected Set<ValidatorTypeEnum> internalValidatorSupports() {
        return Sets.newHashSet(ValidatorTypeEnum.SIZE);
    }
}
