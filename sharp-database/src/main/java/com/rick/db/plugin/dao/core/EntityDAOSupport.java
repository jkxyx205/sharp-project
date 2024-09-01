package com.rick.db.plugin.dao.core;

import com.rick.common.util.StringUtils;
import com.rick.common.validate.ValidatorHelper;
import com.rick.db.config.SharpDatabaseProperties;
import com.rick.db.dto.BaseCodeEntity;
import com.rick.db.plugin.dao.annotation.Table;
import com.rick.db.plugin.dao.support.ColumnAutoFill;
import com.rick.db.plugin.dao.support.ConditionAdvice;
import com.rick.db.service.SharpService;
import com.rick.db.util.OptionalUtils;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
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
import java.util.*;

/**
 * @author Rick.Xu
 * @date 2023/6/30 11:12
 */
@NoArgsConstructor
@Slf4j
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

    private static final String FILE_UPLOAD_ENTITY_PACKAGE = "com.rick.fileupload.client.support";

    public <T, ID> EntityDAO<T, ID> getEntityDAO(@NonNull Class<?> entityClass) {
        EntityDAO entityDAO = EntityDAOManager.getEntityDAO(entityClass);
        boolean isNotInstance = (entityDAO == null);

        if (isNotInstance) {
            entityDAO =  BaseCodeEntity.class.isAssignableFrom(entityClass) ? new EntityCodeDAOImpl(entityClass, null,
                    context,
                    conversionService,
                    sharpService,
                    conditionAdvice,
                    columnAutoFill,
                    validatorHelper,
                    sharpDatabaseProperties)
                    :
                    new EntityDAOImpl(entityClass, null,
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
            beanFactory.registerSingleton(beanName, entityDAO);
            if (log.isDebugEnabled()) {
                log.debug("Auto generate DAO with {}", beanName);
            }
            for (TableMeta.OneToManyProperty oneToManyProperty : entityDAO.getTableMeta().getOneToManyAnnotationList()) {
                getEntityDAO(oneToManyProperty.getSubEntityClass());
            }
        }

        return entityDAO;
    }

    public <T> Optional<T> queryForObject(String sql, Map<String, Object> params, Class<T> tagetClass) {
        return OptionalUtils.expectedAsOptional(query(sql,params, tagetClass));
    }

    public <T> List<T> query(String sql, Map<String, Object> params, Class<T> tagetClass) {
        List<T> list = sharpService.query(sql, params, tagetClass);
        EntityDAO<T, ?> entityDAO = getEntityDAO(tagetClass);
        entityDAO.selectPropertyBySql(list);;
        return list;
    }

    public <T> T query(T t) {
       if (t == null) {
           return null;
       }

       query(Arrays.asList(t));
       return t;
    }

    public <T> List<T> query(List<T> list) {
        if (CollectionUtils.isEmpty(list)) {
            return list;
        }

        EntityDAO<T, ?> entityDAO = getEntityDAO(list.get(0).getClass());
        entityDAO.selectPropertyBySql(list);;
        return list;
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
        String[] packages = sharpDatabaseProperties.getEntityBasePackage().split(",\\s+");
        for (String packagePath : packages) {
            registerDAO(provider, packagePath);
        }

        registerDAO(provider, FILE_UPLOAD_ENTITY_PACKAGE);
    }

    private void registerDAO(ClassPathScanningCandidateComponentProvider provider, String packagePath) throws ClassNotFoundException {
        Set<BeanDefinition> scanList = provider.findCandidateComponents(packagePath);

        for (BeanDefinition beanDefinition : scanList) {
            getEntityDAO(Class.forName(beanDefinition.getBeanClassName()));
        }
    }

}
