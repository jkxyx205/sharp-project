package com.rick.formflow.form.cpn;

import com.google.common.collect.Sets;
import com.rick.formflow.form.cpn.core.AbstractCpn;
import com.rick.formflow.form.cpn.core.CpnTypeEnum;
import com.rick.formflow.form.valid.DateRegex;
import com.rick.formflow.form.valid.core.Validator;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * @author Rick
 * @createdAt 2021-11-02 17:11:00
 */
@Component
public class Date extends AbstractCpn<String> {

    @Override
    public CpnTypeEnum getCpnType() {
        return CpnTypeEnum.DATE;
    }

    @Override
    public Set<Validator> cpnValidators() {
        return Sets.newHashSet(new DateRegex());
    }
}
