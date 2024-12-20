package com.rick.admin.config.dialect;

import com.google.common.collect.Sets;
import com.rick.admin.config.dialect.processor.*;
import com.rick.admin.module.codeinput.service.CodeInputService;
import com.rick.db.service.SharpService;
import com.rick.formflow.form.service.FormService;
import com.rick.meta.dict.service.DictService;
import org.thymeleaf.dialect.AbstractProcessorDialect;
import org.thymeleaf.processor.IProcessor;
import org.thymeleaf.standard.StandardDialect;

import java.util.Set;

/**
 * @author Rick.Xu
 * @date 2023/5/29 13:45
 */
public class CustomizeProcessorDialectDialect extends AbstractProcessorDialect {

    /**
     * 定义方言名称
     */
    private static final String DIALECT_NAME = "Customize Dialect";

    private static final String DIALECT_PREFIX = "sp";

    private final DictService dictService;

    private final SharpService sharpService;

    private final FormService formService;

    private final CodeInputService codeInputService;

    public CustomizeProcessorDialectDialect(DictService dictService, SharpService sharpService, FormService formService, CodeInputService codeInputService) {
        super(DIALECT_NAME, DIALECT_PREFIX, StandardDialect.PROCESSOR_PRECEDENCE);
        this.dictService = dictService;
        this.sharpService = sharpService;
        this.formService = formService;
        this.codeInputService = codeInputService;
    }

    @Override
    public Set<IProcessor> getProcessors(String s) {
        Set<IProcessor> processors = Sets.newHashSetWithExpectedSize(3);

        // 添加自定义标签
        processors.add(new SelectProcessor(DIALECT_PREFIX, dictService, sharpService));
        processors.add(new SelectOptionProcessor(DIALECT_PREFIX, dictService, sharpService));
        processors.add(new SharpFormProcessor(DIALECT_PREFIX, formService));
        processors.add(new FileUploadProcessor(DIALECT_PREFIX));
        processors.add(new DataAttributeTagProcessor(DIALECT_PREFIX));
        processors.add(new CodeInputProcessor(DIALECT_PREFIX, codeInputService));
        processors.add(new SpanProcessor(DIALECT_PREFIX, dictService));
        return processors;
    }
}
