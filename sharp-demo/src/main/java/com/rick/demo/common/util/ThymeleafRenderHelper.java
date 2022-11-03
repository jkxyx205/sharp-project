package com.rick.demo.common.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

/**ø
 * @author Rick
 * @createdAt 2021-04-16 20:14:00
 */
@Component
public final class ThymeleafRenderHelper {

    private static TemplateEngine stringTemplateEngine;

    private static TemplateEngine htmlTemplateEngine;

    private ThymeleafRenderHelper() {}

    @Autowired
    @Qualifier("templateEngine")
    public void setStringTemplateEngine(TemplateEngine stringTemplateEngine) {
        ThymeleafRenderHelper.stringTemplateEngine = stringTemplateEngine;
    }
    @Autowired
    public void setHtmlTemplateEngine(TemplateEngine htmlTemplateEngine) {
        ThymeleafRenderHelper.htmlTemplateEngine = htmlTemplateEngine;
    }

    public static String renderByHtmlContent(String html, Map<String, Object> params) {
        final Context ctx = new Context(LocaleContextHolder.getLocale(), params);
        String template = htmlTemplateEngine.process(html, ctx);
        return template;
    }

    public static String renderByTemplateName(String templateName, Map<String, Object> params) {
        return stringTemplateEngine.process(templateName, new Context(LocaleContextHolder.getLocale(), params));
    }

}
