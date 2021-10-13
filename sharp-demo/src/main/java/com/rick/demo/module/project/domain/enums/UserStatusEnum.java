package com.rick.demo.module.project.domain.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Rick
 * @createdAt 2021-10-11 20:00:00
 */
@AllArgsConstructor
@Getter
public enum UserStatusEnum {
    LOCKED("LOCKED"),
    NORMAL("NORMAL");

    @JsonValue
    public String getCode() {
        return this.name();
    }
    private final String label;
    public static UserStatusEnum valueOfCode(String code) {
        return valueOf(code);
    }
}