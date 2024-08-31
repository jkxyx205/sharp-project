package com.rick.generator.control;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Rick.Xu
 * @date 2024/8/31 05:16
 */
@AllArgsConstructor
@Getter
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum DictCategoryEnum {
    ENUM("ENUM"),
    DICT_VALUE("DICT_VALUE");

    public String getCode() {
        return this.name();
    }

    private final String label;

    public static DictCategoryEnum valueOfCode(String code) {
        return valueOf(code);
    }
}