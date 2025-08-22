package com.rick.db.repository.support;


import com.rick.db.repository.model.EntityId;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;

/**
 * @author Rick
 * @createdAt 2021-10-11 20:28:00
 */
final public class IdToEntityConverterFactory implements ConverterFactory<Object, EntityId> {

    @Override
    public <T extends EntityId> Converter<Object, T> getConverter(Class<T> targetType) {
        return new IdToEntity(targetType);
    }

    private static class IdToEntity<T extends EntityId> implements Converter<Object, T> {

        private Class<?> targetType;

        public <T extends EntityId> IdToEntity(Class<T> targetType) {
            this.targetType = targetType;
        }

        @Override
        public T convert(Object obj) {
            if (obj == null) {
                return null;
            }

            try {
                if (obj instanceof EntityId) {
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
