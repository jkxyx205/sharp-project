package com.rick.db.service.support;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import java.util.concurrent.TimeUnit;

/**
 * @author Rick
 * @createdAt 2021-10-22 11:52:00
 */
@Aspect
@Slf4j
public class SharpServiceQueryInterceptor {

    private Cache<String, Object> cache = CacheBuilder.newBuilder()
            // 设置缓存的最大容量
            .maximumSize(100)
            // 设置缓存在写入一分钟后失效
            .expireAfterWrite(1, TimeUnit.SECONDS)
            // 设置并发级别为10
            .concurrencyLevel(10)
//            .recordStats() // 开启缓存统计
            .build();

    @Pointcut("execution(public * com.rick.db.service.SharpService.query(..))")
    public void sharpServiceQuery(){}

    @Around(value = "sharpServiceQuery()")
    public Object selectAround(ProceedingJoinPoint joinPoint) throws Throwable {
        String key = key(joinPoint);
        return cache.get(key, () -> {
            try {
                return joinPoint.proceed();
            } catch (Throwable throwable) {
                throw new Exception(throwable);
            }
        });
    }

    private String key(ProceedingJoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        StringBuilder hashBuilder = new StringBuilder();
        for (Object arg : args) {
            if (arg == null) {
                hashBuilder.append("null").append(":");
                continue;
            }

            hashBuilder.append(arg.hashCode()).append(":");
        }

        return joinPoint.getTarget().toString() + joinPoint.getSignature() + hashBuilder;
    }

}
