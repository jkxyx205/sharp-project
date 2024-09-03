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
public class MultipleSelect extends AbstractCpn<List<String>> {

    private final CheckBox checkBox;

    public MultipleSelect(CheckBox checkBox) {
        this.checkBox = checkBox;
    }

    @Override
    public void valid(List<String> value, List<CpnConfigurer.CpnOption> options) {
        checkBox.valid(value, options);
    }

    @Override
    public CpnTypeEnum getCpnType() {
        return CpnTypeEnum.MULTIPLE_SELECT;
    }

    @Override
    public List<String> httpConverter(Object value) {
        return checkBox.httpConverter(value);
    }

    @Override
    public List<String> parseValue(Object value) {
        return checkBox.parseValue(value);
    }
}
