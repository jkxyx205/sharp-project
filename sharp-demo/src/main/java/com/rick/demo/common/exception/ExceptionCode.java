package com.rick.demo.common.exception;

import com.rick.common.http.model.ExceptionResult;
import lombok.Getter;
import lombok.ToString;

/**
 * @author Rick
 * @createdAt 2021-10-13 12:17:00
 */
@Getter
@ToString
public enum ExceptionCode {
    // common
    ARGUMENTS_NOT_VALID(101, "请求参数验证失败:%s"),
    DATA_NOT_EXISTS(404, "请求的数据不存在"),
    INTERNAL_SERVER_ERROR(500, "系统内部错误"),

    // Project 1000
    PROJECT_NOT_EXISTS(1000, "项目不存在"),
    // Group 1500
    PROJECT_PARENT_GROUP_NOT_EXISTS(1100, "父项目组不存在"),
    PROJECT_PARENT_GROUP_CIRCULAR_REFERENCE_ERROR(1101, "项目组不能循环引用"),

    // Demo 2000
    DEMO_REF_UNDONE(2000, "由于依赖关系的限制，无法完成任务");

    private int code;
    private String msg;
    ExceptionCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
    public ExceptionResult<String> result() {
        return new ExceptionResult<>(getCode(), getMsg());
    }
}
