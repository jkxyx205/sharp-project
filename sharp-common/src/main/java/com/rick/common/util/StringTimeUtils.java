package com.rick.common.util;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Objects;


/**
 * @author Rick
 * @version 1.0.0
 * @createdAt  2021-01-17 12:42:00
 */
@UtilityClass
public final class StringTimeUtils {

    private static final String DATE_FORMAT_REGEX = "\\d{4}-\\d{2}-\\d{2}";

    private static final String YYYY_MM_DD_HH_FORMAT_REGEX = "\\d{4}-\\d{2}-\\d{2} \\d{2}";

    private static final String YYYY_MM_DD_HH_MM_FORMAT_REGEX = "\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}";

    private static final String DATETIME_FORMAT_REGEX = "\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}";

    private static final String SQL_TIME = "yyyy-MM-dd HH:mm:ss";

    private static final String TIME_SHORT = "HH:mm";

    private static final String DATE_START = "00:00:00";

    private static final String DATE_END = "23:59:59";

    private static final String HOUR_START = "00:00";

    private static final String HOUR_END = "59:59";

    private static final DateTimeFormatter SQL_TIME_FORMATTER = DateTimeFormatter.ofPattern(SQL_TIME);

    private static final DateTimeFormatter TIME_SHORT_FORMATTER = DateTimeFormatter.ofPattern(TIME_SHORT);

    /**
     * 将yyyy-MM-dd[ HH:mm:ss]转成日期类型
     * @param value
     * @return
     */
    public static LocalDateTime toLocalDateTime(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }

        String formatValue = null;
        if (isDate(value)) {
            formatValue = value + " 00:00:00";
        } else if (isDateHour(value)) {
            formatValue = value + ":00:00";
        } else if (isDateMinute(value)) {
            formatValue = value + ":00";
        } else if (value.matches(DATETIME_FORMAT_REGEX)) {
            formatValue = value;
        }

        return LocalDateTime.parse(formatValue, SQL_TIME_FORMATTER).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    public static Instant toInstant(String value) {
        if (Objects.isNull(value)) {
            return null;
        }

        LocalDateTime localDateTime = toLocalDateTime(value);
        return localDateTime.atZone(ZoneId.systemDefault()).toInstant();
    }

    public static LocalDate getLocalDate(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }

        return toLocalDateTime(value).toLocalDate();
    }

    public static LocalTime getTime(String value) {
        return toLocalDateTime(value).toLocalTime();
    }

    /**
     *
     * @param value 06:23
     * @return
     */
    public static LocalTime toTime(String value) {
        return LocalTime.parse(value, TIME_SHORT_FORMATTER);
    }

    /**
     * @param value 2020-01-01
     * @return 2020-01-01 00:00:00
     */
    public static String appendStartSuffix(String value) {
        if(StringUtils.isBlank(value)) {
            return null;
        }
        if (isDate(value)) {
            return value + " " + DATE_START;
        }

        if (isDateHour(value)) {
            return value + ":" + HOUR_START;
        }

        return value;
    }

    /**
     * @param value 2020-01-01
     * @return 2020-01-01 23:59:59
     */
    public static String appendEndSuffix(String value) {
        if(StringUtils.isBlank(value)) {
            return null;
        }
        if (isDate(value)) {
            return value + " " + DATE_END;
        }

        if (isDateHour(value)) {
            return value + ":" + HOUR_END;
        }

        return value;
    }

    private static boolean isDate(String value) {
        return value.matches(DATE_FORMAT_REGEX);
    }

    private static boolean isDateHour(String value) {
        return value.matches(YYYY_MM_DD_HH_FORMAT_REGEX);
    }

    private static boolean isDateMinute(String value) {
        return value.matches(YYYY_MM_DD_HH_MM_FORMAT_REGEX);
    }
}
