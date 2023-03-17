package com.rick.formflow.form.cpn;


import com.google.common.collect.Lists;
import com.rick.common.util.JsonUtils;
import com.rick.formflow.form.cpn.core.AbstractCpn;
import com.rick.formflow.form.cpn.core.CpnConfigurer;
import com.rick.formflow.form.cpn.core.CpnTypeEnum;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Rick
 * @createdAt 2021-11-02 17:11:00
 */
@Component
public class CheckBox extends AbstractCpn<List<String>> {

    @Override
    public void valid(List<String> value, List<CpnConfigurer.CpnOption> options) {
        if (CollectionUtils.isNotEmpty(value) && CollectionUtils.isNotEmpty(options)) {
            if (SetUtils.difference(new HashSet<>(value), options.stream().map(CpnConfigurer.CpnOption::getName).collect(Collectors.toSet())).size() > 0) {
                throw new IllegalArgumentException("没有找到正确的选项");
            }
        }
    }

    @Override
    public CpnTypeEnum getCpnType() {
        return CpnTypeEnum.CHECKBOX;
    }

    @Override
    public List<String> httpConverter(Object value) {
        if (value instanceof String) {
            if (StringUtils.isBlank((CharSequence) value)) {
                return Collections.emptyList();
            }
            try {
                return JsonUtils.toList((String) value, String.class);
            } catch (IOException e) {
                 return Lists.newArrayList((String) value);
            }
        }

        return (List<String>) value;
    }

    @Override
    public void check(List<CpnConfigurer.CpnOption> options) {
        // TODO 选项不能重复
    }

    @Override
    public List<String> parseValue(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Boolean) {
            if (value == Boolean.TRUE) {
                return Arrays.asList(Boolean.TRUE.toString(), "1");
            } else {
                return Arrays.asList(Boolean.FALSE.toString(), "0");
            }
        }

        return super.parseValue(value);
    }
}
