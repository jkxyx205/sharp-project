package com.rick.common.http.convert;

import org.springframework.core.convert.converter.Converter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class LocalDateTimeToInstantConverter implements Converter<LocalDateTime, Instant> {
    @Override
    public Instant convert(LocalDateTime localDateTime) {
        return localDateTime.atZone(ZoneId.systemDefault()).toInstant();
    }
}