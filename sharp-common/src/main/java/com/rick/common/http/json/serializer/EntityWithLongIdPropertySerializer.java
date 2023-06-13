package com.rick.common.http.json.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import lombok.NoArgsConstructor;
import org.springframework.util.ReflectionUtils;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Rick.Xu
 * @date 2023/6/13 22:24
 */
@NoArgsConstructor
public class EntityWithLongIdPropertySerializer<T> extends JsonSerializer<T>  implements ContextualSerializer {
    private JavaType javaType;

    public EntityWithLongIdPropertySerializer(JavaType javaType) {
        this.javaType = javaType;
    }

    @Override
    public void serialize(T t, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if (List.class.isAssignableFrom(this.javaType.getRawClass())) {
            List<?> list = (List<?>) t;
            List<Object> ids = list.stream().map(e -> invokeGetIdMethod(e)).collect(Collectors.toList());
            jsonGenerator.writeObject(ids);
        } else {
            Object id = invokeGetIdMethod(t);
            jsonGenerator.writeObject(id);
        }
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider serializerProvider, BeanProperty beanProperty) {
        return new EntityWithLongIdPropertySerializer<>(beanProperty.getType());
    }

    private Object invokeGetIdMethod(Object t) {
        try {
            return ReflectionUtils.invokeMethod(t.getClass().getMethod("getId"), t);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
