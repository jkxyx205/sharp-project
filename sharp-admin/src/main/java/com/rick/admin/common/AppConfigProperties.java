package com.rick.admin.common;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author Rick.Xu
 * @date 2025/4/29 11:16
 */
@Component
@ConfigurationProperties(prefix = "app")
@Data
public class AppConfigProperties {
    private String version;
}
