package com.rick.generator.control;

import com.rick.formflow.form.cpn.core.CpnTypeEnum;

/**
 * @author Rick.Xu
 * @date 2024/8/25 19:55
 */
public abstract class AbstractControlGenerator {

//    public final String generate(CpnTypeEnum cpnType, String entityName, String name, String label) {
//        return generate(cpnType, entityName, name, label, null);
//    }

    /**
     * 包含label
     * @param cpnType
     * @param entityName
     * @param name
     * @param label
     * @return
     */
    public final String generate(CpnTypeEnum cpnType, String entityName, String name, String label, String dictType) {
        StringBuilder htmlBuilder = new StringBuilder();
        String labelForHtml = (cpnType == CpnTypeEnum.CHECKBOX || cpnType == CpnTypeEnum.RADIO) ? "" :  "for=\""+name+"\"";
        String labelHtml = "<label class=\"col-form-label required\""+labelForHtml+">"+label+"</label>";

        if (cpnType == CpnTypeEnum.DATE) {
            formGroupWrap(htmlBuilder, labelHtml + "<input type=\"text\" class=\"form-control\" id=\""+name+"\" name=\""+name+"\" required>");
        } else if (cpnType == CpnTypeEnum.SELECT) {
            formGroupWrap(htmlBuilder, labelHtml + "<sp:select id=\""+name+"\" name=\""+name+"\" key=\""+dictType+"\" class=\"form-control\" hideAllItem />");
        } else {
            // 子类实现
            formGroupWrap(htmlBuilder, labelHtml + generate(cpnType, entityName, name, dictType));
        }


        if (cpnType == CpnTypeEnum.DATE) {
            htmlBuilder
                    .append("<!-- <link rel=\"stylesheet\" th:href=\"@{/plugins/bootstrap-datepicker/css/bootstrap-datepicker.min.css}\"> -->")
                    .append("<!-- <script th:src=\"@{/plugins/bootstrap-datepicker/js/bootstrap-datepicker.min.js}\"></script> -->")
                    .append("<!--\n" +
                    // mounted 执行
                    "                $('#"+name+"').datepicker({\n" +
                    "                    language: \"zh-CN\",\n" +
                    "                    autoclose: true,\n" +
                    "                    clearBtn: true,\n" +
                    "                    todayBtn: 'linked',\n" +
                    "                    todayHighlight: true,\n" +
                    "                    format: 'yyyy-mm-dd'\n" +
                    "                })           \n" +
                    "            -->");
        } else if (cpnType == CpnTypeEnum.SELECT) {
            htmlBuilder
                    .append("<!-- <link rel=\"stylesheet\" th:href=\"@{/plugins/multiple-select/multiple-select.min.css}\"> -->")
                    .append("<!-- <script th:src=\"@{/plugins/multiple-select/multiple-select.min.js}\"></script> -->")
                    .append("<!--\n" +
                    // mounted 执行
                    "               $('#"+name+"').multipleSelect({\n" +
                    "                    filter: true,\n" +
                    "                    selectAll: true,\n" +
                    "                    single: false,\n" +
                    "                    placeholder: '选择分类'\n" +
                    "                })\n" +
                    "        -->");
        }
        return htmlBuilder.toString();
    }

    private void formGroupWrap(StringBuilder htmlBuilder, String innerHtml) {
        htmlBuilder.append("<div class=\"form-group col-4\">"+innerHtml+"</div>");
    }

    public abstract String generate(CpnTypeEnum cpnType, String entityName, String name, String dictType);

    public abstract RenderTypeEnum renderType();
}
