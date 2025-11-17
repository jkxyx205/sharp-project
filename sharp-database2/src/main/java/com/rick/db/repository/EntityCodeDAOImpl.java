package com.rick.db.repository;

import com.rick.common.http.exception.BizException;
import com.rick.common.util.Maps;
import com.rick.db.repository.model.EntityIdCode;
import com.rick.db.repository.support.InsertUpdateCallback;
import com.rick.db.util.OperatorUtils;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Rick.Xu
 * @date 2025/8/20 14:05
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
@Validated // 必须加入
public class EntityCodeDAOImpl<T extends EntityIdCode<ID>, ID> extends EntityDAOImpl<T, ID> implements EntityCodeDAO<T, ID> {

    public EntityCodeDAOImpl() {
    }

    public EntityCodeDAOImpl(NamedParameterJdbcTemplate jdbcTemplate, Class<T> entityClass, InsertUpdateCallback insertUpdateCallback) {
        this(new TableDAOImpl(jdbcTemplate), entityClass, insertUpdateCallback);
    }

    public EntityCodeDAOImpl(TableDAO tableDAO, Class<T> entityClass, InsertUpdateCallback insertUpdateCallback) {
       super(tableDAO, entityClass, insertUpdateCallback);
    }

    @Override
    public Collection<T> insertOrUpdate(Collection<T> entityList, @NonNull String refColumnName, @NonNull Object refValue) {
        fillEntityIdsByCodes(this, entityList, refColumnName, refValue);
        return super.insertOrUpdate(entityList, refColumnName, refValue);
    }

    @Override
    public T insertOrUpdate(T entity) {
       if (Objects.isNull(entity.getId())) {
           return insert(entity);
       } else {
           return update(entity);
       }
    }

    @Override
    public T insert(T entity) {
        if (exists("code = ?", new Object[]{entity.getCode()})) {
            throw new BizException("编号已经存在");
        }
        return super.insert(entity);
    }

    @Override
    public T update(T entity) {
        fillEntityIdByCode(entity);
        if (exists("id <> ? AND code = ?", new Object[]{entity.getId(), entity.getCode()})) {
            throw new BizException("编号已经存在");
        }
        return super.update(entity);
    }

    @Override
    public Optional<T> selectByCode(String code) {
        return OperatorUtils.expectedAsOptional(select("code = :code", Maps.of("code", code)));
    }

    @Override
    public List<T> selectByCodes(Collection<String> codes) {
        return select("code IN(:codes)", Maps.of("codes", codes));
    }

    @Override
    public Optional<ID> selectIdByCode(String code) {
        return OperatorUtils.expectedAsOptional(select(getTableMeta().getIdMeta().getIdClass(), getTableMeta().getIdMeta().getIdPropertyName(), "code = :code", Maps.of("code", code)));
    }

    @Override
    public List<ID> selectIdsByCodes(Collection<String> codes) {
        return select(getTableMeta().getIdMeta().getIdClass(), getTableMeta().getIdMeta().getIdPropertyName(), "code IN (:codes)", Maps.of("code", codes));
    }

    private void fillEntityIdByCode(T t) {
        if (Objects.isNull(t.getId()) && StringUtils.isNotBlank(t.getCode())) {
            Optional<ID> option = this.selectIdByCode(t.getCode());
            if (option.isPresent()) {
                t.setId(option.get());
            }
        }
    }

    private void fillEntityIdsByCodes(EntityCodeDAO entityCodeDAO, Collection<T> entities, String refColumnName, Object refValue) {
        if (CollectionUtils.isNotEmpty(entities)) {
            Set<String> emptyIdCodeSet = entities.stream().filter(t -> Objects.isNull(t.getId())).map(EntityIdCode::getCode).collect(Collectors.toSet());
            if (CollectionUtils.isNotEmpty(emptyIdCodeSet)) {
                Map<String, ID> codeIdMap = entityCodeDAO.selectWithoutCascadeSelect("code, id", "code IN (:codes)" + (StringUtils.isBlank(refColumnName) ? "" : (" AND " + refColumnName + " = :refColumnName")), Maps.of("codes", emptyIdCodeSet, "refColumnName", refValue));

                if (MapUtils.isNotEmpty(codeIdMap)) {
                    for (T t : entities) {
                        // fillIds
                        if (codeIdMap.containsKey(t.getCode())) {
                            t.setId(codeIdMap.get(t.getCode()));
                        }
                    }
                }
            }
        }
    }
}
