package com.rick.demo.module.project.converter;

import com.rick.demo.module.project.domain.entity.Dept;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.stereotype.Component;

/**
 * 自定义转化
 * @author Rick
 * @createdAt 2022-04-29 23:39:00
 */
@Component
public class DeptConverterFactory implements ConverterFactory<String, Dept> {

    @Override
    public <T extends Dept> Converter<String, T> getConverter(Class<T> aClass) {
        return value -> {
            String[] split = value.split("&");

            return (T) new Dept(Long.parseLong(split[0].substring(3)),
                    split[1].substring(4),
                    Long.parseLong(split[2].substring(9)));
        };
    }
}
