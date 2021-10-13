package com.rick.demo.module.project.domain.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Rick
 * @createdAt 2021-10-11 19:59:00
 */
@AllArgsConstructor
@Getter
public enum SexEnum {
    UNKNOWN(0, "Unknown"),
    MALE(1, "Male"),
    FEMALE(2, "Female");
    private static final Map<Integer, SexEnum> codeMap = new HashMap<>();
    static {
        for (SexEnum e : values()) {
            codeMap.put(e.code, e);
        }
    }
    private final int code;
    private final String label;
    @JsonValue
    public int getCode() {
        return this.code;
    }
    public static SexEnum valueOfCode(int code) {
        return codeMap.get(code);
    }
}