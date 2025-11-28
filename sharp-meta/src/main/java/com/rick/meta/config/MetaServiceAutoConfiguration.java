package com.rick.meta.config;

import com.rick.db.config.SharpDatabaseAutoConfiguration;
import com.rick.db.repository.TableDAO;
import com.rick.meta.dict.convert.*;
import com.rick.meta.dict.dao.DictDAO;
import com.rick.meta.dict.model.DictProperties;
import com.rick.meta.dict.service.DictDOSupplier;
import com.rick.meta.dict.service.DictService;
import com.rick.meta.dict.service.DictServiceImpl;
import com.rick.meta.props.model.KeyValueProperties;
import com.rick.meta.props.service.PropertyService;
import com.rick.meta.props.service.PropertyServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
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
@ConditionalOnSingleCandidate(TableDAO.class)
@AutoConfigureAfter({SharpDatabaseAutoConfiguration.class})
public class MetaServiceAutoConfiguration {

    public MetaServiceAutoConfiguration() {
    }

    @Configuration
    @EnableConfigurationProperties({DictProperties.class, KeyValueProperties.class})
    static class MetaGridServiceConfiguration {

        @Bean
        public DictDAO dictDAO() {
            return new DictDAO();
        }

        @Bean
        public DictService getDictService(TableDAO tableDAO, DictProperties dictProperties,
                                          @Autowired(required = false) DictDOSupplier dictDOSupplier) {
            return new DictServiceImpl(tableDAO, dictProperties, dictDOSupplier);
        }

        @Bean
        public PropertyService getPropertyService(TableDAO tableDAO, KeyValueProperties keyValueProperties) {
            return new PropertyServiceImpl(tableDAO, keyValueProperties);
        }

        @Bean
        public DictConverter dictConverter(DictService dictService, ArrayDictConverter arrayDictConverter) {
            return new DictConverter(dictService, arrayDictConverter);
        }

        @Bean
        public SqlDateConverter sqlDateConverter() {
            return new SqlDateConverter();
        }

        @Bean
        public SqlTimestampConverter sqlTimestampConverter() {
            return new SqlTimestampConverter();
        }

        @Bean
        public LocalDateTimeConverter localDateTimeConverter() {
            return new LocalDateTimeConverter();
        }

        @Bean
        public BoolConverter boolConverter() {
            return new BoolConverter();
        }

        @Bean
        public ArrayDictConverter arrayDictConverter(DictService dictService) {
            return new ArrayDictConverter(dictService);
        }

    }
}
