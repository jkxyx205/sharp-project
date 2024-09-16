package com.rick.formflow.form.cpn;


import com.rick.formflow.form.cpn.core.AbstractCpn;
import com.rick.formflow.form.cpn.core.CpnTypeEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @author Rick
 * @createdAt 2021-11-02 17:11:00
 */
@Component
@RequiredArgsConstructor
public class GroupSelect extends AbstractCpn<String> {

    private final Select select;

    @Override
    public CpnTypeEnum getCpnType() {
        return CpnTypeEnum.GROUP_SELECT;
    }

    @Override
    public String parseValue(Object value) {
        return select.parseValue(value);
    }

}
