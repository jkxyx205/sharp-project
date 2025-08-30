package com.rick.admin.common.api;

import com.rick.admin.common.exception.ExceptionCodeEnum;
import com.rick.admin.common.exception.ResourceNotFoundException;
import com.rick.db.plugin.BaseServiceImpl;
import com.rick.db.repository.EntityCodeDAO;
import com.rick.db.repository.model.BaseCodeEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author Rick.Xu
 * @date 2023/6/14 00:12
 */
public class BaseCodeApi<S extends BaseServiceImpl<? extends EntityCodeDAO<T, ID>, T, ID>, T extends BaseCodeEntity<ID>, ID> extends BaseApi<S, T, ID> {

    public BaseCodeApi(S baseService) {
        super(baseService);
    }

    @GetMapping("code/{code}")
    public T findByCode(@PathVariable String code) {
        return (T) getEntityFromOptional(((EntityCodeDAO) entityDAO).selectByCode(code), code);
    }

    @Override
    protected ResourceNotFoundException getResourceNotFoundException(Object key) {
        if (key.getClass() == String.class) {
            return new ResourceNotFoundException(ExceptionCodeEnum.CODE_NOT_EXISTS_ERROR,
                    new Object[]{comment(), key});
        }
        return super.getResourceNotFoundException(key);
    }

}
