package com.rick.meta.dict.service;


import com.rick.meta.dict.entity.Dict;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author Rick
 * @createdAt 2021-09-07 04:40:00
 */
public interface DictService {

    Map<String, List<Dict>> getDictsByCodes(Collection<String> codes);

    Map<String, List<Dict>> getDictsByCodes(String... codes);

    Optional<Dict> getDictByTypeAndName(String type, String name);

    List<Dict> getDictByType(String type);

    void rebuild(String type);

    void rebuild();
}
