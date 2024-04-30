package com.rick.demo.config;

import com.rick.db.plugin.DbUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Rick.Xu
 * @date 2024/4/30 13:23
 */
@Configuration
public class DbUtilConfig {

    @Bean
    public DbUtils mysqlDbUtil() {
        return new DbUtils("jdbc:mysql://localhost:3306/sharp-demo?useUnicode=true&characterEncoding=UTF-8&serverTimezone=GMT%2B8&useSSL=false&allowPublicKeyRetrieval=true",
                "root",
                "jkxyx205");
    }
}
