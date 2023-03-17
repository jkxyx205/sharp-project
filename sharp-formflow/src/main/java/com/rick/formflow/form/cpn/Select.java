package com.rick.formflow.form.cpn;


import com.rick.formflow.form.cpn.core.AbstractCpn;
import com.rick.formflow.form.cpn.core.CpnConfigurer;
import com.rick.formflow.form.cpn.core.CpnTypeEnum;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Rick
 * @createdAt 2021-11-02 17:11:00
 */
@Component
public class Select extends AbstractCpn<String> {

    @Override
    public CpnTypeEnum getCpnType() {
        return CpnTypeEnum.SELECT;
    }

    @Override
    public void check(List<CpnConfigurer.CpnOption> options) {
        // TODO 选项不能重复
    }

    @Override
    public String parseValue(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Enum) {
            return value.toString();
        }

        return super.parseValue(value);
    }

}
