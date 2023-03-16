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
public enum TestCodeEnum {
    UNKNOWN(1, "Unknown"),
    CODE(0, "Male"),
    CODE2(10, "Male"),
    CODE3(11, "Male");

    private static final Map<Integer, TestCodeEnum> codeMap = new HashMap<>();
    static {
        for (TestCodeEnum e : values()) {
            codeMap.put(e.code, e);
        }
    }
    private final int code;
    private final String label;
    @JsonValue
    public int getCode() {
        return this.code;
    }
    public static TestCodeEnum valueOfCode(int code) {
        return codeMap.get(code);
    }
}