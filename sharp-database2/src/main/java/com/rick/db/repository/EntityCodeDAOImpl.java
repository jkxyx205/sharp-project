package com.rick.db.repository;

import com.rick.common.http.exception.BizException;
import com.rick.common.util.Maps;
import com.rick.db.repository.model.EntityIdCode;
import com.rick.db.util.OperatorUtils;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Rick.Xu
 * @date 2025/8/20 14:05
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
@Validated // 必须加入
public class EntityCodeDAOImpl<T extends EntityIdCode<ID>, ID> extends EntityDAOImpl<T, ID> implements EntityCodeDAO<T, ID> {

    public EntityCodeDAOImpl() {
    }

    public EntityCodeDAOImpl(NamedParameterJdbcTemplate jdbcTemplate, Class<T> entityClass) {
        this(new TableDAOImpl(jdbcTemplate), entityClass);
    }

    public EntityCodeDAOImpl(TableDAO tableDAO, Class<T> entityClass) {
       super(tableDAO, entityClass);
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

}
