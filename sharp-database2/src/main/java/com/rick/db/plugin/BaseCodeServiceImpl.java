package com.rick.db.plugin;

import com.rick.db.repository.EntityCodeDAO;
import com.rick.db.repository.model.EntityIdCode;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * @author Rick.Xu
 * @date 2025/8/15 19:21
 */
public class BaseCodeServiceImpl<D extends EntityCodeDAO<T, ID>, T extends EntityIdCode<ID>, ID>
        extends BaseServiceImpl<D, T, ID>
        implements EntityCodeDAO<T, ID> {

    public BaseCodeServiceImpl(D baseDAO) {
        super(baseDAO);
    }

    @Override
    public <S> Optional<S> selectByCode(String code, String columnName, Class<S> clazz) {
        return baseDAO.selectByCode(code, columnName, clazz);
    }

    @Override
    public <S> List<S> selectByCodes(Collection<String> codes, String columnName, Class<S> clazz) {
        return baseDAO.selectByCodes(codes, columnName, clazz);
    }

    @Override
    public Optional<T> selectByCode(String code) {
        return baseDAO.selectByCode(code);
    }

    @Override
    public List<T> selectByCodes(Collection<String> codes) {
        return baseDAO.selectByCodes(codes);
    }

    @Override
    public Optional<ID> selectIdByCode(String code) {
        return baseDAO.selectIdByCode(code);
    }

    @Override
    public List<ID> selectIdsByCodes(Collection<String> codes) {
        return baseDAO.selectIdsByCodes(codes);
    }

}
