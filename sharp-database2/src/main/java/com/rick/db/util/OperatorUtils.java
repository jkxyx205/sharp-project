package com.rick.db.util;

import com.rick.db.repository.model.EntityId;
import lombok.experimental.UtilityClass;
import org.apache.commons.collections4.CollectionUtils;
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
}
