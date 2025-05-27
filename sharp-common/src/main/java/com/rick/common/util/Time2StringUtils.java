package com.rick.common.util;

import lombok.experimental.UtilityClass;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.Objects;


/**
 * @author Rick
 * @version 1.0.0
 * @createdAt  2021-01-17 12:42:00
 */
@UtilityClass
public final class Time2StringUtils {

    private static final DateTimeFormatter DEFAULT_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    private static final DateTimeFormatter DEFAULT_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault());

    public static String format(Date date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return format(date, simpleDateFormat);
    }

    public static String format(Date date, SimpleDateFormat simpleDateFormat) {
        if (date == null) {
            return null;
        }

        return simpleDateFormat.format(date);
    }

    public static String format(LocalDate localDate) {
        return format(localDate, DEFAULT_DATE_FORMATTER);
    }

    public static String format(LocalDateTime dateTime) {
        return format(dateTime, DEFAULT_TIME_FORMATTER);
    }

    public static String format(Instant instant) {
        return format(instant, DEFAULT_TIME_FORMATTER);
    }

    public static String format(long milliseconds) {
        return format(milliseconds, DEFAULT_TIME_FORMATTER);
    }

    public static String format(long milliseconds, DateTimeFormatter formatter) {
        return format(Instant.ofEpochMilli(milliseconds), formatter);
    }

    public static String format(TemporalAccessor temporal, DateTimeFormatter formatter) {
        return Objects.isNull(temporal) ? null : formatter.format(temporal);
    }
}
