package com.rick.db.repository.support;

import lombok.experimental.UtilityClass;

import java.util.Arrays;
import java.util.stream.Collectors;

@UtilityClass
public class CodeHelper {

    String DEFAULT_DELIMITER = ":";

    public String join(String... values) {
        return join(DEFAULT_DELIMITER, values);
    }

    public String join(CharSequence delimiter, String... values) {
        return String.join(delimiter, Arrays.stream(values)
                .map(value -> value == null ? "" : value)
                .collect(Collectors.toList()));
    }

    public String[] split(String value) {
        return split(value, DEFAULT_DELIMITER);
    }

    public String[] split(String value, CharSequence delimiter) {
        if (value == null) {
            return new String[0];
        }

        String[] values = value.split(String.valueOf(delimiter), -1);

        for (int i = 0; i < values.length; i++) {
            if (values[i].isEmpty()) {
                values[i] = null;
            }
        }

        return values;
    }
}
