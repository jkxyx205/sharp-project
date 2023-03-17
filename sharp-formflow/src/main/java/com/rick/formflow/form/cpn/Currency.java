package com.rick.formflow.form.cpn;

import com.google.common.collect.Sets;
import com.rick.formflow.form.cpn.core.AbstractCpn;
import com.rick.formflow.form.cpn.core.CpnTypeEnum;
import com.rick.formflow.form.valid.NumberRegex;
import com.rick.formflow.form.valid.core.Validator;
import com.rick.formflow.form.valid.core.ValidatorTypeEnum;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Set;

/**
 * @author Rick
 * @createdAt 2021-11-02 20:45:00
 */
@Component
public class Currency extends AbstractCpn<BigDecimal> {


    @Override
    public CpnTypeEnum getCpnType() {
        return CpnTypeEnum.CURRENCY;
    }

    @Override
    public BigDecimal parseValue(Object value) {
        return value == null ? null : (value instanceof String ? super.parseValue(value) : (BigDecimal) value);
    }

    @Override
    public BigDecimal httpConverter(Object value) {
        return parseValue(value);
    }

    @Override
    protected Set<ValidatorTypeEnum> internalValidatorSupports() {
        return Sets.newHashSet(ValidatorTypeEnum.SIZE);
    }

    @Override
    public Set<Validator> cpnValidators() {
        return Sets.newHashSet(new NumberRegex());
    }

}
