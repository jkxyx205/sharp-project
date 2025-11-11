package com.rick.common.http.web.param;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.ServletModelAttributeMethodProcessor;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Rick
 * @createdAt 2023-03-15 17:32:00
 */
public class ParamNameProcessor extends ServletModelAttributeMethodProcessor {

    @Resource
    private RequestMappingHandlerAdapter requestMappingHandlerAdapter;

    private static final Map<Class<?>, Map<String, String>> PARAM_MAPPINGS_CACHE = new ConcurrentHashMap<>(256);

    public ParamNameProcessor() {
        super(true);
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return !parameter.hasParameterAnnotation(RequestBody.class) &&
                !BeanUtils.isSimpleProperty(parameter.getParameterType()) &&
                        Arrays.stream(parameter.getParameterType().getDeclaredFields())
                                .anyMatch(field -> field.getAnnotation(ParamName.class) != null);
    }

    @Override
    protected void bindRequestParameters(WebDataBinder binder, NativeWebRequest nativeWebRequest) {
        Object target = binder.getTarget();
        Map<String, String> paramMappings = this.getParamMappings(target.getClass());
        ParamNameDataBinder paramNameDataBinder = new ParamNameDataBinder(target, binder.getObjectName(), paramMappings);
        requestMappingHandlerAdapter.getWebBindingInitializer().initBinder(paramNameDataBinder);
        super.bindRequestParameters(paramNameDataBinder, nativeWebRequest);
    }

    /**
     * Get param mappings.
     * Cache param mappings in memory.
     *
     * @param targetClass
     * @return {@link Map<String, String>}
     */
    private Map<String, String> getParamMappings(Class<?> targetClass) {
        if (PARAM_MAPPINGS_CACHE.containsKey(targetClass)) {
            return PARAM_MAPPINGS_CACHE.get(targetClass);
        }
        Field[] fields = targetClass.getDeclaredFields();
        Map<String, String> paramMappings = new HashMap<>(32);
        for (Field field : fields) {
            ParamName paramName = field.getAnnotation(ParamName.class);
            if (paramName != null && ArrayUtils.isNotEmpty(paramName.value())) {
                for (String name : paramName.value()) {
                    if (StringUtils.isNotEmpty(name)) {
                        paramMappings.put(name, field.getName());
                    }
                }
            }
        }
        PARAM_MAPPINGS_CACHE.put(targetClass, paramMappings);
        return paramMappings;
    }

}