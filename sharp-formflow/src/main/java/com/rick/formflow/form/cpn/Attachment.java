package com.rick.formflow.form.cpn;


import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.rick.common.util.JsonUtils;
import com.rick.formflow.form.cpn.core.AbstractCpn;
import com.rick.formflow.form.cpn.core.CpnTypeEnum;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @author Rick
 * @createdAt 2021-11-02 17:11:00
 */
@Component
public class Attachment extends AbstractCpn<List<Map<String, Object>>> {

    @Override
    public CpnTypeEnum getCpnType() {
        return CpnTypeEnum.FILE;
    }

    @Override
    public List<Map<String, Object>> httpConverter(Object value) {
        if (value instanceof String) {
            if (StringUtils.isBlank((CharSequence) value)) {
                return null;
            }
            List<Map> list = JsonUtils.toList((String) value, Map.class);
            List<Map<String,  Object>> result = Lists.newArrayListWithExpectedSize(list.size());
            for (Map map : list) {
                result.add(Maps.newHashMap(map));
            }
            return result;
        }

        return (List<Map<String, Object>>) value;
    }
}