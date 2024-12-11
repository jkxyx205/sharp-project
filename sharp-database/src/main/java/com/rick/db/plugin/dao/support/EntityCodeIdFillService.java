package com.rick.db.plugin.dao.support;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.rick.common.http.exception.BizException;
import com.rick.db.dto.BaseCodeEntity;
import com.rick.db.plugin.dao.annotation.Table;
import com.rick.db.plugin.dao.core.EntityCodeDAO;
import com.rick.db.plugin.dao.core.EntityCodeDAOImpl;
import com.rick.db.plugin.dao.core.EntityDAOManager;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Rick
 * @createdAt 2023-03-08 22:17:00
 */
public class EntityCodeIdFillService {

    public <T extends BaseCodeEntity> void fill(T t) {
        if (t == null || t.getId() != null || StringUtils.isBlank(t.getCode())) {
            return;
        }

        t.setId(fill(t.getClass(), t.getId(), t.getCode()));
    }

    public <T extends BaseCodeEntity> void fill(Collection<T> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }

        Class<T> clazz = (Class<T>) list.iterator().next().getClass();
        Map<String, Long> codeIdMap = getCodeIdMap(clazz, list.stream().map(BaseCodeEntity::getCode).collect(Collectors.toSet()));
        list.forEach(t -> t.setId(codeIdMap.get(t.getCode())));
    }

    public <T extends BaseCodeEntity> List<T> fill(Class<T> clazz, Collection<String> codes) {
        Map<String, Long> codeIdMap = getCodeIdMap(clazz, codes);
        List<T> list = Lists.newArrayListWithExpectedSize(codes.size());

        for (String code : codes) {
            try {
                T t = clazz.newInstance();
                t.setId(codeIdMap.get(code));
                list.add(t);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return list;
    }

    public <T extends BaseCodeEntity> T fill(Class<T> clazz, String code) {
        try {
            return fill(clazz, clazz.newInstance(), code);
        } catch (Exception e) {
            return null;
        }
    }

    public <T extends BaseCodeEntity> void fill(T t, String code) {
        if (t == null || StringUtils.isBlank(code)) {
            return;
        }

        fill((Class<T>) t.getClass(), t, code);
    }

    public <T extends BaseCodeEntity> T fill(Class<T> clazz, T t, String code) {
        if (StringUtils.isBlank(code)) {
            return t;
        }

        if (t == null) {
            try {
                t = clazz.newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        if (t.getId() != null) {
            return t;
        }

        t.setCode(code);
        t.setId(fill(clazz, t.getId(), t.getCode()));
        return t;
    }

    public <T extends BaseCodeEntity, ID> ID fill(Class<T> clazz, ID id, String code) {
        if (id != null || StringUtils.isBlank(code)) {
            return id;
        }

        return (ID) ((EntityCodeDAO) EntityDAOManager.getEntityDAO(clazz)).selectIdByCodeOrThrowException(code);
    }

    public <T extends BaseCodeEntity> Map<String, Long> getCodeIdMap(Class<T> clazz, Collection<String> codes) {
        if (CollectionUtils.isEmpty(codes)) {
            return Collections.emptyMap();
        }

        Map<String, Long> codeIdMap = ((EntityCodeDAOImpl) EntityDAOManager.getEntityDAO(clazz)).selectCodeIdMap(codes);

        SetUtils.SetView<String> difference = SetUtils.difference(Sets.newHashSet(codes), codeIdMap.keySet());
        if (CollectionUtils.isNotEmpty(difference)) {
            throw new BizException("%s codes %s 不存在", new Object[]{clazz.getAnnotation(Table.class).comment(), difference});
        }

        return codeIdMap;
    }

}
