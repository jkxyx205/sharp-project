package com.rick.common.http.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * All rights Reserved, Designed By www.xhope.top
 *
 * @version V1.0
 * @Description: (用一句话描述该文件做什么)
 * @author: Rick.Xu
 * @date: 9/10/19 1:55 PM
 * @Copyright: 2019 www.yodean.com. All rights reserved.
 */
@Data
@AllArgsConstructor
public class Result<T> implements Serializable {

    private boolean success;

    private int code;

    private String msg;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T data;

}
