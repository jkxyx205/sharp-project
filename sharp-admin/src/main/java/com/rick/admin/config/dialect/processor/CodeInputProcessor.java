package com.rick.admin.config.dialect.processor;

import com.rick.admin.common.util.ThymeleafRenderHelper;
import com.rick.admin.module.codeinput.service.CodeInputService;
import com.rick.common.util.HtmlTagUtils;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.AbstractElementTagProcessor;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.templatemode.TemplateMode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Rick.Xu
 * @date 2024/9/2 14:33
 */
public class CodeInputProcessor extends AbstractElementTagProcessor {
    /**
     * 标签名
     */
    private static final String TAG_NAME = "code-input";

    /**
     * 优先级
     */
    private static final int PRECEDENCE = 10000;

    private static final String PROP_HIDE_HEADER = "hide-header";

    private static final String PROP_ID = "id";

    private static final String PROP_KEY = "key";

    private final CodeInputService codeInputService;


    public CodeInputProcessor(String dialectPrefix, CodeInputService codeInputService) {
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

        this.codeInputService = codeInputService;
    }

    @Override
    protected void doProcess(ITemplateContext iTemplateContext, IProcessableElementTag iProcessableElementTag, IElementTagStructureHandler iElementTagStructureHandler) {
        Map<String, String> attrMap = iProcessableElementTag.getAttributeMap();
        String key = attrMap.get(PROP_KEY);
        String id = attrMap.get(PROP_ID);

        Map<String, Object> dataMap = codeInputService.codeSearchResult(key, null, null);
        List<Map> columnProperties = (List<Map>) dataMap.get("columnProperties");

        StringBuilder attrBuilder = new StringBuilder(" th:attr=\"");
        for (Map columnProperty : columnProperties) {
            String name = (String) columnProperty.get("name");

            // data-id=${d.id}, data-code=${d.code}, data-name=${d.name}
            attrBuilder.append("data-"+name+"=${d."+name+"}").append(",");
        }

        attrBuilder = attrBuilder.deleteCharAt(attrBuilder.length() - 1);
        attrBuilder.append("\"");

        // language=HTML
        String template = "<div id=\""+id+"\">\n" +
                "    <div>\n" +
                "        <input class=\"form-control code-input\" type=\"text\" autocomplete=\"off\">\n" +
                "    </div>\n" +
                "    <div class=\"code-input-table\">\n" +
                "        <table class='table table-responsive-sm' style='margin-bottom: 0'>\n" +
                "            <thead class=\"code-input-table-fixed-head\" th:if=\"${!hideHeader}\">\n" +
                "            <tr>\n" +
                "                <th th:each=\"prop : ${columnProperties}\" th:if=\"${prop.name != 'id'}\" th:text=\"${prop.label}\" th:data-name=\"${prop.name}\"></th>\n" +
                "            </tr>\n" +
                "            </thead>\n" +
                "            <tbody>\n" +
                "            <tr th:each=\"d : ${data}\""+attrBuilder+">\n" +
                "                <td th:each=\"p: ${d}\" th:if=\"${p.key != 'id'}\" th:text=\"${p.value}\"></td>\n" +
                "            </tr>\n" +
                "            </tbody>\n" +
                "        </table>\n" +
                "        <div class=\"code-input-table-empty\">暂无数据</div>\n" +
                "    </div>\n" +
                "</div>";

        Map<String, Object> params = new HashMap<>();
        params.put("hideHeader", HtmlTagUtils.isTagPropertyTrueAndPut(attrMap, PROP_HIDE_HEADER));
        params.putAll(dataMap);

        String htmlContent = ThymeleafRenderHelper.renderByHtmlContent(template, params);
        iElementTagStructureHandler.replaceWith(htmlContent, false);
    }

}
