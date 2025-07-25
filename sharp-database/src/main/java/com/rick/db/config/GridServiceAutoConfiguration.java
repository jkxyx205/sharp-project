package com.rick.db.config;

import com.rick.common.http.convert.*;
import com.rick.common.validate.ValidatorHelper;
import com.rick.db.formatter.AbstractSqlFormatter;
import com.rick.db.formatter.MysqlSqlFormatter;
import com.rick.db.formatter.OracleSqlFormatter;
import com.rick.db.formatter.PostgresSqlFormatter;
import com.rick.db.middleware.mybatis.MappedSharpService;
import com.rick.db.plugin.*;
import com.rick.db.plugin.dao.core.*;
import com.rick.db.plugin.dao.support.*;
import com.rick.db.service.GridService;
import com.rick.db.service.SharpService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import javax.validation.Validator;
import java.sql.SQLException;
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

    public GridServiceAutoConfiguration() {}

    @Configuration
    static class GridServiceConfiguration {

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
        public AbstractSqlFormatter sqlFormatter(SharpDatabaseProperties sharpDatabaseProperties) {
            if (Constants.DB_ORACLE.equals(sharpDatabaseProperties.getType())) {
                return new OracleSqlFormatter();
            } else if (Constants.DB_POSTGRESQL.equals(sharpDatabaseProperties.getType())) {
                return new PostgresSqlFormatter();
            }

            return new MysqlSqlFormatter();
        }

        @Bean
        public SharpService sharpService(JdbcTemplate jdbcTemplate, SharpDatabaseProperties sharpDatabaseProperties) {
            if (sharpDatabaseProperties.isInitDatabaseMetaData()) {
                DatabaseMetaData.initTableMapping(jdbcTemplate);
            }

            try {
                java.sql.DatabaseMetaData metaData = jdbcTemplate.getDataSource().getConnection().getMetaData();
                sharpDatabaseProperties.setType(metaData.getDatabaseProductName());
                sharpDatabaseProperties.setDatabaseProductVersion(metaData.getDatabaseProductVersion());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            // 加载
            return new SharpService();
        }

        @Bean
        public GridService gridService(SharpService sharpService) {
            return new GridService(sharpService);
        }

        @Bean
        public EntityCodeIdFillService getEntityCodeIdFillService() {
            return new EntityCodeIdFillService();
        }
    }

//    @ConditionalOnProperty(prefix = "sharp.database", name = "select-cache")
//    @Import({SharpServiceQueryInterceptor.class})
//    static class GridServiceCacheConfiguration {}

    @Configuration
    static class EntityDAOConfiguration {

        @Autowired(required = false)
        private List<ConverterFactory> converterFactories;

        @Bean
        public EntityDAOManager baseDAOManager(@Autowired(required = false) List<EntityDAO> entityDAOList) {
            EntityDAOManager entityDAOManager = new EntityDAOManager();
            EntityDAOManager.setBaseDAOList(entityDAOList);
            return entityDAOManager;
        }

        @Bean
        @DependsOn("baseDAOManager")
        public EntityDAOSupport entityDAOSupport() {
            EntityDAOSupport entityDAOSupport = new EntityDAOSupport();
            return entityDAOSupport;
        }

        @Bean
        @ConditionalOnMissingBean
        @ConditionalOnBean(Validator.class)
        public ValidatorHelper validatorHelper(Validator validator) {
            return new ValidatorHelper(validator);
        }

        @Bean
        public ConversionService dbConversionService() {
            DefaultFormattingConversionService dbConversionService= new DefaultFormattingConversionService();

            if (CollectionUtils.isNotEmpty(converterFactories)) {
                for (ConverterFactory converterFactory : converterFactories) {
                    dbConversionService.addConverterFactory(converterFactory);
                }
            }

            dbConversionService.addConverterFactory(new StringToLocalDateConverterFactory());
            dbConversionService.addConverterFactory(new CodeToEnumConverterFactory());
            dbConversionService.addConverter(new JsonStringToListMapConverter());
            dbConversionService.addConverterFactory(new JsonStringToObjectConverterFactory());
            dbConversionService.addConverterFactory(new JsonStringToMapConverterFactory());
            dbConversionService.addConverter(new JsonStringToCollectionConverter());
            dbConversionService.addConverter(new JsonStringToSetMapConverter());
            dbConversionService.addConverterFactory(new IdToEntityConverterFactory());
            dbConversionService.addConverter(new LocalDateTimeToInstantConverter());

            return dbConversionService;
        }

    }

    @Configuration
    static class UtilGridServiceConfiguration {

        @Autowired
        public void setGridService(GridService gridService, SharpDatabaseProperties sharpDatabaseProperties) {
            GridUtils gridUtils = new GridUtils();
            gridUtils.setGridService(gridService);

            SQLUtils sqlUtils = new SQLUtils();
            sqlUtils.setNamedJdbcTemplate(gridService.getSharpService().getNamedJdbcTemplate());
            sqlUtils.setSharpDatabaseProperties(sharpDatabaseProperties);

            QueryUtils.setSharpService(gridService.getSharpService());

            IdDescriptionUtils.setSharpService(gridService.getSharpService());
        }
    }

    @Configuration
    @ConditionalOnClass(SqlSessionFactory.class)
    @RequiredArgsConstructor
    static class MappedSharpServiceConfiguration {

        private final SqlSessionFactory sqlSessionFactory;

        @Bean
        public MappedSharpService getMappedSharpService(SharpService sharpService) {
            MappedSharpService mappedSharpService = new MappedSharpService(sqlSessionFactory, sharpService);
            return mappedSharpService;
        }
    }

    @Configuration
    static class TableGeneratorConfiguration {

        @Bean
        public TableGenerator initTableGenerator(JdbcTemplate jdbcTemplate, SharpDatabaseProperties sharpDatabaseProperties) {
            if (Constants.DB_ORACLE.equals(sharpDatabaseProperties.getType())) {
                // TODO
                //return new OracleSqlFormatter();
            } else if (Constants.DB_POSTGRESQL.equals(sharpDatabaseProperties.getType())) {
                return new PostgresSQLTableGenerator(jdbcTemplate, sharpDatabaseProperties);
            }

            return new MySQLTableGenerator(jdbcTemplate, sharpDatabaseProperties);
        }
    }

}
