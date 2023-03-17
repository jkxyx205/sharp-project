package com.rick.formflow.form.cpn;

import com.google.common.collect.Sets;
import com.rick.common.util.Time2StringUtils;
import com.rick.formflow.form.cpn.core.AbstractCpn;
import com.rick.formflow.form.cpn.core.CpnTypeEnum;
import com.rick.formflow.form.valid.DateRegex;
import com.rick.formflow.form.valid.core.Validator;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
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

    @Override
    public String parseValue(Object value) {
        if (value instanceof LocalDate) {
            return Time2StringUtils.format((LocalDate) value);
        } else if (value instanceof java.sql.Date) {
            return value.toString();
        } else if (value instanceof java.util.Date) {
            return Time2StringUtils.format((java.util.Date) value);
        }

        return super.parseValue(value);
    }
}
