package com.rick.admin.config.dialect.processor;

import com.rick.admin.config.dialect.SelectOptionHelper;
import com.rick.db.service.SharpService;
import com.rick.meta.dict.service.DictService;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IModel;
import org.thymeleaf.model.IModelFactory;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.AbstractElementTagProcessor;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.templatemode.TemplateMode;

/**
 * @author Rick.Xu
 * @date 2023/5/29 13:45
 */
public class SelectOptionProcessor extends AbstractElementTagProcessor {

    /**
     * 标签名
     */
    private static final String TAG_NAME = "option";

    /**
     * 优先级
     */
    private static final int PRECEDENCE = 10000;

    private final DictService dictService;

    private final SharpService sharpService;

    public SelectOptionProcessor(String dialectPrefix, DictService dictService, SharpService sharpService) {
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
        this.sharpService = sharpService;
    }

    @Override
    protected void doProcess(ITemplateContext iTemplateContext, IProcessableElementTag iProcessableElementTag, IElementTagStructureHandler iElementTagStructureHandler) {
        IModelFactory modelFactory = iTemplateContext.getModelFactory();
        IModel model = modelFactory.createModel();
        new SelectOptionHelper(dictService, sharpService)
                .appendOptions(modelFactory, model, iProcessableElementTag, iElementTagStructureHandler);
    }



}
