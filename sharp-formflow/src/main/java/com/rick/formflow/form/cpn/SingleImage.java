package com.rick.formflow.form.cpn;


import com.rick.common.util.JsonUtils;
import com.rick.formflow.form.cpn.core.AbstractCpn;
import com.rick.formflow.form.cpn.core.CpnTypeEnum;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author Rick
 * @createdAt 2021-11-02 17:11:00
 */
@Component
public class SingleImage extends AbstractCpn<Map<String, Object>> {

    @Override
    public CpnTypeEnum getCpnType() {
        return CpnTypeEnum.SINGLE_IMAGE;
    }

    @Override
    public Map<String, Object> httpConverter(Object value) {
        if (value instanceof String) {
            if (StringUtils.isBlank((CharSequence) value)) {
                return null;
            }

            return JsonUtils.toObject((String) value, Map.class);
        }

        return (Map<String, Object>) value;
    }
}