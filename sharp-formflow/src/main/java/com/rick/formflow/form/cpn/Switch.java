package com.rick.formflow.form.cpn;


import com.rick.common.util.StringUtils;
import com.rick.formflow.form.cpn.core.AbstractCpn;
import com.rick.formflow.form.cpn.core.CpnTypeEnum;
import org.springframework.stereotype.Component;

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
        return StringUtils.toBoolean(value) ? "1" : "0";
    }

}
