package com.rick.db.service;


import com.rick.db.dto.BaseCodeEntity;
import com.rick.db.plugin.dao.core.EntityCodeDAO;

import java.util.*;

/**
 * 带 code 的实体 Service 基类，code 相关操作委托给 baseDAO
 *
 * @author Rick.Xu
 * @date 2025/8/15 19:21
 */
public class BaseCodeServiceImpl<D extends EntityCodeDAO<T, ID>, T extends BaseCodeEntity<ID>, ID>
        extends BaseServiceImpl<D, T, ID> {

    public BaseCodeServiceImpl(D baseDAO) {
        super(baseDAO);
    }

    /**
     * 根据code查询
     *
     * @param code
     * @return
     */
    public Optional<T> findByCode(String code) {
        return baseDAO.selectByCode(code);
    }

    /**
     * 根据多个code查询
     *
     * @param codes
     * @return
     */
    public List<T> findByCodes(Collection<String> codes) {
        return baseDAO.selectByCodes(codes);
    }

    /**
     * 根据code获取id
     *
     * @param code
     * @return
     */
    public Optional<ID> findIdByCode(String code) {
        return baseDAO.selectIdByCode(code);
    }

    /**
     * 根据code获取id，不存在抛异常
     *
     * @param code
     * @return
     */
    public ID findIdByCodeOrThrowException(String code) {
        return baseDAO.selectIdByCodeOrThrowException(code);
    }

    /**
     * 根据codes获取ids
     *
     * @param codes
     * @return
     */
    public List<ID> findIdsByCodes(Collection<String> codes) {
        return baseDAO.selectIdsByCodes(codes);
    }

    /**
     * 根据id获取code
     *
     * @param id
     * @return
     */
    public Optional<String> findCodeById(Long id) {
        return baseDAO.selectCodeById(id);
    }

    /**
     * 根据code获取description
     *
     * @param code
     * @return
     */
    public Optional<String> findDescriptionByCode(String code) {
        return baseDAO.selectDescriptionByCode(code);
    }

    /**
     * 根据code获取单列值
     *
     * @param code
     * @param columnName
     * @param clazz
     * @return
     */
    public <S> Optional<S> findSingleValueByCode(String code, String columnName, Class<S> clazz) {
        return baseDAO.selectSingleValueByCode(code, columnName, clazz);
    }

    /**
     * key: code
     * value: id
     *
     * @param codes
     * @return
     */
    public Map<String, ID> findCodeIdMap(Collection<String> codes) {
        return baseDAO.selectCodeIdMap(codes);
    }

    /**
     * key: code
     * value: 实体
     *
     * @param codes
     * @return
     */
    public Map<String, T> findByCodesAsMap(Collection<String> codes) {
        return baseDAO.selectByCodesAsMap(codes);
    }

    /**
     * 查询不存在的code
     *
     * @param codes
     * @return
     */
    public Set<String> findNotExistsCodes(Collection<String> codes) {
        return baseDAO.selectNotExistsCodes(codes);
    }

    /**
     * 断言code不存在，存在抛异常
     *
     * @param code
     */
    public void assertCodeNotExists(String code) {
        baseDAO.assertCodeNotExists(code);
    }

    /**
     * 断言code存在，不存在抛异常
     *
     * @param code
     */
    public void assertCodeExists(String code) {
        baseDAO.assertCodeExists(code);
    }

    /**
     * 断言code存在（带附加条件），不存在抛异常
     *
     * @param code
     * @param conditionParams
     * @param condition
     */
    public void assertCodeExists(String code, Map<String, Object> conditionParams, String condition) {
        baseDAO.assertCodeExists(code, conditionParams, condition);
    }

    /**
     * 断言codes全部存在，不存在抛异常
     *
     * @param codes
     */
    public void assertCodesExists(Collection<String> codes) {
        baseDAO.assertCodesExists(codes);
    }

    /**
     * 断言codes全部存在（带附加条件），不存在抛异常
     *
     * @param codes
     * @param conditionParams
     * @param condition
     */
    public void assertCodesExists(Collection<String> codes, Map<String, Object> conditionParams, String condition) {
        baseDAO.assertCodesExists(codes, conditionParams, condition);
    }

    /**
     * 断言codes全部存在且不重复
     *
     * @param codes
     */
    public void assertCodesExistsAndUnDuplicate(List<String> codes) {
        baseDAO.assertCodesExistsAndUnDuplicate(codes);
    }

    /**
     * 断言codes不重复
     *
     * @param codes
     */
    public void assertCodesUnDuplicate(List<String> codes) {
        baseDAO.assertCodesUnDuplicate(codes);
    }

}
