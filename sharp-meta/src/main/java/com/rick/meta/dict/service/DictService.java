package com.rick.meta.dict.service;

import com.rick.meta.dict.dao.dataobject.DictDO;

import java.util.List;
import java.util.Optional;

/**
 * @author Rick
 * @createdAt 2021-09-07 04:40:00
 */
public interface DictService {

    Optional<DictDO> getDictByTypeAndName(String type, String name);

    List<DictDO> getDictByType(String type);

    void rebuild(String type);
}
