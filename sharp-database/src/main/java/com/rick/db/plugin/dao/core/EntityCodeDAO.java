package com.rick.db.plugin.dao.core;

import com.rick.db.dto.BaseCodeEntity;

import java.util.*;

/**
 * @author Rick.Xu
 * @date 2023/7/12 11:34
 */
public interface EntityCodeDAO<T extends BaseCodeEntity, ID> extends EntityDAO<T, ID> {

    Optional<T> selectByCode(String code);

    List<T> selectByCodes(Collection<String> codes);

    Long selectIdByCodeOrThrowException(String code);

    Optional<Long> selectIdByCode(String code);

    Optional<String> selectDescriptionByCode(String code);

    <T2> Optional<T2> selectSingleValueByCode(String code, String columnName, Class<T2> clazz);

    List<Long> selectIdsByCodes(Collection<String> codes);

    Optional<String> selectCodeById(Long id);

    Map<String, Long> selectCodeIdMap(Collection<String> codes);

    Map<String, T> selectByCodesAsMap(String... codes);

    Map<String, T> selectByCodesAsMap(Collection<String> codes);

    Map<String, T> selectByCodesAsMap(Map<String, ?> params, String conditionSQL);

    void assertCodeNotExists(String code);

    void assertCodeExists(String code);

    void assertCodeExists(String code, Map<String, Object> conditionParams, String condition);

    void assertCodesExists(Collection<String> codes);

    void assertCodesExists(Collection<String> codes, Map<String, Object> conditionParams, String condition);

    Set<String> selectNotExistsCodes(Collection<String> codes);

    void assertCodesExistsAndUnDuplicate(List<String> codes);

    void assertCodesUnDuplicate(List<String> codes);
}
