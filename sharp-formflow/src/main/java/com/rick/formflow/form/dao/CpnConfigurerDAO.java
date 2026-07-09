package com.rick.formflow.form.dao;

import com.rick.db.repository.EntityDAOImpl;
import com.rick.formflow.form.cpn.core.CpnConfigurer;
import com.rick.meta.dict.entity.Dict;
import com.rick.meta.dict.service.DictService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * @author Rick
 * @createdAt 2021-11-03 11:22:00
 */

@Repository
public class CpnConfigurerDAO extends EntityDAOImpl<CpnConfigurer, Long> {

    @Resource
    private DictService dictService;

    @Override
    public List<CpnConfigurer> select(Map<String, Object> paramMap) {
        List<CpnConfigurer> list = super.select(paramMap);
        datasourceOptionsHandler(list);
        return list;
    }

    @Override
    public <E> List<E> select(Class<E> clazz, String columns, String condition, Map<String, Object> paramMap) {
        List<E> list = super.select(clazz, columns, condition, paramMap);
        datasourceOptionsHandler((List<CpnConfigurer>) list);
        return list;
    }

    private void datasourceOptionsHandler(List<CpnConfigurer> cpnConfigurerList) {
        if (CollectionUtils.isNotEmpty(cpnConfigurerList)) {
            for (CpnConfigurer cpnConfigurer : cpnConfigurerList) {
                if (StringUtils.isNotBlank(cpnConfigurer.getDatasource())) {
                    List<Dict> dictList = dictService.getDictByType(cpnConfigurer.getDatasource());
                    cpnConfigurer.setOptions(dictList.stream().map(dictDO -> new CpnConfigurer.CpnOption(dictDO.getName(), dictDO.getLabel())).collect(Collectors.toList()));
                }
            }
        }
    }

}