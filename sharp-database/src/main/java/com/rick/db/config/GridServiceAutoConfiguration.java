package com.rick.db.config;

import com.rick.common.http.convert.*;
import com.rick.common.validate.ValidatorHelper;
import com.rick.db.formatter.AbstractSqlFormatter;
import com.rick.db.formatter.MysqlSqlFormatter;
import com.rick.db.formatter.OracleSqlFormatter;
import com.rick.db.middleware.mybatis.MappedSharpService;
import com.rick.db.plugin.GridUtils;
import com.rick.db.plugin.SQLUtils;
import com.rick.db.plugin.dao.core.EntityDAO;
import com.rick.db.plugin.dao.core.EntityDAOManager;
import com.rick.db.plugin.dao.core.TableGenerator;
import com.rick.db.plugin.dao.support.*;
import com.rick.db.service.GridService;
import com.rick.db.service.support.SharpServiceQueryInterceptor;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.Order;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import javax.validation.Validator;
import java.util.List;

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
@ConditionalOnSingleCandidate(DataSource.class)
@AutoConfigureAfter({DataSourceAutoConfiguration.class})
@EnableConfigurationProperties({SharpDatabaseProperties.class})
public class GridServiceAutoConfiguration {

    public GridServiceAutoConfiguration() {
    }

    @Configuration
    static class GridServiceConfiguration {

        @Bean
        public AbstractSqlFormatter sqlFormatter(SharpDatabaseProperties sharpDatabaseProperties) {
            if (Constants.DB_ORACLE.equals(sharpDatabaseProperties.getType())) {
                return new OracleSqlFormatter();
            }

            return new MysqlSqlFormatter();
        }

        @Bean
        public GridService gridService() {
            return new GridService();
        }

        @Bean
        public BaseCodeEntityIdFillService getBaseCodeEntityIdFillService() {
            return new BaseCodeEntityIdFillService();
        }

    }

    @ConditionalOnProperty(prefix = "sharp.database", name = "select-cache")
    @Import({SharpServiceQueryInterceptor.class})
    static class GridServiceCacheConfiguration {}

    @Configuration
    static class BaseDAOConfiguration implements ApplicationListener<ContextRefreshedEvent> {

        @Autowired(required = false)
        private List<ConverterFactory> converterFactories;

        @Bean
        @ConditionalOnMissingBean
        public ColumnAutoFill columnAutoFill() {
            return new DefaultColumnAutoFill();
        }

        @Bean
        @ConditionalOnMissingBean
        public DefaultConditionAdvice defaultConditionAdvice() {
            return new DefaultConditionAdvice();
        }

        @Bean
        public EntityDAOManager baseDAOManager(@Autowired(required = false) List<EntityDAO> entityDAOList) {
            EntityDAOManager entityDAOManager = new EntityDAOManager();
            EntityDAOManager.setBaseDAOList(entityDAOList);
            return entityDAOManager;
        }

        @Bean
        @ConditionalOnMissingBean
        @ConditionalOnBean(Validator.class)
        public ValidatorHelper validatorHelper(Validator validator) {
            return new ValidatorHelper(validator);
        }

        @Override
        public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
            FormattingConversionService conversionService = contextRefreshedEvent.getApplicationContext().getBean(FormattingConversionService.class);

            if (CollectionUtils.isNotEmpty(converterFactories)) {
                for (ConverterFactory converterFactory : converterFactories) {
                    conversionService.addConverterFactory(converterFactory);
                }
            }

            conversionService.addConverterFactory(new StringToLocalDateConverterFactory());

            conversionService.addConverterFactory(new CodeToEnumConverterFactory());
            conversionService.addConverter(new JsonStringToListMapConverter());
            conversionService.addConverterFactory(new JsonStringToObjectConverterFactory());
            conversionService.addConverterFactory(new JsonStringToMapConverterFactory());
            conversionService.addConverter(new JsonStringToCollectionConverter());
            conversionService.addConverter(new JsonStringToSetMapConverter());
            conversionService.addConverterFactory(new IdToEntityConverterFactory());
        }
    }

    @Configuration
    static class UtilGridServiceConfiguration {

        @Autowired
        public void setGridService(GridService gridService, SharpDatabaseProperties sharpDatabaseProperties) {
            GridUtils gridUtils = new GridUtils();
            gridUtils.setGridService(gridService);

            SQLUtils sqlUtils = new SQLUtils();
            sqlUtils.setNamedJdbcTemplate(gridService.getNamedJdbcTemplate());
            sqlUtils.setSharpDatabaseProperties(sharpDatabaseProperties);
        }
    }

    @Configuration
    @ConditionalOnClass(SqlSessionFactory.class)
    @RequiredArgsConstructor
    static class MappedSharpServiceConfiguration {

        private final SqlSessionFactory sqlSessionFactory;

        @Bean
        @Order
        public MappedSharpService getMappedSharpService(GridService gridService) {
            MappedSharpService mappedSharpService = new MappedSharpService(sqlSessionFactory, gridService);
            return mappedSharpService;
        }
    }

    @Configuration
    static class TableGeneratorConfiguration {

        @Bean
        public TableGenerator initTableGenerator(JdbcTemplate jdbcTemplate) {
            return new TableGenerator(jdbcTemplate);
        }
    }

}
