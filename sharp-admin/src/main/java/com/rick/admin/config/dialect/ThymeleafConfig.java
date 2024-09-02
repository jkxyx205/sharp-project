package com.rick.admin.config.dialect;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.core.io.CharacterEscapes;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.rick.db.service.SharpService;
import com.rick.formflow.form.service.FormService;
import com.rick.meta.dict.service.DictService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.exceptions.TemplateProcessingException;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.standard.StandardDialect;
import org.thymeleaf.standard.serializer.IStandardJavaScriptSerializer;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.StringTemplateResolver;

import java.io.IOException;
import java.io.Writer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author Rick.Xu
 * @date 2023/5/29 13:45
 */
@Configuration
public class ThymeleafConfig {

    @Bean
    public CustomizeProcessorDialectDialect dictSelectDialect(DictService dictService, SharpService sharpService, FormService formService) {
        return new CustomizeProcessorDialectDialect(dictService, sharpService, formService);
    }

    @Bean
    public TemplateEngine htmlTemplateEngine(){
        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.setEnableSpringELCompiler(true);
        StringTemplateResolver templateResolver = new StringTemplateResolver();

        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateEngine.setTemplateResolver(templateResolver);

        // 自定义解析
        StandardDialect standardDialect = CollectionUtils.findValueOfType(templateEngine.getDialects(), StandardDialect.class);
        standardDialect.setJavaScriptSerializer(new JacksonStandardJavaScriptSerializer());
        return templateEngine;
    }

    private static final class JacksonStandardJavaScriptSerializer implements IStandardJavaScriptSerializer {
        private final ObjectMapper mapper = new ObjectMapper();

        JacksonStandardJavaScriptSerializer() {
            this.mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            this.mapper.disable(new JsonGenerator.Feature[]{JsonGenerator.Feature.AUTO_CLOSE_TARGET});
            this.mapper.enable(new JsonGenerator.Feature[]{JsonGenerator.Feature.ESCAPE_NON_ASCII});
            this.mapper.getFactory().setCharacterEscapes(new JacksonThymeleafCharacterEscapes());
            SimpleModule simpleModule = new SimpleModule();
            simpleModule.addSerializer(Long.class, ToStringSerializer.instance);
            simpleModule.addSerializer(Long.TYPE, ToStringSerializer.instance);
            simpleModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            simpleModule.addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            this.mapper.registerModule(simpleModule);
        }

        public void serializeValue(Object object, Writer writer) {
            try {
                this.mapper.writeValue(writer, object);
            } catch (IOException var4) {
                throw new TemplateProcessingException("An exception was raised while trying to serialize object to JavaScript using Jackson", var4);
            }
        }
    }

    private static final class JacksonThymeleafCharacterEscapes extends CharacterEscapes {
        private static final int[] CHARACTER_ESCAPES = CharacterEscapes.standardAsciiEscapesForJSON();
        private static final SerializableString SLASH_ESCAPE;
        private static final SerializableString AMPERSAND_ESCAPE;

        JacksonThymeleafCharacterEscapes() {
        }

        public int[] getEscapeCodesForAscii() {
            return CHARACTER_ESCAPES;
        }

        public SerializableString getEscapeSequence(int ch) {
            if (ch == 47) {
                return SLASH_ESCAPE;
            } else {
                return ch == 38 ? AMPERSAND_ESCAPE : null;
            }
        }

        static {
            CHARACTER_ESCAPES[47] = -2;
            CHARACTER_ESCAPES[38] = -2;
            SLASH_ESCAPE = new SerializedString("\\/");
            AMPERSAND_ESCAPE = new SerializedString("\\u0026");
        }
    }

}
