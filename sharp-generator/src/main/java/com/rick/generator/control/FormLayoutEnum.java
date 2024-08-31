package com.rick.generator.control;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Rick.Xu
 * @date 2024/8/30 21:40
 */
@AllArgsConstructor
@Getter
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum FormLayoutEnum {
    INLINE("内联：默认一行3列"),
    HORIZONTAL("垂直"),
    FLUID("流式");

    public String getCode() {
        return this.name();
    }

    private final String label;
}