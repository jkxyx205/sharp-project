package com.rick.formflow.form.cpn.core;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Rick
 * @createdAt 2021-11-02 17:14:00
 */
@AllArgsConstructor
@Getter
public enum CpnTypeEnum {
    TEXT("短文本"),
    TEXTAREA("长文本"),
    DATE_TEXT("日期文本"),
    SELECT("选项"),
    RADIO("单选"),
    NUMBER_TEXT("数字"),
    CHECKBOX("多选"),
    MOBILE("手机号"),
    FILE("文件"),
    EMAIL("邮箱"),
    DATE("日期"),
    TABLE("表格");

    @JsonValue
    public String getCode() {
        return this.name();
    }

    private final String label;

    public static CpnTypeEnum valueOfCode(String code) {
        return valueOf(code);
    }
}