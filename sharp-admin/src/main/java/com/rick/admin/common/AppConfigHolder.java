package com.rick.admin.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.ServletContext;

/**
 * @author Rick.Xu
 * @date 2025/4/29 11:16
 */
@Component
public class AppConfigHolder {

    private static AppConfigProperties properties;

    @Resource
    private ServletContext servletContext;

    @Autowired
    public AppConfigHolder(AppConfigProperties properties) {
        AppConfigHolder.properties = properties;
    }

    @PostConstruct
    public void init() {
        servletContext.setAttribute("VERSION", properties.getVersion());
        servletContext.setAttribute("DOMAIN", properties.getDomain());
    }

    public static String getVersion() {
        return properties.getVersion();
    }

    public static String getDomain() {
        return properties.getDomain();
    }

}
