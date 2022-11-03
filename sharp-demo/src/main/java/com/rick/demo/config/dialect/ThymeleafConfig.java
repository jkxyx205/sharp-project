package com.rick.demo.config.dialect;

import com.rick.meta.dict.service.DictService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.StringTemplateResolver;

/**
 * All rights Reserved, Designed By www.xhope.top
 *
 * @version V1.0
 * @Description: (用一句话描述该文件做什么)
 * @author: Rick.Xu
 * @date: 9/27/19 11:54 AM
 * @Copyright: 2019 www.yodean.com. All rights reserved.
 */
@Configuration
public class ThymeleafConfig {

    /**
     * 系统方言
     *
     * @return
     */
    @Bean
    public DictSelectDialect dictSelectDialect(DictService dictService) {
        return new DictSelectDialect(dictService);
    }

    @Bean
    public TemplateEngine htmlTemplateEngine(){
        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.setEnableSpringELCompiler(true);
        StringTemplateResolver templateResolver = new StringTemplateResolver();

        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateEngine.setTemplateResolver(templateResolver);
        return templateEngine;
    }
}
