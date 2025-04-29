package com.rick.admin.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Rick.Xu
 * @date 2025/4/29 11:16
 */
@Component
public class AppConfigHolder {

    private static AppConfigProperties properties;

    @Autowired
    public AppConfigHolder(AppConfigProperties properties) {
        AppConfigHolder.properties = properties;
    }

    public static String getVersion() {
        return properties.getVersion();
    }
}
