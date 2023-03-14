package com.rick.demo.converter;

import com.rick.demo.module.project.domain.enums.SexEnum;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.convert.ConversionService;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Rick
 * @createdAt 2021-10-26 10:06:00
 */
@SpringBootTest
public class ConverterTest {

    @Autowired
    @Qualifier("dbConversionService")
    private ConversionService formattingConversionService;

    @Test
    public void testConvert() {
        Integer result = formattingConversionService.convert("2", Integer.class);
        assertThat(result).isEqualTo(2);

        String convert = formattingConversionService.convert(Instant.now(), String.class);
        System.out.println(convert);

        SexEnum sex = formattingConversionService.convert(2, SexEnum.class);
        System.out.println(sex);

    }
}
