package com.rick.formflow.form.cpn;


import com.google.common.collect.Lists;
import com.rick.formflow.form.cpn.core.AbstractCpn;
import com.rick.formflow.form.cpn.core.CpnTypeEnum;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * @author Rick
 * @createdAt 2021-11-02 17:11:00
 */
@Component
public class Attachment extends AbstractCpn<List<String>> {

    @Override
    public CpnTypeEnum getCpnType() {
        return CpnTypeEnum.FILE;
    }

    @Override
    public List<String> httpConverter(Object value) {
        if (value instanceof String) {
            if (StringUtils.isBlank((CharSequence) value)) {
                return Collections.emptyList();
            }
            return Lists.newArrayList((String) value);
        }

        return (List<String>) value;
    }
}