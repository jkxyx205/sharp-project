package com.rick.admin.config.dialect.processor;

import com.rick.common.util.JsonUtils;
import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.engine.AttributeName;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.AbstractAttributeTagProcessor;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.standard.StandardDialect;
import org.thymeleaf.standard.expression.IStandardExpression;
import org.thymeleaf.standard.expression.IStandardExpressionParser;
import org.thymeleaf.standard.expression.StandardExpressions;
import org.thymeleaf.templatemode.TemplateMode;

import java.util.Objects;

/**
 * @author Rick.Xu
 * @date 2024/9/8 07:07
 */
public class DataAttributeTagProcessor extends AbstractAttributeTagProcessor {

    private static final String TEXT_ATTRIBUTE = "value";

    public DataAttributeTagProcessor( String dialectPrefix) {
        // 方言名称，前缀，处理优先级
        super(TemplateMode.HTML, dialectPrefix, null, false, TEXT_ATTRIBUTE, true, StandardDialect.PROCESSOR_PRECEDENCE, true);
    }

    @Override
    protected void doProcess(ITemplateContext iTemplateContext, IProcessableElementTag iProcessableElementTag, AttributeName attributeName, String s, IElementTagStructureHandler iElementTagStructureHandler) {
        //s为自定义属性text的内容，如果s为表达式，该函数可以获取表达式的值
        final Object value = getExpressionValue(iTemplateContext, s);
        iElementTagStructureHandler.setAttribute("value", Objects.isNull(value) ? null : JsonUtils.toJson(value));
    }

    private Object getExpressionValue(ITemplateContext iTemplateContext, String expressionString) {
        final IEngineConfiguration configuration = iTemplateContext.getConfiguration();
        final IStandardExpressionParser parser = StandardExpressions.getExpressionParser(configuration);
        // 解析expression
        final IStandardExpression expression = parser.parseExpression(iTemplateContext, expressionString);
        // 获取expression的执行结果
        return expression.execute(iTemplateContext);
    }
}
