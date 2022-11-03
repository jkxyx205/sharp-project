package com.rick.demo.config.dialect;

import com.google.common.collect.Sets;
import com.rick.meta.dict.service.DictService;
import org.thymeleaf.dialect.AbstractProcessorDialect;
import org.thymeleaf.processor.IProcessor;
import org.thymeleaf.standard.StandardDialect;

import java.util.Set;

/**
 * All rights Reserved, Designed By www.xhope.top
 *
 * @version V1.0
 * @Description: (用一句话描述该文件做什么)
 * @author: Rick.Xu
 * @date: 9/27/19 11:38 AM
 * @Copyright: 2019 www.yodean.com. All rights reserved.
 */
public class DictSelectDialect extends AbstractProcessorDialect {

    // 定义方言名称
    private static final String DIALECT_NAME = "Dict Dialect";

    private static final String DIALECT_PREFIX = "sp";

    private DictService dictService;

    public DictSelectDialect(DictService dictService) {
        super(DIALECT_NAME, DIALECT_PREFIX, StandardDialect.PROCESSOR_PRECEDENCE);
        this.dictService = dictService;
    }

    @Override
    public Set<IProcessor> getProcessors(String s) {
        Set<IProcessor> processors = Sets.newHashSetWithExpectedSize(1);

        // 添加自定义标签
        processors.add(new DictTagProcessor(DIALECT_PREFIX, dictService));
//        processors.add(new StandardXmlNsTagProcessor(TemplateMode.HTML, DIALECT_PREFIX));
        return processors;
    }
}
