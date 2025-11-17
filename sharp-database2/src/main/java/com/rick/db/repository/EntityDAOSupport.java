package com.rick.db.repository;

import com.rick.common.util.StringUtils;
import com.rick.db.config.SharpDatabaseProperties;
import com.rick.db.repository.model.EntityIdCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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

    @Resource
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

        String[] packages = sharpDatabaseProperties.getEntityBasePackage().split(",\\s+");
        for (String packagePath : packages) {
            registerDAO(provider, packagePath);
        }
    }

    public <T> List<T> select(Class<T> clazz, String sql, Map<String, Object> params) {
        List<T> list = tableDAO.select(clazz, sql, params);
        select(list);
        return list;
    }

    public <T> List<T> select(Class<T> clazz, String sql, Object args) {
        List<T> list = tableDAO.select(clazz, sql, args);
        select(list);
        return list;
    }

    public <T> T select(T t) {
        if (t == null) {
            return null;
        }

        select(Arrays.asList(t));
        return t;
    }

    public <T> List<T> select(List<T> list) {
        if (CollectionUtils.isEmpty(list)) {
            return list;
        }

        EntityDAO<T, ?> entityDAO = getEntityDAO(list.get(0).getClass());
        entityDAO.cascadeSelect(list);
        return list;
    }

    private void registerDAO(ClassPathScanningCandidateComponentProvider provider, @NonNull String packagePath) throws ClassNotFoundException {
        Set<BeanDefinition> scanList = provider.findCandidateComponents(packagePath);

        for (BeanDefinition beanDefinition : scanList) {
            getEntityDAO(Class.forName(beanDefinition.getBeanClassName()));
        }
    }

}
