package com.rick.admin.auth.validate.image;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author rick
 */
@Data
public class CaptchaImageVO {
    /**
     * 验证码文字
     */
    private String code;

    /**
     * 验证码失效时间
     */
    private LocalDateTime expireTime;

    public CaptchaImageVO(String code, int expireAfterSeconds){
        this.code = code;
        this.expireTime = LocalDateTime.now().plusSeconds(expireAfterSeconds);
    }

    /**
     * 验证码是否失效
     * @return 是否失效
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expireTime);
    }
}
