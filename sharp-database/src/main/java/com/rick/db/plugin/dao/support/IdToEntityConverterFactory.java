package com.rick.db.plugin.dao.support;

import com.rick.db.dto.BaseEntity;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;

/**
 * @author Rick
 * @createdAt 2021-10-11 20:28:00
 */
final public class IdToEntityConverterFactory implements ConverterFactory<Object, BaseEntity> {

    @Override
    public <T extends BaseEntity> Converter<Object, T> getConverter(Class<T> targetType) {
        return new IdToEntityConverterFactory.IdToEntity(targetType);
    }

    private static class IdToEntity<T extends BaseEntity> implements Converter<Object, T> {

        private Class<?> targetType;

        public <T extends BaseEntity> IdToEntity(Class<T> targetType) {
            this.targetType = targetType;
        }

        @Override
        public T convert(Object id) {
            if (id == null) {
                return (T) id;
            }

            try {
                T t = (T) targetType.newInstance();
                if (id.getClass() == Long.class) {
                    t.setId((Long) id);
                } else if (id.getClass() == String.class) {
                    t.setId(Long.parseLong((String) id));
                }
                return t;
            } catch (Exception e) {
                return null;
            }
        }
    }
}
