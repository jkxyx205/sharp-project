package com.rick.db.repository;

import com.rick.common.util.Maps;
import com.rick.db.repository.model.EntityIdCode;
import com.rick.db.util.OperatorUtils;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * @author Rick.Xu
 * @date 2025/8/20 14:05
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
@Validated // 必须加入
public class EntityCodeDAOImpl<T extends EntityIdCode<ID>, ID> extends EntityDAOImpl<T, ID> implements EntityCodeDAO<T, ID> {

    @Override
    public Optional<T> selectByCode(String code) {
        return OperatorUtils.expectedAsOptional(select("code = :code", Maps.of("code", code)));
    }

    @Override
    public List<T> selectByCodes(Collection<String> codes) {
        return select("code IN(:codes)", Maps.of("codes", codes));
    }

    @Override
    public T insertOrUpdate(T entity) {
        if (exists("id <> ? AND code = ?", new Object[]{ObjectUtils.defaultIfNull(entity.getId(), Integer.MIN_VALUE), entity.getCode()})) {
            throw new RuntimeException("编号已经存在");
        }

        return super.insertOrUpdate(entity);
    }
}
