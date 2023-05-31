package com.rick.meta.dict.service;


import com.rick.meta.dict.entity.Dict;

import java.util.List;
import java.util.Optional;

/**
 * @author Rick
 * @createdAt 2021-09-07 04:40:00
 */
public interface DictService {

    Optional<Dict> getDictByTypeAndName(String type, String name);

    List<Dict> getDictByType(String type);

    void rebuild(String type);
}
