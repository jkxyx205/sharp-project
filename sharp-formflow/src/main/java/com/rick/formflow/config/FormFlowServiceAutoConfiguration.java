package com.rick.formflow.config;


import com.rick.db.config.SharpDatabaseAutoConfiguration;
import com.rick.db.plugin.page.GridService;
import com.rick.formflow.form.service.FormAdvice;
import com.rick.formflow.form.service.bo.FormBO;
import com.rick.formflow.form.service.convert.DateTimeToStringConverter;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

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
@ConditionalOnSingleCandidate(GridService.class)
@AutoConfigureAfter({SharpDatabaseAutoConfiguration.class})
public class FormFlowServiceAutoConfiguration {

    public FormFlowServiceAutoConfiguration() {
    }

    @Configuration
    @ComponentScan(value = "com.rick.formflow.form")
    static class FormServiceConfiguration {

        @Bean
        public DateTimeToStringConverter dateTimeToStringConverter() {
            return new DateTimeToStringConverter();
        }

        @Bean
        @ConditionalOnMissingBean
        public FormAdvice formAdvice() {
            return new FormAdvice() {
                @Override
                public void beforeInstanceHandle(FormBO form, Long instanceId, Map<String, Object> values) {

                }

                @Override
                public void afterInstanceHandle(FormBO form, Long instanceId, Map<String, Object> values) {

                }

//                @Override
//                public Map<String, Object> getValue(Long formId, Long instanceId) {
//                    return Collections.emptyMap();
//                }
            };
        }
    }
}
