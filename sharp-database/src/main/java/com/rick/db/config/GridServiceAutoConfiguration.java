package com.rick.db.config;

import com.rick.common.validate.ValidatorHelper;
import com.rick.db.formatter.AbstractSqlFormatter;
import com.rick.db.formatter.MysqlSqlFormatter;
import com.rick.db.formatter.OracleSqlFormatter;
import com.rick.db.middleware.mybatis.MappedSharpService;
import com.rick.db.plugin.GridUtils;
import com.rick.db.plugin.SQLUtils;
import com.rick.db.plugin.dao.core.BaseDAO;
import com.rick.db.plugin.dao.core.BaseDAOManager;
import com.rick.db.plugin.dao.core.TableGenerator;
import com.rick.db.plugin.dao.support.ColumnAutoFill;
import com.rick.db.plugin.dao.support.DefaultColumnAutoFill;
import com.rick.db.plugin.dao.support.DefaultConditionAdvice;
import com.rick.db.service.GridService;
import com.rick.db.service.support.SharpServiceQueryInterceptor;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
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

    }

    @ConditionalOnProperty(prefix = "sharp.database", name = "select-cache")
    @Import({SharpServiceQueryInterceptor.class})
    static class GridServiceCacheConfiguration {}

    @Configuration
    static class BaseDAOConfiguration {

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
        public BaseDAOManager baseDAOManager(@Autowired(required = false) List<BaseDAO> baseDAOList) {
            BaseDAOManager baseDAOManager = new BaseDAOManager();
            BaseDAOManager.setBaseDAOList(baseDAOList);
            return baseDAOManager;
        }

        @Bean
        @ConditionalOnMissingBean
        @ConditionalOnBean(Validator.class)
        public ValidatorHelper validatorHelper(Validator validator) {
            return new ValidatorHelper(validator);
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
    @ConditionalOnClass(org.apache.ibatis.session.SqlSessionFactory.class)
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
