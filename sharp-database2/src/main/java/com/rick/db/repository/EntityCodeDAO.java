package com.rick.db.repository;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * @author Rick.Xu
 * @date 2025/8/20 14:00
 */
public interface EntityCodeDAO<T, ID> extends EntityDAO<T, ID> {

    Optional<T> selectByCode(@NotNull String code);

    List<T> selectByCodes(@NotEmpty Collection<String> codes);

    Optional<ID> selectIdByCode(@NotNull String code);

    List<ID> selectIdsByCodes(@NotEmpty Collection<String> codes);

}
