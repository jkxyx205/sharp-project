package com.rick.admin.config.dialect.processor;

import com.rick.meta.dict.service.DictService;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IModel;
import org.thymeleaf.model.IModelFactory;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.AbstractElementTagProcessor;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.templatemode.TemplateMode;

import java.util.Map;
import java.util.Objects;

/**
 * @author Rick.Xu
 * @date 2023/5/29 13:45
 */
public class SpanProcessor extends AbstractElementTagProcessor {

    /**
     * 标签名
     */
    private static final String TAG_NAME = "span";

    /**
     * 优先级
     */
    private static final int PRECEDENCE = 10000;

    private final DictService dictService;

    public SpanProcessor(String dialectPrefix, DictService dictService) {
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
        Map<String, String> attrMap = iProcessableElementTag.getAttributeMap();
        // 创建标签
        IModelFactory modelFactory = iTemplateContext.getModelFactory();
        IModel model = modelFactory.createModel();
        StringBuilder openElementString = new StringBuilder();
        openElementString.append("span");
        attrMap.forEach((key, value) -> {
            openElementString.append(" ").append(key);
            if (Objects.nonNull(value)) {
                openElementString.append("=\"").append(value).append("\"");
            }
        });

        model.add(modelFactory.createOpenElementTag(openElementString.toString()));
        model.add(modelFactory.createText(dictService.getDictByTypeAndName(attrMap.get("key"), attrMap.get("value")).get().getLabel()));
        model.add(modelFactory.createCloseElementTag("span"));
        iElementTagStructureHandler.replaceWith(model, false);
    }

}
