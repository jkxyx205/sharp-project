package com.rick.formflow.form.cpn;

import com.rick.common.util.JsonUtils;
import com.rick.formflow.form.cpn.core.AbstractCpn;
import com.rick.formflow.form.cpn.core.CpnTypeEnum;
import com.rick.formflow.form.valid.core.Validator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

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
    public void valid(List<List> value, String[] options) {
        // 验证控件特性
        for (Validator cpnValidator : cpnValidators()) {
            cpnValidator.valid(value);
        }
    }

    @Override
    public List<List> parseStringValue(String value) {
        try {
            if (StringUtils.isBlank(value)) {
                return Collections.emptyList();
            }
            return JsonUtils.toList(value, List.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
