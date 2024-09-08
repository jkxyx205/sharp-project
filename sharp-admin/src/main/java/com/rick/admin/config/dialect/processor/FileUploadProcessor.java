package com.rick.admin.config.dialect.processor;

import com.rick.admin.common.util.ThymeleafRenderHelper;
import com.rick.common.util.JsonUtils;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.AbstractElementTagProcessor;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.templatemode.TemplateMode;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author Rick.Xu
 * @date 2024/9/2 14:33
 */
public class FileUploadProcessor extends AbstractElementTagProcessor {

    /**
     * 标签名
     */
    private static final String TAG_NAME = "file";

    /**
     * 优先级
     */
    private static final int PRECEDENCE = 10000;

    private static final String PROP_FORM_NAME = "form-name";

    private static final String PROP_NAME = "name";

    private static final String PROP_VALUE = "value";

    public FileUploadProcessor(String dialectPrefix) {
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
        String formName = attrMap.get(PROP_FORM_NAME);

        String template = "<div class=\"attachment\">\n" +
                "                                <div style=\"display: inline-block;\" id=\"btn-file\">\n" +
                "                                    <label class=\"btn btn-primary btn-sm btn-upload\" style=\"margin: 2px\" for=\""+name+"_file\"><i class=\"fa fa-upload\"></i> 上传</label>\n" +
                "                                </div>\n" +
                "                                <input style=\"display: none;\" type=\"text\" id=\""+name+"\" name=\""+name+"\" th:value=\"${value ne null ? T(com.rick.common.util.JsonUtils).toJson(value) : '[]'}\">\n" +
                "                                <input style=\"display: none;\" type=\"file\" id=\""+name+"_file\" name=\""+name+"_file\" multiple data-group-name=\""+name+"\" onchange=\""+formName+"_"+name+"_file.ajaxFileUpload()\">\n" +
                "                                <div class=\"attachment-items\">\n" +
                "                                    <th:block th:if=\"${value ne null}\">\n" +
                "                                        <div class=\"item\" th:each=\"f : ${value}\">\n" +
                "                                            <a th:text=\"${f.fullName}\" th:href=\"${f.url}\" target=\"_blank\"></a>\n" +
                "                                            <button type=\"button\" class=\"btn btn-link attachment_delete_btn\" th:onclick=\""+formName+"_"+name+"_file.deleteAttachment([[${f.id}]], this)\">删除</button>\n" +
                "                                        </div>\n" +
                "                                    </th:block>\n" +
                "                                </div>\n" +
                "                            </div>";

        String value = attrMap.get(PROP_VALUE);
        Map<String, Object> params = new HashMap<>();
        params.put(PROP_VALUE, Optional.ofNullable(value).map(v -> JsonUtils.toList(value, Map.class)).orElse(null));
        String htmlContent = ThymeleafRenderHelper.renderByHtmlContent(template, params);
        iElementTagStructureHandler.replaceWith(htmlContent, false);
    }

}
