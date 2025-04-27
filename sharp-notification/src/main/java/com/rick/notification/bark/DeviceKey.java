package com.rick.notification.bark;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author Rick
 * @createdAt 2025-04-27 15:30:00
 */
@Configuration
@ConfigurationProperties(prefix = "bark")
@Data
public class DeviceKey {

    private String url;

    private String[] deviceKeys;

    private String deviceKey;
}