package com.rick.generator.control;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Rick.Xu
 * @date 2024/8/25 19:52
 */
@AllArgsConstructor
@Getter
public enum RenderTypeEnum {
    THYMELEAF("thymeleaf"),
    VUE("vue"),
    REACT("react");

    public String getCode() {
        return this.name();
    }

    private final String label;

    public static RenderTypeEnum valueOfCode(String code) {
        return valueOf(code);
    }
}