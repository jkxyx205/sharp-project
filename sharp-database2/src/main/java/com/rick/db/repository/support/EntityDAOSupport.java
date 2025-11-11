package com.rick.db.repository.support;

import com.rick.common.util.StringUtils;
import com.rick.db.config.SharpDatabaseProperties;
import com.rick.db.repository.*;
import com.rick.db.repository.model.EntityIdCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Set;

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
    private SharpDatabaseProperties sharpDatabaseProperties;

    @Resource
    private TableDAO tableDAO;

    public <T, ID> EntityDAO<T, ID> getEntityDAO(@NonNull Class<?> entityClass) {
        EntityDAO entityDAO = EntityDAOManager.getDAO(entityClass);
        boolean isNotInstance = (entityDAO == null);

        if (isNotInstance) {
            entityDAO =  EntityIdCode.class.isAssignableFrom(entityClass) ? new EntityCodeDAOImpl(tableDAO, entityClass) :
                    new EntityDAOImpl(tableDAO, entityClass);

            EntityDAOManager.register(entityClass, entityDAO);

            ConfigurableListableBeanFactory beanFactory = ((ConfigurableApplicationContext) context).getBeanFactory();
//        String beanName = StringUtils.stringToCamel(entityClass.getSimpleName()) + "DAO";
            String beanName = StringUtils.stringToCamel(entityClass.getName().replaceAll(entityClass.getPackage().getName() + ".", "").replace("$", "")) + "DAO";
            beanFactory.registerSingleton(beanName, entityDAO);
        }

        return entityDAO;
    }

    @PostConstruct
    public void init() throws ClassNotFoundException {
        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(
                false);
        // 只扫描注解是 @Table 的类
        provider.addIncludeFilter(new AnnotationTypeFilter(Table.class));

        registerDAO(provider, sharpDatabaseProperties.getEntityBasePackage());
    }

    private void registerDAO(ClassPathScanningCandidateComponentProvider provider, @NonNull String packagePath) throws ClassNotFoundException {
        Set<BeanDefinition> scanList = provider.findCandidateComponents(packagePath);

        for (BeanDefinition beanDefinition : scanList) {
            getEntityDAO(Class.forName(beanDefinition.getBeanClassName()));
        }
    }

}
