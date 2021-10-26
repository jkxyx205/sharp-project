package com.rick.common.util;

import lombok.experimental.UtilityClass;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;


/**
 * @author Rick
 * @version 1.0.0
 * @createdAt  2021-01-17 12:42:00
 */
@UtilityClass
public final class Time2StringUtils {

    private static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static String format(LocalDateTime dateTime) {
        return format(dateTime, DEFAULT_FORMATTER);
    }

    public static String format(LocalDateTime dateTime, DateTimeFormatter formatter) {
        return Objects.isNull(dateTime) ? null : formatter.format(dateTime);
    }

    public static String format(Instant instant) {
        return format(instant, DEFAULT_FORMATTER);
    }

    public static String format(Instant instant, DateTimeFormatter formatter) {
        return Objects.isNull(instant) ? null : formatter.format(instant);
    }

    public static String format(long milliseconds) {
        return format(milliseconds, DEFAULT_FORMATTER);
    }

    public static String format(long milliseconds, DateTimeFormatter formatter) {
        return format(Instant.ofEpochMilli(milliseconds), formatter);
    }
}
