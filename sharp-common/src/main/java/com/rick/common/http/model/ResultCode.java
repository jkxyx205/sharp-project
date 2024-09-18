package com.rick.common.http.model;

import lombok.Getter;
import lombok.ToString;

/**
 * All rights Reserved, Designed By www.xhope.top
 *
 * @version V1.0
 * @Description: (用一句话描述该文件做什么)
 * @author: Rick.Xu
 * @date: 9/10/19 2:03 PM
 * @Copyright: 2019 www.yodean.com. All rights reserved.
 */
@Getter
@ToString
public enum ResultCode {
    ERROR(500, "服务器端异常"),
    OK(200, "OK"),
    ARGUMENT_NOT_VALID(400, "参数验证失败"),
    ACCESS_FORBIDDEN_ERROR(403, "访问未授权"),
    RESOURCE_NOT_EXISTS_ERROR(404, "资源不存在"),
    UNPROCESSABLE_ENTITY_ERROR(422, "请求出现错误");

    private int code;

    private String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
