package com.rick.formflow.form.valid.core;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Rick
 * @createdAt 2021-11-03 12:14:00
 */
@AllArgsConstructor
@Getter
public enum ValidatorTypeEnum {

    LENGTH("Length"),
    POSITIVE_INTEGER("Positive Integer"),
    NUMBER("number"),
    REQUIRED("Required"),
    SIZE("Size"),
    TEXT_NUMBER_SIZE("TextNumberSize"),
    DATE("Date"),
    TIME("Time"),
    EMAIL("Email"),
    MOBILE("Mobile"),
    DECIMAL("Decimal");

    @JsonValue
    public String getCode() {
        return this.name();
    }

    private final String label;

    public static ValidatorTypeEnum valueOfCode(String code) {
        return valueOf(code);
    }
}