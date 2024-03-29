package com.rick.meta.dict.service;


import com.rick.meta.dict.entity.Dict;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * All rights Reserved, Designed By www.xhope.top
 *
 * @version V1.0
 * @Description: (用一句话描述该文件做什么)
 * @author: Rick.Xu
 * @date: 9/27/19 10:40 AM
 * @Copyright: 2019 www.yodean.com. All rights reserved.
 */
final class DictUtils {

    public static Map<String, List<Dict>> dictMap;

    public static List<Dict> getDict(String key) {
        return ListUtils.unmodifiableList(ListUtils.emptyIfNull(dictMap.get(key)));
    }

    public static Optional<Dict> getDictLabel(String key, String name) {
        List<Dict> dictList = getDict(key);
        if (CollectionUtils.isEmpty(dictList)) {
            return Optional.empty();
        }

        for (Dict dict : dictList) {
            if (Objects.equals(dict.getName(), name)) {
                return Optional.of(dict);
            }
        }

        return Optional.empty();
    }
}