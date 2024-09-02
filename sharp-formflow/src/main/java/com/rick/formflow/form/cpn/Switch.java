package com.rick.formflow.form.cpn;


import com.rick.formflow.form.cpn.core.AbstractCpn;
import com.rick.formflow.form.cpn.core.CpnTypeEnum;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Objects;

/**
 * @author Rick
 * @createdAt 2021-11-02 17:11:00
 */
@Component
public class Switch extends AbstractCpn<String> {

    @Override
    public CpnTypeEnum getCpnType() {
        return CpnTypeEnum.SWITCH;
    }

    @Override
    public String parseValue(Object value) {
        if (value == null) {
            return null;
        } else if (value instanceof Enum) {
            return value.toString();
        } else if (value instanceof Boolean) {
            return Boolean.TRUE == value ? "1" : "0";
        }

        return super.parseValue(value);
    }

    @Override
    public String httpConverter(Object value) {
        if (Objects.isNull(value)) {
            return "0";
        } else if (CharSequence.class.isAssignableFrom(value.getClass())) {
            return StringUtils.isNotBlank((String)value) ? "1" : "0";
        } else if (Collection.class.isAssignableFrom(value.getClass())) {
            return ((Collection)value).size() > 0 ? "1" : "0";
        }

        return "1";
    }

}
