package com.rick.admin.common.api;

import com.rick.admin.common.exception.ExceptionCodeEnum;
import com.rick.admin.common.exception.ResourceNotFoundException;
import com.rick.db.dto.BaseCodeEntity;
import com.rick.db.plugin.dao.core.EntityCodeDAOImpl;
import com.rick.db.service.BaseServiceImpl;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author Rick.Xu
 * @date 2023/6/14 00:12
 */
public class BaseCodeApi<T extends BaseCodeEntity> extends BaseApi {

    public BaseCodeApi(BaseServiceImpl baseService) {
        super(baseService);
    }

    @GetMapping("code/{code}")
    public T findByCode(@PathVariable String code) {
        return (T) getEntityFromOptional(((EntityCodeDAOImpl) entityDAO).selectByCode(code), code);
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
