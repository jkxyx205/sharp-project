package com.rick.formflow.form.cpn;


import com.rick.common.util.JsonUtils;
import com.rick.formflow.form.cpn.core.AbstractCpn;
import com.rick.formflow.form.cpn.core.CpnConfigurer;
import com.rick.formflow.form.cpn.core.CpnTypeEnum;
import com.rick.meta.dict.model.DictValue;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.*;
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
        if (value == null) {
            return Collections.emptyList();
        }

        if (value instanceof String) {
            if (StringUtils.isBlank((CharSequence) value)) {
                return Collections.emptyList();
            }

            if (!value.toString().startsWith("[")) {
                value = "[\""+value+"\"]";
            }

            return JsonUtils.toList((String) value, String.class);
        }

        return (List<String>) value;
    }

    @Override
    public List<String> parseValue(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Collection) {
            if (CollectionUtils.isEmpty((Collection<?>) value)) {
                return Collections.emptyList();
            }

            Class<?> classGenericsType = ((Collection)value).iterator().next().getClass();

            if (classGenericsType.isEnum()) {
                return ((Collection<?>) value).stream().map((en -> ((Enum)en).name())).collect(Collectors.toList());
            } else if (classGenericsType == DictValue.class) {
                return ((Collection<?>) value).stream().map((dictValue -> ((DictValue)dictValue).getCode())).collect(Collectors.toList());
            } else if (classGenericsType == String.class) {
                return (List<String>) value;
            }

            throw new IllegalArgumentException(classGenericsType + "无法决断，请指定字段 map");
        }

        if (value instanceof Boolean) {
            if (value == Boolean.TRUE) {
                return Arrays.asList(Boolean.TRUE.toString(), "1");
            } else {
                return Arrays.asList(Boolean.FALSE.toString(), "0");
            }
        } else if (value instanceof CharSequence) {
            return Arrays.asList(value.toString());
        } else if (value.getClass().isEnum()) {
            return Arrays.asList(((Enum)value).name());
        } else if (value.getClass() == DictValue.class) {
            return Arrays.asList(((DictValue)value).getCode());
        }

        return super.parseValue(value);
    }
}
