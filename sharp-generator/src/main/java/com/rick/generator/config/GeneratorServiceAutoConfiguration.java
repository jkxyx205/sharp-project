package com.rick.generator.config;

import com.rick.db.plugin.dao.core.TableGenerator;
import com.rick.generator.Generator;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * All rights Reserved, Designed By www.xhope.top
 *
 * @version V1.0
 * @Description: 自动配置类
 * @author: Rick.Xu
 * @date: 9/12/19 7:42 PM
 * @Copyright: 2019 www.yodean.com. All rights reserved.
 */
@Configuration
@ConditionalOnSingleCandidate(TableGenerator.class)
@AutoConfigureAfter({DataSourceAutoConfiguration.class})
public class GeneratorServiceAutoConfiguration {

    public GeneratorServiceAutoConfiguration() {}

    @Bean
    public Generator initGenerator(TableGenerator tableGenerator) {
        return new Generator(tableGenerator);
    }
}
