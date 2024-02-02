package com.rick.admin.common.exception;

import com.rick.common.http.exception.ExceptionCode;
import lombok.Getter;
import lombok.ToString;

/**
 * @author Rick.Xu
 * @date 2023/5/29 13:46
 */
@Getter
@ToString
public enum ExceptionCodeEnum implements ExceptionCode {
    CODE_NOT_EXISTS_ERROR(400, "%s code「%s」不存在"),
    RESOURCE_NOT_EXISTS_ERROR(404, "「%s」不存在"),

    USER_NOT_FOUND(400019, "用户不存在！"),
    USER_NOT_AVAILABLE(400010, "用户状态不可用，请联系管理员！"),
    INVALID_SESSION(400011, "访问超时，请退出后重新登录！"),
    VALIDATE_CODE_ERROR(40003, "验证码输入不正确！"),
    ROLE_NULL_ERROR(404009, "角色名不能为空！"),
    LOW_STOCKS_ERROR(504001, "「%s」库存不足！"),
    MATERIAL_DOCUMENT_NOT_FOUND_ERROR(504002, "物料凭证「%s」不存在！"),
    MATERIAL_DOCUMENT_IS_CANCELED_ERROR(504003, "物料凭证「%s」已经被取消了，不能重复取消！"),
    MATERIAL_OVER_MAX_MOVEMENT_ERROR(504004, "物料「%s」不能超过最大的库存移动数量！"),
    COUNT_MATERIAL_DOCUMENT_REF_ERROR(504005, "盘点凭证不能被参考！"),
    PO_DOCUMENT_NOT_FOUND_ERROR(505001, "采购订单「%s」不存在！"),
    PP_DOCUMENT_NOT_FOUND_ERROR(505002, "生产订单「%s」不存在！"),
    SO_DOCUMENT_NOT_FOUND_ERROR(505003, "销售订单「%s」不存在！");

    private int code;
    private String message;
    ExceptionCodeEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

}
