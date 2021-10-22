package com.rick.common.validate;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;

import java.lang.reflect.Method;

/**
 * @author Rick
 * @createdAt 2021-10-22 11:52:00
 */
@Aspect
public class ServiceMethodValidationInterceptor {

    @Autowired
    private ValidatorHelper validatorHelper;

    /**
     * 扫描service目录下，类名是Service结尾的类
     */
    @Pointcut("execution(public * *..*.service.*Service.*(..))")
    public void serviceParamsValidPointcut(){}

    @Before("serviceParamsValidPointcut()")
    public void valid(JoinPoint joinPoint) {
        Method method = MethodSignature.class.cast(joinPoint.getSignature()).getMethod();
        // 如果类上有注解Validated 交给Spring去处理
        if (joinPoint.getTarget().getClass().getAnnotation(Validated.class) == null && method.getAnnotation(Validated.class) != null) {
            validatorHelper.validate(joinPoint.getTarget(), method, joinPoint.getArgs());
        }
    }
}
