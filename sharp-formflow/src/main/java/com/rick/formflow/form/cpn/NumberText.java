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
 * @createdAt 2021-11-02 20:45:00
 */
@Component
public class NumberText extends AbstractCpn<Integer> {


    @Override
    public CpnTypeEnum getCpnType() {
        return CpnTypeEnum.NUMBER_TEXT;
    }

    @Override
    public Integer parseStringValue(String value) {
        return StringUtils.isBlank(value) ? null : Integer.parseInt(value);
    }

    @Override
    public Integer httpConverter(Object value) {
        return parseStringValue((String) value);
    }

    @Override
    protected Set<ValidatorTypeEnum> internalValidatorSupports() {
        return Sets.newHashSet(ValidatorTypeEnum.SIZE);
    }

}
