package com.rick.admin.config.dialect;

import com.google.common.collect.Lists;
import com.rick.db.service.SharpService;
import com.rick.meta.dict.entity.Dict;
import com.rick.meta.dict.service.DictService;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.thymeleaf.model.IModel;
import org.thymeleaf.model.IModelFactory;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.IElementTagStructureHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Rick.Xu
 * @date 2023/8/7 21:05
 */
@AllArgsConstructor
public class SelectOptionHelper {

    private DictService dictService;

    private SharpService sharpService;

    private static final Map<String, List<Map<String, Object>>> groupDataMap = new HashMap<>();

    public void appendOptions(IModelFactory modelFactory, IModel model, IProcessableElementTag iProcessableElementTag, IElementTagStructureHandler iElementTagStructureHandler) {
        //  获取前端页面传递的属性
        String key = iProcessableElementTag.getAttributeValue("key");
        String selected = iProcessableElementTag.getAttributeValue("value");
        String excludeValues = iProcessableElementTag.getAttributeValue("exclude");

        // emptyItemText => dummyItemText
        String dummyItemText = iProcessableElementTag.hasAttribute("dummyItemText") ? StringUtils.defaultString(iProcessableElementTag.getAttributeValue("dummyItemText"), "") : "&nbsp;";
        // hideAllItem => hideDummyItemText
        if (!iProcessableElementTag.hasAttribute("hideDummyItemText")) {
            model.add(modelFactory.createOpenElementTag("option value=\"\""));
            model.add(modelFactory.createText(dummyItemText));
            model.add(modelFactory.createCloseElementTag("option"));
        } else {
            model.add(modelFactory.createOpenElementTag("option value=\"\" selected disabled hidden"));
            model.add(modelFactory.createText(dummyItemText));
            model.add(modelFactory.createCloseElementTag("option"));
        }

        if (iProcessableElementTag.hasAttribute("group")) {
            initGroupOptions(modelFactory, model, key, excludeValues, selected);
        } else {
            initOptions(modelFactory, model, key, excludeValues, selected, iProcessableElementTag);
        }

        iElementTagStructureHandler.replaceWith(model, false);
    }

    private void initOptions(IModelFactory modelFactory, IModel model, String key, String excludeValues, String selected, IProcessableElementTag iProcessableElementTag) {
        // 进行数据的查询 根据 type 查询
        List<Dict> dictList = dictService.getDictByType(key);
        if (StringUtils.isNotBlank(excludeValues)) {
            List<String> excludeValueArr = Lists.newArrayList(excludeValues.split(","));
            dictList = dictList.stream().filter(dict -> !excludeValueArr.contains(dict.getName())).collect(Collectors.toList());
        }


        for (Dict dict : dictList) {
            // TODO 权限控制
//            if (iProcessableElementTag.hasAttribute("auth") && !UserContextHolder.get().getAuthorityList().contains(dict.getName())){
//                continue;
//            }

            model.add(modelFactory.createOpenElementTag(String.format("option value='%s'%s", dict.getName(),(Objects.equals(dict.getName(), selected) ? " selected" : ""))));
            model.add(modelFactory.createText(dict.getLabel()));
            model.add(modelFactory.createCloseElementTag("option"));
        }
    }

    private void initGroupOptions(IModelFactory modelFactory, IModel model, String key, String excludeValues, String selected) {
        List<Map<String, Object>> valueList = groupDataMap.get(key);
        if (valueList == null) {
            // 进行数据的查询 根据 key 查询
            String querySql =  dictService.getDictPropertyItemByType(key).getSql(); //PropertyUtils.getProperty(key);
            valueList = sharpService.query(querySql, null);
            groupDataMap.put(key, valueList);
        }

        if (StringUtils.isNotBlank(excludeValues)) {
            List<String> excludeValueArr = Lists.newArrayList(excludeValues.split(","));
            valueList = valueList.stream().filter(kv -> !excludeValueArr.contains(kv.get("name"))).collect(Collectors.toList());
        }

        // 分组
        Map<String, List<Map<String, Object>>> groupNameMap = valueList.stream().collect(Collectors.groupingBy(a -> StringUtils.defaultString((String)a.get("parent_name"), "其他")));
        for (Map.Entry<String, List<Map<String, Object>>> entry: groupNameMap.entrySet()) {
            IModel optgroup = modelFactory.createModel();
            optgroup.add(modelFactory.createOpenElementTag("optgroup label=\""+entry.getKey()+"\""));

            for (Map<String, Object> option : entry.getValue()) {
                optgroup.add(modelFactory.createOpenElementTag(String.format("option value='%s'%s", option.get("id"),(Objects.equals(String.valueOf(option.get("id")), selected) ? " selected" : ""))));
                optgroup.add(modelFactory.createText((CharSequence) option.get("name")));
                optgroup.add(modelFactory.createCloseElementTag("option"));
            }

            optgroup.add(modelFactory.createCloseElementTag("optgroup"));
            model.addModel(optgroup);
        }
    }
}
