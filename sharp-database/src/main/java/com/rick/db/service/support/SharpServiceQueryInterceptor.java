package com.rick.db.service.support;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Pointcut;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Rick
 * @createdAt 2021-10-22 11:52:00
 */
//@Aspect
@Slf4j
public class SharpServiceQueryInterceptor {

    private ThreadLocal<Map<String, Object>> threadLocalContainer = ThreadLocal.withInitial(() -> new HashMap<>());


    @Pointcut("execution(public * com.rick.db.service.SharpService.query(..))")
    public void sharpServiceQuery(){}

    @Around(value = "sharpServiceQuery()")
    public Object selectAround(ProceedingJoinPoint joinPoint) throws Throwable {
        String key = key(joinPoint);
        Object threadValue = get(key);

        if (log.isDebugEnabled() && Objects.nonNull(threadValue)) {
            log.debug("Get value from threadLocal");
        }

        if (Objects.isNull(threadValue)) {
            threadValue = joinPoint.proceed();
            add(key, threadValue);
        }
        return threadValue;
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

    private void add(String key, Object value) {
        threadLocalContainer.get().put(key, value);
    }

    private Object get(String key) {
        return threadLocalContainer.get().get(key);
    }

}
