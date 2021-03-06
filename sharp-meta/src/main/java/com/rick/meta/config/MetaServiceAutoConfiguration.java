package com.rick.meta.config;

import com.rick.db.config.GridServiceAutoConfiguration;
import com.rick.db.service.GridService;
import com.rick.meta.dict.model.DictProperties;
import com.rick.meta.dict.service.DictService;
import com.rick.meta.dict.service.DictServiceImpl;
import com.rick.meta.props.service.PropertyService;
import com.rick.meta.props.service.PropertyServiceImpl;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
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
@ConditionalOnSingleCandidate(GridService.class)
@AutoConfigureAfter({GridServiceAutoConfiguration.class})
public class MetaServiceAutoConfiguration {

    public MetaServiceAutoConfiguration() {
    }

    @Configuration
    @EnableConfigurationProperties(DictProperties.class)
    static class MetaGridServiceConfiguration {

        @Bean
        public DictService getDictService(GridService gridService, DictProperties dictProperties) {
            return new DictServiceImpl(gridService, dictProperties);
        }

        @Bean
        public PropertyService getPropertyService(GridService gridService) {
            return new PropertyServiceImpl(gridService);
        }
    }
}
