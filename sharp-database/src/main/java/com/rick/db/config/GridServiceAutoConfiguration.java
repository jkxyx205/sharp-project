package com.rick.db.config;

import com.rick.db.formatter.AbstractSqlFormatter;
import com.rick.db.formatter.MysqlSqlFormatter;
import com.rick.db.formatter.OracleSqlFormatter;
import com.rick.db.plugin.GridUtils;
import com.rick.db.plugin.SQLUtils;
import com.rick.db.plugin.mybatis.MappedSharpService;
import com.rick.db.service.GridService;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;

import javax.sql.DataSource;

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
        @Primary
        public GridService gridService() {
            return new GridService();
        }

    }

    @Configuration
    static class UtilGridServiceConfiguration {

        @Autowired
        public void setGridService(GridService gridService) {
            GridUtils gridUtils = new GridUtils();
            gridUtils.setGridService(gridService);

            SQLUtils sqlUtils = new SQLUtils();
            sqlUtils.setJdbcTemplate(gridService.getNamedJdbcTemplate().getJdbcTemplate());
        }
    }

    @Configuration
    @ConditionalOnClass(org.apache.ibatis.session.SqlSessionFactory.class)
    static class MappedSharpServiceConfiguration {

        @Bean
        @Order
        public MappedSharpService getMappedSharpService(SqlSessionFactory sqlSessionFactory, GridService gridService) {
            MappedSharpService mappedSharpService = new MappedSharpService(sqlSessionFactory, gridService);
            return mappedSharpService;
        }
    }

}
