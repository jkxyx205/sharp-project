package com.rick.demo.module.school.converter;

import com.rick.demo.module.school.entity.Evaluate;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.stereotype.Component;

/**
 * 自定义转化
 * @author Rick
 * @createdAt 2022-04-29 23:39:00
 */
@Component
public class EvaluateConverterFactory implements ConverterFactory<String, Evaluate> {

    @Override
    public <T extends Evaluate> Converter<String, T> getConverter(Class<T> aClass) {
        return value -> {
            // value => Evaluate{grade=1, description='GREAT!'}

            String substring = value.substring(value.indexOf("{") + 1, value.length() - 1);
            String[] values = substring.split(",\\s+");

            return (T) new Evaluate(Integer.parseInt(values[0].split("=")[1]), values[1].split("=")[1]);
        };
    }
}
