package com.rick.formflow.form.cpn;

import com.rick.formflow.form.cpn.core.AbstractCpn;
import com.rick.formflow.form.cpn.core.CpnConfigurer;
import com.rick.formflow.form.cpn.core.CpnTypeEnum;
import com.rick.formflow.form.valid.core.Validator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Rick
 * @createdAt 2021-11-02 17:11:00
 */
@Component
public class Table extends AbstractCpn<List<List>> {

    @Override
    public CpnTypeEnum getCpnType() {
        return CpnTypeEnum.TABLE;
    }

    @Override
    public void valid(List<List> value, List<CpnConfigurer.CpnOption> options) {
        // 验证控件特性
        for (Validator cpnValidator : cpnValidators()) {
            cpnValidator.valid(value);
        }
    }

    @Override
    public List<List> httpConverter(Object value) {
        return super.httpConverter(value).stream().filter(v -> {
            List<String> rows = (List)v;
            // 忽略所有为空的行
            return !rows.stream().allMatch(d -> StringUtils.isBlank(d));
        }).collect(Collectors.toList());
    }
}
