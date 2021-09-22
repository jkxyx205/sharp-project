package com.rick.common.util;

import lombok.experimental.UtilityClass;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;

/**
 * @author Rick
 * @createdAt 2021-09-22 19:32:00
 */
@UtilityClass
public class DateConvertUtils {

    public static LocalDate unixTimeToLocalDate(Long milliseconds) {
        LocalDateTime localDateTime = unixTimeToLocalDateTime(milliseconds);
        return Objects.nonNull(localDateTime) ? localDateTime.toLocalDate() : null;
    }

    public static LocalDateTime unixTimeToLocalDateTime(Long milliseconds) {
        return Objects.isNull(milliseconds) ? null : LocalDateTime.ofInstant(Instant.ofEpochMilli(milliseconds), ZoneId.systemDefault());
    }
}
