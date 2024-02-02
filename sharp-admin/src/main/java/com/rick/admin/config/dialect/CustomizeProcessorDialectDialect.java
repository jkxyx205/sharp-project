package com.rick.admin.config.dialect;

import com.google.common.collect.Sets;
import com.rick.admin.config.dialect.processor.SelectOptionProcessor;
import com.rick.admin.config.dialect.processor.SelectProcessor;
import com.rick.db.service.SharpService;
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

    public CustomizeProcessorDialectDialect(DictService dictService, SharpService sharpService) {
        super(DIALECT_NAME, DIALECT_PREFIX, StandardDialect.PROCESSOR_PRECEDENCE);
        this.dictService = dictService;
        this.sharpService = sharpService;
    }

    @Override
    public Set<IProcessor> getProcessors(String s) {
        Set<IProcessor> processors = Sets.newHashSetWithExpectedSize(1);

        // 添加自定义标签
        processors.add(new SelectProcessor(DIALECT_PREFIX, dictService, sharpService));
        processors.add(new SelectOptionProcessor(DIALECT_PREFIX, dictService, sharpService));
        return processors;
    }
}
