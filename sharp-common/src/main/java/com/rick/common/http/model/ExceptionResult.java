package com.rick.common.http.model;

/**
 * All rights Reserved, Designed By www.xhope.top
 *
 * @version V1.0
 * @Description: (用一句话描述该文件做什么)
 * @author: Rick.Xu
 * @date: 9/10/19 1:55 PM
 * @Copyright: 2019 www.yodean.com. All rights reserved.
 */
public class ExceptionResult<T> extends Result<T> {

    public ExceptionResult(int code, String msg) {
        super(false, code, msg, null);
    }
}
