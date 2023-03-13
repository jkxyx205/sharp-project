package com.rick.db.plugin.dao.support;

import com.rick.db.dto.SimpleEntity;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;

/**
 * @author Rick
 * @createdAt 2021-10-11 20:28:00
 */
final public class IdToEntityConverterFactory implements ConverterFactory<Object, SimpleEntity> {

    @Override
    public <T extends SimpleEntity> Converter<Object, T> getConverter(Class<T> targetType) {
        return new IdToEntityConverterFactory.IdToEntity(targetType);
    }

    private static class IdToEntity<T extends SimpleEntity> implements Converter<Object, T> {

        private Class<?> targetType;

        public <T extends SimpleEntity> IdToEntity(Class<T> targetType) {
            this.targetType = targetType;
        }

        @Override
        public T convert(Object obj) {
            if (obj == null) {
                return null;
            }

            try {
                if (obj instanceof SimpleEntity) {
                    return (T) obj;
                }

                T t = (T) targetType.newInstance();
                if (obj.getClass() == Long.class) {
                    t.setId((Long) obj);
                } else if (obj.getClass() == String.class) {
                    t.setId(Long.parseLong((String) obj));
                }
                return t;
            } catch (Exception e) {
                return null;
            }
        }
    }
}
