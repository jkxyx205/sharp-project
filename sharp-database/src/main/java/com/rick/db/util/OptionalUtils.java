package com.rick.db.util;

import lombok.experimental.UtilityClass;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.dao.IncorrectResultSizeDataAccessException;

import java.util.List;
import java.util.Optional;

/**
 * @author Rick.Xu
 * @date 2023/7/29 16:45
 */
@UtilityClass
public class OptionalUtils {

    public static <E> Optional<E> expectedAsOptional(List<E> list) {
        if (CollectionUtils.isEmpty(list)) {
            return Optional.empty();
        }

        if (list.size() > 1) {
            throw new IncorrectResultSizeDataAccessException(1, list.size());
        }

        return Optional.ofNullable(list.get(0));
    }
}
