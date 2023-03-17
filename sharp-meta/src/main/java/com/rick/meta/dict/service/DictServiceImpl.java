package com.rick.meta.dict.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.rick.db.service.SharpService;
import com.rick.meta.dict.dao.dataobject.DictDO;
import com.rick.meta.dict.model.DictProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import java.util.*;
import java.util.stream.Collectors;


/**
 * @author Rick
 * @createdAt 2021-09-06 17:45:00
 */
@RequiredArgsConstructor
@Slf4j
public class DictServiceImpl implements DictService, InitializingBean {

    private final SharpService sharpService;

    private final DictProperties dictProperties;

    @Autowired(required = false)
    private DictDOSupplier dictDOSupplier;

    private static final String SELECT_SQL = "SELECT type, name, label, sort FROM sys_dict WHERE type = :type";

    @Override
    public Optional<DictDO> getDictByTypeAndName(String type, String name) {
        Assert.hasText(type, "type cannot be empty");
        Assert.hasText(name, "name cannot be empty");
        return DictUtils.getDictLabel(type, name);
    }

    @Override
    public List<DictDO> getDictByType(String type) {
        Assert.hasText(type, "type cannot be empty");
        return DictUtils.getDict(type);
    }

    @Override
    public void rebuild(String type) {
        // sys_dict
        Map<String, Object> params = Maps.newLinkedHashMapWithExpectedSize(1);
        params.put("type", type);
        List<DictDO> list = getDbDictList(params);
        if (CollectionUtils.isNotEmpty(list)) {
            DictUtils.dictMap.put(type, list);
            return;
        }

        // yml 不需要 rebuild
//        for (DictProperties.Item item : dictProperties.getItems()) {
//            if (item.getType().equals(type)) {
//                initYml(item);
//            }
//        }
    }

    @Override
    public void afterPropertiesSet() {
        // sys_dict
        List<DictDO> list = getDbDictList(null);
        if (dictDOSupplier != null) {
            list.addAll(dictDOSupplier.get());
        }

        Map<String, List<DictDO>> map = list.stream().collect(Collectors.groupingBy(DictDO::getType));
        DictUtils.dictMap = Maps.newHashMapWithExpectedSize(dictProperties.getItems().size() + map.size());

        for (Map.Entry<String, List<DictDO>> en : map.entrySet()) {
            DictUtils.dictMap.put(en.getKey(), en.getValue().stream().sorted(Comparator.comparing(DictDO::getSort)).collect(Collectors.toList()));
        }

        // yml
        for (DictProperties.Item item : dictProperties.getItems()) {
            initYml(item);
        }
    }

    private List<DictDO> getDbDictList(Map<String, Object> params) {
        try {
            List<DictDO> list = sharpService.query(SELECT_SQL, params, DictDO.class);
            return list;
        } catch (Exception e) {
            log.warn("sys_dict表没有创建成功！");
            return Collections.emptyList();
        }
    }

    private void initYml(DictProperties.Item item) {
        if (Objects.nonNull(item.getMap())) {
            initMap(item.getType(), item.getMap());
        } else if (Objects.nonNull(item.getSql())) {
            initSQL(item.getType(), item.getSql());
        } else if(Objects.nonNull(item.getList())) {
            initList(item.getType(), item.getList());
        }
    }

    private void initSQL(String type, String sql) {
        initMap(type, sharpService.queryForKeyValue(sql, null));
    }

    private void initMap(String type, Map<String, String> map) {
        List<DictDO> list = Lists.newArrayListWithExpectedSize(map.size());

        int i = 0;
        for (Map.Entry<String, String> en : map.entrySet()) {
            list.add(DictDO.builder().type(type)
                    .name(en.getKey())
                    .label(en.getValue())
                    .sort(i++).build());

        }
        DictUtils.dictMap.put(type, list);
    }

    private void initList(String type, List<String> strings) {
        List<DictDO> list = Lists.newArrayListWithExpectedSize(strings.size());
        int i = 0;
        for (String key : strings) {
            list.add(DictDO.builder().type(type)
                    .name(key)
                    .label(key)
                    .sort(i++).build());

        }
        DictUtils.dictMap.put(type, list);
    }
}