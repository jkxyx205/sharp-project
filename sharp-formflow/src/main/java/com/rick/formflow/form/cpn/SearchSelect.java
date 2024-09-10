package com.rick.formflow.form.cpn;


import com.rick.formflow.form.cpn.core.AbstractCpn;
import com.rick.formflow.form.cpn.core.CpnTypeEnum;
import org.springframework.stereotype.Component;

/**
 * @author Rick
 * @createdAt 2021-11-02 17:11:00
 */
@Component
public class SearchSelect extends AbstractCpn<String> {

    private final Select select;

    public SearchSelect(Select select) {
        this.select = select;
    }

    @Override
    public CpnTypeEnum getCpnType() {
        return CpnTypeEnum.SEARCH_SELECT;
    }

    @Override
    public String parseValue(Object value) {
        return select.parseValue(value);
    }

}
