package com.rick.db.config;

import com.rick.common.http.convert.*;
import com.rick.common.validate.ValidatorHelper;
import com.rick.db.plugin.generator.*;
import com.rick.db.plugin.page.GridService;
import com.rick.db.plugin.page.GridUtils;
import com.rick.db.repository.EntityDAO;
import com.rick.db.repository.TableDAO;
import com.rick.db.repository.TableDAOImpl;
import com.rick.db.repository.model.DatabaseType;
import com.rick.db.repository.support.*;
import com.rick.db.repository.support.dialect.*;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

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
public class SharpDatabaseAutoConfiguration {

    public SharpDatabaseAutoConfiguration() {}

    @Configuration
    static class GridServiceConfiguration {

        @Bean
        @ConditionalOnMissingBean({TableDAO.class})
        public TableDAO tableDAO(NamedParameterJdbcTemplate namedParameterJdbcTemplate, SharpDatabaseProperties sharpDatabaseProperties) {
            if (sharpDatabaseProperties.isInitDatabaseMetaData()) {
                DatabaseMetaData.initTableMapping(namedParameterJdbcTemplate.getJdbcTemplate());
            }

            try {
                java.sql.DatabaseMetaData metaData = namedParameterJdbcTemplate.getJdbcTemplate().getDataSource().getConnection().getMetaData();
                sharpDatabaseProperties.setDatabaseProductVersion(metaData.getDatabaseProductVersion());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return new TableDAOImpl(namedParameterJdbcTemplate);
        }

        @Bean
        public AbstractDialect getDialect(SharpDatabaseProperties properties) {
            if (properties.getType() == DatabaseType.Oracle10g) {
                return new Oracle10gDialect();
            } else if (properties.getType() == DatabaseType.Oracle11c) {
                return new Oracle11cDialect();
            } else if (properties.getType() == DatabaseType.SQLServer2012) {
                return new SQLServer2012Dialect();
            } else if (properties.getType() == DatabaseType.PostgreSQL) {
                return new PostgresDialect();
            } else if (properties.getType() == DatabaseType.MySQL5) {
                return new MySQL5Dialect();
            } else if (properties.getType() == DatabaseType.MySQL8) {
                return new MySQL8Dialect();
            } else

                if (properties.getType() == DatabaseType.SQLite) {
                return new SQLiteDialect();
            }

            return null;
        }

        @Bean
        public GridService gridService(TableDAO tableDAO, AbstractDialect dialect) {
            SQLParamCleaner.setDialect(dialect);
            return new GridService(tableDAO, dialect);
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
        public EntityDAOSupport entityDAOSupport(@Autowired(required = false) List<EntityDAO> entityDAOList) {
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
        public void setGridService(GridService gridService) {
            GridUtils gridUtils = new GridUtils();
            gridUtils.setGridService(gridService);
        }
    }

    @Configuration
    static class TableGeneratorConfiguration {

        @Bean
        public TableGenerator tableGenerator(JdbcTemplate jdbcTemplate, AbstractDialect dialect) {
            if (dialect.getType() == DatabaseType.Oracle10g) {
                // TODO
                //return new Oracle10gTableGenerator();
            } else if (dialect.getType() == DatabaseType.Oracle11c) {
                // TODO
                //return new Oracle11cTableGenerator();
            } else if (dialect.getType() == DatabaseType.SQLServer2012) {
                // TODO
                //return new SQLServer2012TableGenerator();
            } else if (dialect.getType() == DatabaseType.PostgreSQL) {
                return new PostgresSQLTableGenerator(jdbcTemplate);
            } else if (dialect.getType() == DatabaseType.MySQL5) {
                return new MySQL5TableGenerator(jdbcTemplate);
            } else if (dialect.getType() == DatabaseType.MySQL8) {
                return new MySQL8TableGenerator(jdbcTemplate);
            } else if (dialect.getType() == DatabaseType.SQLite) {
                return new SQLiteTableGenerator(jdbcTemplate);
            }

            return new MySQL5TableGenerator(jdbcTemplate);
        }
    }

}
