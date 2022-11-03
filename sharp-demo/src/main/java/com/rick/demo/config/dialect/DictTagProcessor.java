package com.rick.demo.config.dialect;

import com.google.common.collect.Lists;
import com.rick.meta.dict.dao.dataobject.DictDO;
import com.rick.meta.dict.service.DictService;
import org.apache.commons.lang3.StringUtils;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IModel;
import org.thymeleaf.model.IModelFactory;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.AbstractElementTagProcessor;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.templatemode.TemplateMode;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * All rights Reserved, Designed By www.xhope.top
 *
 * @version V1.0
 * @Description: (用一句话描述该文件做什么)
 * @author: Rick.Xu
 * @date: 9/27/19 11:44 AM
 * @Copyright: 2019 www.yodean.com. All rights reserved.
 */
public class DictTagProcessor extends AbstractElementTagProcessor {

    // 标签名
    private static final String TAG_NAME = "dict";

    // 优先级
    private static final int PRECEDENCE = 10000;

    private DictService dictService;

    public DictTagProcessor(String dialectPrefix, DictService dictService) {
        super(
                // 此处理器将仅应用于HTML模式
                TemplateMode.HTML,

                // 要应用于名称的匹配前缀
                dialectPrefix,

                // 标签名称：匹配此名称的特定标签 该内容就是在使用名称空间调用的 标签
                TAG_NAME,

                // 将标签前缀应用于标签名称
                true,

                // 无属性名称：将通过标签名称匹配
                null,

                // 没有要应用于属性名称的前缀
                false,

                // 优先(内部方言自己的优先)
                PRECEDENCE
        );
        this.dictService = dictService;
    }

    @Override
    protected void doProcess(ITemplateContext iTemplateContext, IProcessableElementTag iProcessableElementTag, IElementTagStructureHandler iElementTagStructureHandler) {
        //  获取前端页面传递的属性
        String key = iProcessableElementTag.getAttributeValue("key");
        String selected = iProcessableElementTag.getAttributeValue("value");
        String excludeValues = iProcessableElementTag.getAttributeValue("exclude");

        Map<String, String> attrMap = iProcessableElementTag.getAttributeMap();

        // 进行数据的查询 根据 type 查询
        List<DictDO> dictList = dictService.getDictByType(key);
        if (StringUtils.isNotBlank(excludeValues)) {
            List<String> excludeValueArr = Lists.newArrayList(excludeValues.split(","));
            dictList = dictList.stream().filter(dict -> !excludeValueArr.contains(dict.getName())).collect(Collectors.toList());
        }

        // 创建标签
        IModelFactory modelFactory = iTemplateContext.getModelFactory();
        IModel model = modelFactory.createModel();
        StringBuilder openElementString = new StringBuilder();
        openElementString.append("select");
        attrMap.entrySet().forEach(stringStringEntry -> {
            openElementString.append(" ").append(stringStringEntry.getKey());
            if (Objects.nonNull(stringStringEntry.getValue()))  {
                openElementString.append("=\"").append(stringStringEntry.getValue()).append("\"");
            }
        });

        model.add(modelFactory.createOpenElementTag(openElementString.toString()));

        if (!iProcessableElementTag.hasAttribute("hideAllItem")) {
            model.add(modelFactory.createOpenElementTag("option value=\"\"selected"));
            model.add(modelFactory.createText("全部"));
            model.add(modelFactory.createCloseElementTag("option"));
        }

        for (DictDO dict : dictList) {
            model.add(modelFactory.createOpenElementTag(String.format("option value='%s'%s", dict.getName(),(Objects.equals(dict.getName(), selected) ? " selected" : ""))));
            model.add(modelFactory.createText(dict.getLabel()));
            model.add(modelFactory.createCloseElementTag("option"));
        }
        model.add(modelFactory.createCloseElementTag("select"));

        iElementTagStructureHandler.replaceWith(model, false);

    }
}
