package com.rick.db.plugin.dao.support;

import com.rick.db.dto.BasePureEntity;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;

/**
 * @author Rick
 * @createdAt 2021-10-11 20:28:00
 */
final public class IdToEntityConverterFactory implements ConverterFactory<Long, BasePureEntity> {

    @Override
    public <T extends BasePureEntity> Converter<Long, T> getConverter(Class<T> targetType) {
        return new IdToEntityConverterFactory.IdToEntity(targetType);
    }

    private static class IdToEntity<T extends BasePureEntity> implements Converter<Long, T> {

        private Class<?> targetType;

        public <T extends BasePureEntity> IdToEntity(Class<T> targetType) {
            this.targetType = targetType;
        }

        @Override
        public T convert(Long id) {
            try {
                T t = (T) targetType.newInstance();
                t.setId(id);
                return t;
            } catch (Exception e) {
                return null;
            }
        }
    }
}
