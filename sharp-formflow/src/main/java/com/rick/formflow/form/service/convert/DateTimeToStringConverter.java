package com.rick.formflow.form.service.convert;

import com.rick.common.util.Time2StringUtils;
import com.rick.formflow.form.service.CpnValueConverter;

import java.time.LocalDateTime;

/**
 * @author Rick.Xu
 * @date 2023/6/3 23:10
 */
public class DateTimeToStringConverter implements CpnValueConverter<LocalDateTime, String> {

    @Override
    public String convert(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return Time2StringUtils.format(dateTime);
    }
}
