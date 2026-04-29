package com.rick.db.repository;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author Rick.Xu
 * @date 2025/8/20 14:00
 */
public interface EntityCodeDAO<T, ID> extends EntityDAO<T, ID> {

    Optional<T> selectByCode(@NotBlank String code);

    List<T> selectByCodes(@NotEmpty Collection<String> codes);

    /**
     * 根据 code & 字段 获取值
     * @param code
     * @param columnName
     * @param clazz
     * @return
     * @param <S>
     */
    <S> Optional<S> selectByCode(@NotBlank String code, @NotBlank String columnName, Class<S> clazz);

    <S> Map<ID, S> selectByCodes(@NotEmpty Collection<String> codes, @NotBlank String columnName, Class<S> clazz);

    Optional<ID> selectIdByCode(@NotBlank String code);

    List<ID> selectIdsByCodes(@NotEmpty Collection<String> codes);

}
