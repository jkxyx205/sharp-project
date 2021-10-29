package com.rick.db.service.support;

import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import java.util.Map;
import java.util.Objects;

/**
 * @author Rick
 * @createdAt 2021-10-22 11:52:00
 */
@Aspect
@Slf4j
public class SelectInterceptor {

    private static final ThreadLocal<Map<String, Object>> selectContext = ThreadLocal.withInitial(() -> Maps.newHashMap());

    @Pointcut("execution(public * com.rick.db.service.SharpService.query(..))")
    public void baseDAOSelectPointCut(){}

    @Around(value = "baseDAOSelectPointCut()")
    public Object selectAround(ProceedingJoinPoint joinPoint) throws Throwable {
        Map<String, Object> map = selectContext.get();
        String key = key(joinPoint);
        Object cachedValue = map.get(key);

        if (Objects.isNull(cachedValue)) {
            cachedValue = joinPoint.proceed();
            map.put(key, cachedValue);
        } else if (log.isDebugEnabled()) {
            log.debug("get data from cache, key = {}", key);
        }
        return cachedValue;
    }

    private String key(ProceedingJoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        StringBuilder hashBuilder = new StringBuilder();
        for (Object arg : args) {
            if (arg == null) {
                hashBuilder.append("null").append(":");
                continue;
            }
//            if (arg instanceof Map) {
//                Map<String, ?> params =  (Map<String, ?>) arg;
//                List<String> keys = params.keySet().stream().sorted().collect(Collectors.toList());
//                for (String key : keys) {
//                    hashBuilder.append(key.hashCode()).append(":").append(params.get(key)).append(":");
//                }
//            }

            hashBuilder.append(arg.hashCode()).append(":");
        }

        return joinPoint.getTarget().toString() + joinPoint.getSignature() + hashBuilder;
    }

}
