package com.rick.notification.wechat;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author Rick
 * @createdAt 2025-04-27 15:30:00
 */
@Configuration
@ConfigurationProperties(prefix = "wechat")
@Data
public class WechatKey {

    private String appid;

    private String secret;
}