package com.rick.demo.module.project.domain.entity;

import com.rick.common.validate.annotation.EnumValid;
import com.rick.common.validate.annotation.PhoneValid;
import com.rick.demo.module.project.domain.enums.SexEnum;
import com.rick.demo.module.project.domain.enums.UserStatusEnum;
import lombok.Builder;
import lombok.Value;

import javax.validation.constraints.NotBlank;

/**
 * @author Rick
 * @createdAt 2021-10-08 18:15:00
 */
@Value
@Builder
public class User {

    @PhoneValid
    private String phone;

    @NotBlank
    private String name;

//    @EnumValid(target = SexEnum.class, message = "性别有误，不存在值${validatedValue}")
    @EnumValid(target = SexEnum.class)
    private Integer sex;

    @EnumValid(target = UserStatusEnum.class, message = "用户状态有误，不存在值${validatedValue}")
    private String userStatus;
}
