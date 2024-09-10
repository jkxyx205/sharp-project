package com.rick.admin.config.dialect.processor;

import com.rick.admin.common.util.ThymeleafRenderHelper;
import com.rick.common.util.HtmlTagUtils;
import com.rick.db.service.support.Params;
import com.rick.formflow.form.service.FormService;
import com.rick.formflow.form.service.bo.FormBO;
import org.apache.commons.lang3.StringUtils;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.AbstractElementTagProcessor;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.templatemode.TemplateMode;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Rick.Xu
 * @date 2024/9/2 14:33
 */
public class SharpFormProcessor extends AbstractElementTagProcessor {

    /**
     * 标签名
     */
    private static final String TAG_NAME = "form";

    /**
     * 优先级
     */
    private static final int PRECEDENCE = 10000;

    private final FormService formService;


    private static final String FORM_ID = "id";

    private static final String FORM_INSTANCE_ID = "value";

    private static final String FORM_PAGE = "form-page";

    private static final String FORM_READONLY = "readonly";

    private static final String FORM_SHOW_BTN = "show-btn";

    private static final String FORM_RELOAD_TAB = "reload-tab";

    private static final String FORM_HIDE_FORM_TAG = "hide-form-tag";

    public SharpFormProcessor(String dialectPrefix, FormService formService) {
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

        this.formService = formService;
    }

    @Override
    protected void doProcess(ITemplateContext iTemplateContext, IProcessableElementTag iProcessableElementTag, IElementTagStructureHandler iElementTagStructureHandler) {
        Map<String, String> attrMap = iProcessableElementTag.getAttributeMap();

        FormBO formBO = formService.getFormBO(Long.parseLong(attrMap.get(FORM_ID)), StringUtils.isBlank(attrMap.get(FORM_INSTANCE_ID)) ? null : Long.parseLong(attrMap.get(FORM_INSTANCE_ID)));

        formBO.getForm().getAdditionalInfo().put("showSaveFormBtn", HtmlTagUtils.isTagPropertyTrueAndPut(attrMap, FORM_SHOW_BTN));

        Map<String, Object> params = new HashMap<>();
        params.put("formBO", formBO);
        params.put("query", Params.builder(1)
                .pv(FORM_READONLY, HtmlTagUtils.isTagPropertyTrueAndPut(attrMap, FORM_READONLY))
                .build());
        params.put("reloadTab", attrMap.get(FORM_RELOAD_TAB));

        String htmlContent = ThymeleafRenderHelper.renderByTemplateName(attrMap.get(FORM_PAGE), params);
        if (HtmlTagUtils.isTagPropertyTrueAndPut(attrMap, FORM_HIDE_FORM_TAG)) {
            htmlContent = htmlContent.replace("<form", "<div").replace("</form", "</div");
        }

        iElementTagStructureHandler.replaceWith(htmlContent, false);
    }

}
