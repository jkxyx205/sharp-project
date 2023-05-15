package com.rick.db.plugin;

import com.rick.db.service.SharpService;
import com.rick.db.service.support.Params;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.util.ReflectionUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.rick.common.util.StringUtils.stringToCamel;

/**
 * @author Rick.Xu
 * @date 2023/5/15 17:13
 */
public final class QueryUtils {

    private static SharpService sharpService;

    private QueryUtils() {}

    public static void setSharpService(SharpService sharpService) {
        if (QueryUtils.sharpService != null) {
            throw new BeanCreationException("bean has init already!");
        }

        QueryUtils.sharpService = sharpService;
    }

    /**
     *  select id, name, grade, sex from t_school_student where school_id IN (552173736070144000);
     *
     * @param selectColumnNames id, name, grade, sex
     * @param queryObject t_school_student
     * @param refColumnName school_id
     * @param refValue [552173736070144000]
     * @param tClass 子表返回的bean
     * @return map（school_id, List<tClass>)
     * @param <T>
     */
    public static <T> Map<?, List<T>> subTableValueMap(String selectColumnNames, String queryObject, String refColumnName, Collection<?> refValue, Class<T> tClass) {
        List<T> list = sharpService.query("select " + selectColumnNames + " from " + queryObject + " where " + refColumnName + " IN (:refColumnName)", Params.builder(1).pv("refColumnName", refValue).build(), tClass);
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyMap();
        }

        String groupPropertyName = stringToCamel(refColumnName);
        return subTableValueMap(list, groupPropertyName, tClass);
    }

    public static <T> Map<?, List<T>> subTableValueMap(List<T> list, String groupPropertyName, Class<T> tClass) {
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyMap();
        }

        Map<?, List<T>> map = list.stream().collect(Collectors.groupingBy(t -> ReflectionUtils.invokeMethod(ReflectionUtils.findMethod(tClass, "get" + groupPropertyName.substring(0, 1).toUpperCase() + groupPropertyName.substring(1)), t), Collectors.mapping(Function.identity(), Collectors.toList())));
        return map;
    }
}
