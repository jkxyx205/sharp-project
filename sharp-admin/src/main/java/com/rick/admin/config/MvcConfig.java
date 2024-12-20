package com.rick.admin.config;

import com.rick.admin.auth.common.UrlHandlerInterceptor;
import com.rick.admin.common.servlet.AccessFilter;
import com.rick.common.http.exception.ApiExceptionHandler;
import com.rick.common.http.util.MessageUtils;
import com.rick.common.http.web.SharpWebMvcConfigurer;
import com.rick.db.plugin.dao.support.IdToEntityConverterFactory;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * @author Rick.Xu
 * @date 2023/5/29 13:45
 */
@Configuration
@ComponentScan(basePackageClasses = {ApiExceptionHandler.class, MessageUtils.class})
public class MvcConfig extends SharpWebMvcConfigurer {

    @Resource
    private MessageSource messageSource;

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/home").setViewName("index");
        registry.addViewController("/index").setViewName("index");
        registry.addViewController("/").setViewName("index");
        registry.addViewController("/forbidden").setViewName("error/forbidden");
        registry.addViewController("/error/index").setViewName("error/index");
        registry.addViewController("/releases").setViewName("changeLog");
    }

    @Override
    public List<ConverterFactory> converterFactories() {
        // 发起 GET 请求的时候，允许值映射到实体对象的 id 字段上，不常用，提供传参的多样性
        // private Person person;
        // GET person = "1" => person.setId(1L)
        return Arrays.asList(new IdToEntityConverterFactory());
    }

    @Bean
    public UrlHandlerInterceptor urlHandlerInterceptor() {
        return new UrlHandlerInterceptor();
    }

    @Bean
    public FilterRegistrationBean httpServletRequestReplacedFilter() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new AccessFilter());
        // /* 是全部的请求拦截，和Interceptor的拦截地址/**区别开
        registration.addUrlPatterns("/*");
        registration.setName("accessRequestFilter");
        registration.setOrder(1);
        return registration;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(urlHandlerInterceptor())
                .excludePathPatterns("/coreui/**", "/css/**", "/js/**", "/img/**", "/plugins/**", "/favicon.ico");

        LocaleChangeInterceptor localeInterceptor = new LocaleChangeInterceptor();
        // ?lang=zh_CN
        localeInterceptor.setParamName("lang");
        registry.addInterceptor(localeInterceptor);
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new UserHandlerMethodArgumentResolver());
    }

    @Bean
    public LocaleResolver localeResolver() {
        CookieLocaleResolver cookieLocaleResolver = new CookieLocaleResolver();
        cookieLocaleResolver.setDefaultLocale(Locale.CHINA);
        cookieLocaleResolver.setCookieName("lang");
        cookieLocaleResolver.setCookieMaxAge(-1);
        return cookieLocaleResolver;
    }

    @Bean
    public LocalValidatorFactoryBean defaultValidator() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.setValidationMessageSource(messageSource);
        return validator;
    }
}
