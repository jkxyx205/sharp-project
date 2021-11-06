package com.rick.formflow.form.cpn;

import com.google.common.collect.Sets;
import com.rick.formflow.form.cpn.core.AbstractCpn;
import com.rick.formflow.form.cpn.core.CpnTypeEnum;
import com.rick.formflow.form.valid.core.ValidatorTypeEnum;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * @author Rick
 * @createdAt 2021-11-02 17:11:00
 */
@Component
public class TextArea extends AbstractCpn<String> {

    @Override
    public CpnTypeEnum getCpnType() {
        return CpnTypeEnum.TEXTAREA;
    }

    @Override
    protected Set<ValidatorTypeEnum> internalValidatorSupports() {
        return Sets.newHashSet(ValidatorTypeEnum.LENGTH);
    };

}
