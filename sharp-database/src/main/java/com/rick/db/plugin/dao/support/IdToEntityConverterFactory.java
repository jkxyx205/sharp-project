package com.rick.db.plugin.dao.support;

import com.rick.db.dto.BasePureEntity;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;

import java.io.Serializable;

/**
 * @author Rick
 * @createdAt 2021-10-11 20:28:00
 */
final public class IdToEntityConverterFactory implements ConverterFactory<Serializable, BasePureEntity> {

    @Override
    public <T extends BasePureEntity> Converter<Serializable, T> getConverter(Class<T> targetType) {
        return new IdToEntityConverterFactory.IdToEntity(targetType);
    }

    private static class IdToEntity<T extends BasePureEntity> implements Converter<Serializable, T> {

        private Class<?> targetType;

        public <T extends BasePureEntity> IdToEntity(Class<T> targetType) {
            this.targetType = targetType;
        }

        @Override
        public T convert(Serializable id) {
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
