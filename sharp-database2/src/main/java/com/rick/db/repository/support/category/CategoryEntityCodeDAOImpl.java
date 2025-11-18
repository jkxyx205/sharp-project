package com.rick.db.repository.support.category;

import com.rick.common.http.exception.BizException;
import com.rick.common.util.Maps;
import com.rick.db.repository.EntityCodeDAO;
import com.rick.db.repository.EntityCodeDAOImpl;
import com.rick.db.repository.model.EntityIdCode;
import com.rick.db.util.OperatorUtils;
import lombok.NonNull;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.map.MultiKeyMap;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 如果有通过 category 会区分不同的分组，比如 CodeDescription
 * 编辑器报错，但是可以通过编译。本质是 Lombok 的 @SuperBuilder + 泛型继承 + 多重边界接口(RowCategory) 组合使用导致的 builder() 冲突
 * @author Rick.Xu
 * @date 2025/11/14 11:59
 */
public class CategoryEntityCodeDAOImpl<T extends EntityIdCode<ID> & RowCategory<E>, ID, E extends Enum<E>> extends EntityCodeDAOImpl<T, ID> {

    public void insert(@NotNull E category, Collection<T> list) {
        if (CollectionUtils.isNotEmpty(list)) {
            for (T t : list) {
                 t.setCategory(category);
            }
        }

        insertOrUpdate(list, "category", category.name());
    }

    @Override
    public T insert(T entity) {
        if (exists("code = ? AND category = ?", new Object[]{entity.getCode(), entity.getCategory().name()})) {
            throw new BizException("编号已经存在");
        }
        return insertOrUpdate0(entity, true);
    }

    @Override
    public T update(T entity) {
        fillEntityIdByCode(entity);
        if (exists("id <> ? AND code = ? AND category = ?", new Object[]{entity.getId(), entity.getCode(), entity.getCategory().name()})) {
            throw new BizException("编号已经存在");
        }
        return insertOrUpdate0(entity, false);
    }

    @Override
    public Collection<T> insertOrUpdate(Collection<T> entityList, @NonNull String refColumnName, @NonNull Object refValue) {
        fillEntityIdsByCodes(this, entityList);
        return super.insertOrUpdate(entityList, refColumnName, refValue);
    }

    public Optional<T> selectByCategoryAndCode(@NotNull E category, @NotBlank String code) {
        return OperatorUtils.expectedAsOptional(select("code = ? AND category = ?", code, category.name()));
    }

    public List<T> selectAll(@NotNull E category) {
        return select("category = :category", Maps.of("category", category.name()));
    }

    private void fillEntityIdByCode(T t) {
        if (Objects.isNull(t.getId()) && StringUtils.isNotBlank(t.getCode())) {
            Optional<T> option = selectByCategoryAndCode(t.getCategory(), t.getCode());
            if (option.isPresent()) {
                t.setId(option.get().getId());
            }
        }
    }

    private void fillEntityIdsByCodes(EntityCodeDAO<T, ID> entityCodeDAO, Collection<T> entities) {
        if (CollectionUtils.isNotEmpty(entities)) {
            Set<String> emptyIdCategorySet = entities.stream().filter(t -> Objects.isNull(t.getId())).map(e -> e.getCategory().name()).collect(Collectors.toSet());
            if (CollectionUtils.isNotEmpty(emptyIdCategorySet)) {

                List<T> list = entityCodeDAO.selectWithoutCascadeSelect(entityCodeDAO.getTableMeta().getEntityClass(), "code, id, category", "category IN (:category)", Maps.of("category", emptyIdCategorySet));
                if (CollectionUtils.isNotEmpty(list)) {
                    MultiKeyMap<Object, ID> multiKeyMap = new MultiKeyMap();

                    for (T t : list) {
                        multiKeyMap.put(t.getCategory(), t.getCode(), t.getId());
                    }

                    for (T t : entities) {
                        // fillIds
                        if (Objects.isNull(t.getId()) && multiKeyMap.containsKey(t.getCategory(), t.getCode())) {
                            t.setId(multiKeyMap.get(t.getCategory(), t.getCode()));
                        }
                    }
                }
            }
        }
    }
}