package com.rick.formflow.form.cpn;

import com.google.common.collect.Sets;
import com.rick.formflow.form.cpn.core.AbstractCpn;
import com.rick.formflow.form.cpn.core.CpnTypeEnum;
import com.rick.formflow.form.valid.MobileRegex;
import com.rick.formflow.form.valid.core.Validator;
import com.rick.formflow.form.valid.core.ValidatorTypeEnum;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * @author Rick
 * @createdAt 2021-11-05 22:05:00
 */
@Component
public class Mobile extends AbstractCpn<String> {

    @Override
    public CpnTypeEnum getCpnType() {
        return CpnTypeEnum.MOBILE;
    }

    @Override
    protected Set<ValidatorTypeEnum> internalSupports() {
        return Sets.newHashSet(ValidatorTypeEnum.MOBILE);
    };

    @Override
    public Set<Validator> cpnValidators() {
        return Sets.newHashSet(new MobileRegex());
    }
}
