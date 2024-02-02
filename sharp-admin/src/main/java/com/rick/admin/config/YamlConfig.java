package com.rick.admin.config;

import org.springframework.beans.factory.config.YamlMapFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Rick
 * @date 2021-04-08 14:47:00
 */
@Configuration
public class YamlConfig {

    @Bean
    public YamlMapFactoryBean yamlMapFactoryBean() {
        YamlMapFactoryBean yamlMapFactoryBean = new YamlMapFactoryBean();
        yamlMapFactoryBean.setSingleton(false);
        return yamlMapFactoryBean;
    }
}
