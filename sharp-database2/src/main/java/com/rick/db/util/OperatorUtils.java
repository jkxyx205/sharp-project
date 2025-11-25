package com.rick.db.util;

import com.rick.common.function.SFunction;
import com.rick.db.repository.model.EntityId;
import lombok.experimental.UtilityClass;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.dao.IncorrectResultSizeDataAccessException;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Rick.Xu
 * @date 2023/7/29 16:45
 */
@UtilityClass
public class OperatorUtils {

    public static <E> Optional<E> expectedAsOptional(List<E> list) {
        if (CollectionUtils.isEmpty(list)) {
            return Optional.empty();
        }

        if (list.size() > 1) {
            throw new IncorrectResultSizeDataAccessException(1, list.size());
        }

        return Optional.ofNullable(list.get(0));
    }

    public static <ID, T extends EntityId<ID>> Map<ID, T> map(List<T> list) {
        return list.stream().collect(Collectors.toMap(EntityId::getId, Function.identity()));
    }

    public static <R, T> Map<R, T> map(List<T> list, SFunction<T, R> function) {
        return list.stream().collect(Collectors.toMap(t -> function.isMethodReference() ? (R) new BeanWrapperImpl(t).getPropertyValue(function.getPropertyName()) : function.apply(t), Function.identity()));
    }

    public static <ID, T extends EntityId<ID>> Map<ID, List<T>> groupMap(List<T> list) {
        return list.stream().collect(Collectors.groupingBy(EntityId::getId));
    }

    public static <R, T> Map<R, List<T>> groupMap(List<T> list, SFunction<T, R> function) {
        return list.stream().collect(Collectors.groupingBy(t -> function.isMethodReference() ? (R) new BeanWrapperImpl(t).getPropertyValue(function.getPropertyName()) : function.apply(t)));
    }
}
