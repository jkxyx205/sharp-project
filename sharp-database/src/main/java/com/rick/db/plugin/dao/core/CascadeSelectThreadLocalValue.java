package com.rick.db.plugin.dao.core;

import com.rick.db.dto.SimpleEntity;
import lombok.experimental.UtilityClass;

import java.util.Set;
import java.util.function.BiConsumer;

/**
 * @author Rick
 * @createdAt 2022-04-15 14:51:00
 */
@UtilityClass
public class CascadeSelectThreadLocalValue {

    private static ThreadLocal<EntityDAOImpl.TriConsumer<TableMeta.OneToManyProperty, EntityDAO<? extends SimpleEntity, ?>, Set<?>>> oneToManyConsumerThreadLocal = new ThreadLocal<>();

    private static ThreadLocal<BiConsumer<TableMeta.ManyToManyProperty, Set<?>>> manyToManyConsumerLocal = new ThreadLocal<>();

    public static void add(EntityDAOImpl.TriConsumer<TableMeta.OneToManyProperty, EntityDAO<? extends SimpleEntity, ?>, Set<?>> oneToManyConsumer,
                           BiConsumer<TableMeta.ManyToManyProperty, Set<?>> manyToManyConsumer) {
        oneToManyConsumerThreadLocal.set(oneToManyConsumer);
        manyToManyConsumerLocal.set(manyToManyConsumer);
    }

    public static EntityDAOImpl.TriConsumer<TableMeta.OneToManyProperty, EntityDAO<? extends SimpleEntity, ?>, Set<?>> getOneToManyConsumer() {
        return oneToManyConsumerThreadLocal.get();
    }

    public static BiConsumer<TableMeta.ManyToManyProperty, Set<?>> getManyToManyConsumer() {
        return manyToManyConsumerLocal.get();
    }

    public static void removeAll() {
        oneToManyConsumerThreadLocal.remove();
        manyToManyConsumerLocal.remove();
    }

}
