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
    FAIL(-1, "服务器异常"),
    OK(0, "OK"),
    ACCESS_FORBIDDEN_ERROR(1, "访问未授权"),;

    private int code;

    private String msg;

    ResultCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
