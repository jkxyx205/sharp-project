package com.rick.db.plugin.dao.core;

import com.google.common.collect.Sets;
import com.rick.common.http.exception.BizException;
import com.rick.common.http.model.ResultUtils;
import com.rick.common.validate.ValidatorHelper;
import com.rick.db.config.SharpDatabaseProperties;
import com.rick.db.dto.BaseCodeEntity;
import com.rick.db.plugin.dao.support.ColumnAutoFill;
import com.rick.db.plugin.dao.support.ConditionAdvice;
import com.rick.db.service.SharpService;
import com.rick.db.service.support.Params;
import lombok.NonNull;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.core.convert.ConversionService;
import org.springframework.util.Assert;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Rick
 * @createdAt 2023-03-08 22:10:00
 */
public class EntityCodeDAOImpl<T extends BaseCodeEntity, ID> extends EntityDAOImpl<T, ID> implements EntityCodeDAO<T, ID> {

    public EntityCodeDAOImpl() {
        super();
    }

    /**
     * 全部由外部指定
     *
     * @param entityClass
     * @param idClass
     * @param context
     * @param conversionService
     * @param sharpService
     * @param conditionAdvice
     * @param columnAutoFill
     * @param validatorHelper
     * @param sharpDatabaseProperties
     */
    public EntityCodeDAOImpl(Class<T> entityClass, Class<ID> idClass,
                             ApplicationContext context,
                             ConversionService conversionService,
                             SharpService sharpService,
                             ConditionAdvice conditionAdvice,
                             ColumnAutoFill columnAutoFill,
                             ValidatorHelper validatorHelper,
                             SharpDatabaseProperties sharpDatabaseProperties) {


        super(entityClass, idClass, context, conversionService, sharpService, conditionAdvice, columnAutoFill, validatorHelper, sharpDatabaseProperties);
    }

    @Override
    public int[] insertOrUpdate(Collection<T> entities) {
        if (CollectionUtils.isNotEmpty(entities)) {
            Map<String, Long> codeIdMap = this.selectCodeIdMap(entities.stream().map(T::getCode).collect(Collectors.toSet()));
            for (T t : entities) {
                t.setId(codeIdMap.get(t.getCode()));
            }
        }

        return super.insertOrUpdate(entities);
    }

    @Override
    public int[] insertOrUpdate(@NonNull String refColumnName, @NonNull Object refValue, Collection<T> entities) {
        if (CollectionUtils.isNotEmpty(entities)) {
            Set<String> emptyIdCodeSet = entities.stream().filter(t -> Objects.isNull(t.getId())).map(BaseCodeEntity::getCode).collect(Collectors.toSet());
            if (CollectionUtils.isNotEmpty(emptyIdCodeSet)) {
                Map<String, Long> codeIdMap = selectCodeIdMap(emptyIdCodeSet);
                for (T entity : entities) {
                    // fillIds
                    if (codeIdMap.containsKey(entity.getCode())) {
                        entity.setId(codeIdMap.get(entity.getCode()));
                    }
                }
            }
        }

        return super.insertOrUpdate(refColumnName, refValue, entities);
    }

    @Override
    public int insertOrUpdate(T t) {
        if (t.getId() == null && t.getCode() != null) {
            Optional<Long> option = this.selectIdByCode(t.getCode());
            if (option.isPresent()) {
                t.setId(option.get());
            }
        }

        return super.insertOrUpdate(t);
    }

    @Override
    public int insertOrUpdate(Map<String, Object> params) {
        T t = mapToEntity(params);
        int count = insertOrUpdate(t);
        params.put(getIdColumnName(), getIdValue(t));
        return count;
    }

    @Override
    public int insert(T t) {
        // check code if unique
        this.assertCodeNotExists(t.getCode());
        return super.insert(t);
    }

    @Override
    public int update(T t) {
        handleEntityIdBeforeUpdate(t);
        return super.update(t);
    }

    @Override
    public int update(T t, Object[] params, String conditionSQL) {
        handleEntityIdBeforeUpdate(t);
        return super.update(t, params, conditionSQL);
    }

    private void handleEntityIdBeforeUpdate(T t) {
        if (Objects.isNull(t.getId())) {
            Optional<Long> option = this.selectIdByCode(t.getCode());
            if (option.isPresent()) {
                t.setId(option.get());
            }
        }
    }

    /**
     * 根据code查询
     *
     * @param code
     * @return
     */
    @Override
    public Optional<T> selectByCode(String code) {
        Assert.notNull(code, "code cannot be null");
        List<T> list = selectByParams(Params.builder(1).pv("code", code).build(), "code = :code");
        return expectedAsOptional(list);
    }

    /**
     * 根据多个code查询
     *
     * @param codes
     * @return
     */
    @Override
    public List<T> selectByCodes(Collection<String> codes) {
        Assert.notEmpty(codes, "codes cannot be empty");
        return selectByParams(Params.builder(1).pv("codes", codes).build(), "code IN(:codes)");
    }

    @Override
    public Long selectIdByCodeOrThrowException(String code) {
        Assert.notNull(code, "code cannot be null");
        Optional<Long> idOptional = selectIdByCode(code);
        if (idOptional.isPresent()) {
            return idOptional.get();
        }
        throw new BizException(ResultUtils.fail(4040, entityComment() + " code=" + code + " 不存在"));
    }

    /**
     * 根据code获取id
     *
     * @param code
     * @return
     */
    @Override
    public Optional<Long> selectIdByCode(String code) {
        return selectSingleValueByCode(code, "id", Long.class);
    }

    /**
     * 根据code获取description
     *
     * @param code
     * @return
     */
    @Override
    public Optional<String> selectDescriptionByCode(String code) {
        return selectSingleValueByCode(code, "description", String.class);
    }

    @Override
    public <T> Optional<T> selectSingleValueByCode(String code, String columnName, Class<T> clazz) {
        Assert.notNull(code, "code cannot be null");
        List<T> values = selectByParams(Params.builder(1).pv("code", code).build(), columnName, "code = :code", clazz);
        return expectedAsOptional(values);
    }

    /**
     * 根据codes获取ids
     *
     * @param codes
     * @return
     */
    @Override
    public List<Long> selectIdsByCodes(Collection<String> codes) {
        Assert.notEmpty(codes, "codes cannot be empty");
        List<Long> ids = selectByParams(Params.builder(1).pv("codes", codes).build(), "id", "code IN (:codes)", Long.class);
        return ids;
    }

    /**
     * 根据id获取code
     *
     * @param id
     * @return
     */
    @Override
    public Optional<String> selectCodeById(Long id) {
        Assert.notNull(id, "id cannot be null");
        List<String> ids = selectByParams(Params.builder(1).pv("id", id).build(), "code", "id = :id", String.class);
        return expectedAsOptional(ids);
    }

    /**
     * key: code
     * value: id
     *
     * @param codes
     * @return
     */
    @Override
    public Map<String, Long> selectCodeIdMap(Collection<String> codes) {
        return selectByParamsAsMap(Params.builder(1).pv("codes", codes).build(), "code, id", "code IN (:codes)");
    }

    @Override
    public Map<String, T> selectByCodesAsMap(String... codes) {
        return selectByCodesAsMap(Arrays.asList(codes));
    }

    @Override
    public Map<String, T> selectByCodesAsMap(Collection<String> codes) {
        List<T> list = selectByCodes(codes);
        return list.stream().collect(Collectors.toMap(t -> t.getCode(), v -> v));
    }

    @Override
    public Map<String, T> selectByCodesAsMap(Map<String, ?> params, String conditionSQL) {
        List<T> list = selectByParams(params, conditionSQL);
        return list.stream().collect(Collectors.toMap(t -> t.getCode(), v -> v));
    }

    @Override
    public void assertCodeNotExists(String code) {
        Assert.notNull(code, "code cannot be null");

        if (existsByParams(Params.builder(1).pv("code", code).build(), "code = :code")) {
            throw new BizException(ResultUtils.fail(400, entityComment() + " code=" + code + " 已经存在", code));
        }
    }

    @Override
    public void assertCodeExists(String code) {
        assertCodeExists(code, Collections.emptyMap(), null);
    }

    @Override
    public void assertCodeExists(String code, Map<String, Object> conditionParams, String condition) {
        Assert.notNull(code, "code cannot be null");
        if (!existsByParams(Params.builder(1 + conditionParams.size()).pv("code", code).pvAll(conditionParams).build(), "code = :code" + (StringUtils.isBlank(condition) ? "" : " AND " + condition))) {
            throw new BizException(ResultUtils.fail(404, entityComment() + " code=" + code + "不存在", code));
        }
    }

    @Override
    public void assertCodesExists(Collection<String> codes) {
        assertCodesExists(codes, Collections.emptyMap(), null);
    }

    @Override
    public void assertCodesExists(Collection<String> codes, Map<String, Object> conditionParams, String condition) {
        Assert.notEmpty(codes, "code cannot be empty");

        List<String> codesInDB = selectByParams(Params.builder(1 + conditionParams.size())
                        .pv("codes", codes).pvAll(conditionParams).build(),
                "code", "code in (:codes)" + (StringUtils.isBlank(condition) ? "" : " AND " + condition), String.class);

        SetUtils.SetView<String> difference = SetUtils.difference(Sets.newHashSet(codes), Sets.newHashSet(codesInDB));
        if (CollectionUtils.isNotEmpty(difference)) {
            throw new BizException(ResultUtils.fail(404, entityComment() + " code=" + StringUtils.join(difference.toArray(), ",") + "不存在", difference.toArray()));
        }
    }

    @Override
    public Set<String> selectNotExistsCodes(Collection<String> codes) {
        if (CollectionUtils.isEmpty(codes)) {
            Collections.emptySet();
        }

        return SetUtils.difference(Sets.newHashSet(codes), selectCodeIdMap(codes).keySet());
    }

    /**
     * 检查code
     * 1. 不重复
     * 2. 存在code
     *
     * @param codes 待检查的code集合
     */
    @Override
    public void assertCodesExistsAndUnDuplicate(List<String> codes) {
        if (CollectionUtils.isEmpty(codes)) {
            return;
        }

        assertCodesUnDuplicate(codes);

        Set<String> notExistsCodes = selectNotExistsCodes(codes);
        if (notExistsCodes.size() > 0) {
            throw new BizException(ResultUtils.fail(4040, entityComment() + " code=" + StringUtils.join(notExistsCodes, ",") + " 不存在"));
        }
    }

    /**
     * 检查是否重复
     *
     * @param codes 待检查的code集合
     */
    @Override
    public void assertCodesUnDuplicate(List<String> codes) {
        Map<String, Long> codeOccurrenceMap = codes.stream().collect(Collectors.groupingBy(code -> code, Collectors.counting()));
        Set<String> codeOccurrenceErrors = codeOccurrenceMap.keySet().stream().filter(m -> codeOccurrenceMap.get(m) > 1).collect(Collectors.toSet());

        if (codeOccurrenceErrors.size() > 0) {
            throw new BizException(ResultUtils.fail(4040, entityComment() + " code=" + StringUtils.join(codeOccurrenceErrors, ",") + " 已经重复"));
        }
    }

    private String entityComment() {
        return EntityDAOManager.getTableMeta(getEntityClass()).getTable().comment();
    }
}
