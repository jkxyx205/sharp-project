package com.rick.admin.config.dialect.processor;

import com.rick.admin.common.util.ThymeleafRenderHelper;
import com.rick.common.util.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.AbstractElementTagProcessor;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.templatemode.TemplateMode;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 单图片
 * @author Rick.Xu
 * @date 2025/7/29 14:33
 */
public class ImageUploadProcessor extends AbstractElementTagProcessor {

    /**
     * 标签名
     */
    private static final String TAG_NAME = "image";

    /**
     * 优先级
     */
    private static final int PRECEDENCE = 10000;

    private static final String PROP_FORM_NAME = "form-name";

    private static final String PROP_NAME = "name";

    private static final String PROP_VALUE = "value";

    private static final String PROP_GROUP = "group";


    public ImageUploadProcessor(String dialectPrefix) {
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
    }

    @Override
    protected void doProcess(ITemplateContext iTemplateContext, IProcessableElementTag iProcessableElementTag, IElementTagStructureHandler iElementTagStructureHandler) {
        Map<String, String> attrMap = iProcessableElementTag.getAttributeMap();
        String name = attrMap.get(PROP_NAME);
        String formName = StringUtils.defaultString(attrMap.get(PROP_FORM_NAME), "form");
        String group = StringUtils.defaultString(attrMap.get(PROP_GROUP), "upload");

        // language=HTML
        String template = "<div class=\"avatar-container\">\n" +
                "    <input type=\"text\" style=\"display:none;\" id=\""+name+"\" name=\""+name+"\" th:value=\"${T(com.rick.common.util.JsonUtils).toJson(value)}\">\n" +
                "    <input style=\"display: none;\" type=\"file\" id=\""+name+"_file\" name=\""+name+"_file\" accept='image/*' data-group-name='"+group+"' onchange=\""+formName+"_"+name+"_file.ajaxFileUpload()\">\n" +
                "    <div class=\"attachment-items\">\n" +
                "        <div class=\"item\" style=\"display: inline-block\" th:with=\"avatar = ${value['url'] eq null ? '/img/default_avatar.png' : value.url}\">\n" +
                "            <div style=\"position: relative; width: 200px;  max-width: 100%; max-height: 100%; display: flex; align-items: center; justify-content: center; user-select: none; opacity: 1;\">\n" +
                "                <a class=\"delete-btn\" style=\"display: none; position: absolute; right: 0; top:0; border-radius: 50%; border: 1px solid #20a8d8; width: 20px; height: 20px; text-align: center; line-height: 15px;\" href=\"javascript:;\" onclick=\""+formName+"_"+name + "_file.deleteAttachment(document.getElementById('avatar').getAttribute('value'), this)\">x</a>\n" +
                "                <img id=\"avatar\" th:src=\"${avatar}\"\n" +
                "                     style=\"display: block; object-fit: cover; width: 100%;cursor: pointer\"\n" +
                "                     onclick=\"document.getElementById('"+name+"_file').click()\"\n" +
                "                     alt=\"avatar\" th:value=\"${value['id']}\">\n" +
                "            </div>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "</div>";

        String value = attrMap.get(PROP_VALUE);
        if (StringUtils.isBlank(value)) {
            value = "{}";
        }

        Map<String, Object> params = new HashMap<>();
        params.put(PROP_VALUE, Optional.ofNullable(value).map(v -> JsonUtils.toObject(v, Map.class)).orElse(null));
        String htmlContent = ThymeleafRenderHelper.renderByHtmlContent(template, params);
        iElementTagStructureHandler.replaceWith(htmlContent, false);
    }

}
