package com.rick.db.plugin.dao.core;

import com.rick.common.util.StringUtils;
import com.rick.common.validate.ValidatorHelper;
import com.rick.db.config.SharpDatabaseProperties;
import com.rick.db.dto.BaseCodeEntity;
import com.rick.db.plugin.dao.annotation.Table;
import com.rick.db.plugin.dao.support.ColumnAutoFill;
import com.rick.db.plugin.dao.support.ConditionAdvice;
import com.rick.db.service.SharpService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Set;

/**
 * @author Rick.Xu
 * @date 2023/6/30 11:12
 */
@RequiredArgsConstructor
public class EntityDAOSupport {

    @Resource
    private ApplicationContext context;

    @Autowired
    @Qualifier("dbConversionService")
    private ConversionService conversionService;

    @Autowired
    private SharpService sharpService;

    @Autowired(required = false)
    private ConditionAdvice conditionAdvice;

    @Autowired(required = false)
    private ColumnAutoFill columnAutoFill;

    @Autowired(required = false)
    private ValidatorHelper validatorHelper;

    @Autowired
    private SharpDatabaseProperties sharpDatabaseProperties;

    public <T, ID> EntityDAO<T, ID> getEntityDAO(@NonNull Class<?> entityClass) {
        EntityDAO entityDAO = EntityDAOManager.getEntityDAO(entityClass);
        boolean isNotInstance = (entityDAO == null);

        if (isNotInstance) {
            Class<?> idClass = TableMetaResolver.resolve(entityClass).getIdField().getType();
            entityDAO =  BaseCodeEntity.class.isAssignableFrom(entityClass) ? new EntityCodeDAOImpl(entityClass, idClass,
                    context,
                    conversionService,
                    sharpService,
                    conditionAdvice,
                    columnAutoFill,
                    validatorHelper,
                    sharpDatabaseProperties)
                    :
                    new EntityDAOImpl(entityClass, idClass,
                            context,
                            conversionService,
                            sharpService,
                            conditionAdvice,
                            columnAutoFill,
                            validatorHelper,
                            sharpDatabaseProperties);

            EntityDAOManager.register(entityDAO);

            ConfigurableListableBeanFactory beanFactory = ((ConfigurableApplicationContext) context).getBeanFactory();
//        String beanName = StringUtils.stringToCamel(entityClass.getSimpleName()) + "DAO";
            String beanName = StringUtils.stringToCamel(entityClass.getName().replaceAll(entityClass.getPackage().getName() + ".", "").replace("$", "")) + "DAO";
            if (!beanFactory.containsBean(beanName)) {
                beanFactory.registerSingleton(beanName, entityDAO);
            }

            for (TableMeta.OneToManyProperty oneToManyProperty : entityDAO.getTableMeta().getOneToManyAnnotationList()) {
                getEntityDAO(oneToManyProperty.getSubEntityClass());
            }
        }

        return entityDAO;
    }

    @PostConstruct
    public void init() throws ClassNotFoundException {
        if (org.apache.commons.lang3.StringUtils.isBlank(sharpDatabaseProperties.getEntityBasePackage())) {
            return;
        }

        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(
                false);
        // 只扫描注解是 @Table 的类
        provider.addIncludeFilter(new AnnotationTypeFilter(Table.class));
        Set<BeanDefinition> scanList = provider.findCandidateComponents(sharpDatabaseProperties.getEntityBasePackage());

        for (BeanDefinition beanDefinition : scanList) {
            getEntityDAO(Class.forName(beanDefinition.getBeanClassName()));
        }
    }
}
