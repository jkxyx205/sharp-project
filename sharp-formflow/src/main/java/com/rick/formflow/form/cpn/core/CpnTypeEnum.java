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
    LABEL("标签"),
    TEXT("短文本"),
    TEXTAREA("长文本"),
    SELECT("选项"),
    MULTIPLE_SELECT("多选项"),
    SWITCH("二单选一"),
    RADIO("单选"),
    NUMBER_TEXT("数字"),
    INTEGER_NUMBER("数字"),
    CURRENCY("金额"),
    CHECKBOX("多选"),
    MOBILE("手机号"),
    FILE("文件"),
    EMAIL("邮箱"),
    DATE("日期"),
    TIME("时间"),
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