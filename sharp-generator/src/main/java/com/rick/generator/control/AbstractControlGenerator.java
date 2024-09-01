package com.rick.generator.control;

import com.rick.formflow.form.cpn.core.CpnTypeEnum;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;

import static com.rick.generator.Generator.ADDITIONAL_DICT_CATEGORY;
import static com.rick.generator.Generator.ADDITIONAL_DICT_FIELD;

/**
 * @author Rick.Xu
 * @date 2024/8/25 19:55
 */
public abstract class AbstractControlGenerator {

    /**
     * 包含label
     *
     * @param formLayout
     * @param cpnType
     * @param entityName
     * @param name
     * @param label
     * @return
     */
    public final String generate(FormLayoutEnum formLayout, CpnTypeEnum cpnType, String entityName, String name, String label, String dictType, Map<String, Object> additionalInfo) {
        StringBuilder htmlBuilder = new StringBuilder();
        String labelForHtml = (cpnType == CpnTypeEnum.CHECKBOX || cpnType == CpnTypeEnum.RADIO) ? "" : "for=\"" + name + "\"";
        String labelHtml = "";
        if (formLayout == FormLayoutEnum.INLINE) {
            labelHtml = "<label class=\"col-form-label required\"" + labelForHtml + ">" + label + "</label>";
        } else if (formLayout == FormLayoutEnum.HORIZONTAL) {
            labelHtml = "<label class=\"col-1 col-form-label required\"" + labelForHtml + ">" + label + "</label>";
        }

        if (cpnType == CpnTypeEnum.DATE) {
            formGroupWrap(htmlBuilder, formLayout, cpnType, labelHtml + horizontalFormWrap(formLayout, cpnType, "<input type=\"text\" class=\"form-control\" id=\""+name+"\" name=\""+name+"\" th:value=\"${"+entityName+"."+name+"}\" required>"));
        } else if (cpnType == CpnTypeEnum.MULTIPLE_SELECT) {
            String embeddedClassPathName = getEmbeddedClassPathName(name, additionalInfo);
            formGroupWrap(htmlBuilder, formLayout, cpnType, labelHtml + horizontalFormWrap(formLayout, cpnType, "<sp:select class=\"form-control\" id=\"" + name + "\" name=\"" + name + "\" key=\"" + dictType + "\" th:value=\"${" + entityName + "." + StringUtils.substringBefore(embeddedClassPathName, ".") + " ne null ? " + entityName + "." + embeddedClassPathName + " : ''}\" hideDummyItemText required/>"));
        } else if (cpnType == CpnTypeEnum.SEARCH_SELECT) {
            String embeddedClassPathName = getEmbeddedClassPathName(name, additionalInfo);
            formGroupWrap(htmlBuilder, formLayout, cpnType, labelHtml + horizontalFormWrap(formLayout, cpnType, "<sp:select class=\"form-control\" id=\"" + name + "\" name=\"" + name + "\" key=\"" + dictType + "\" th:value=\"${" + entityName + "." + StringUtils.substringBefore(embeddedClassPathName, ".") + " ne null ? " + entityName + "." + embeddedClassPathName + " : ''}\" required/>"));
        } else {
            // 子类实现
            formGroupWrap(htmlBuilder, formLayout, cpnType, labelHtml + horizontalFormWrap(formLayout, cpnType, generate(formLayout, cpnType, entityName, name, dictType, additionalInfo)));
        }

        if (cpnType == CpnTypeEnum.DATE) {
            htmlBuilder
                    .append("<!-- <link rel=\"stylesheet\" th:href=\"@{/plugins/bootstrap-datepicker/css/bootstrap-datepicker.min.css}\"> -->")
                    .append("<!-- <script th:src=\"@{/plugins/bootstrap-datepicker/js/bootstrap-datepicker.min.js}\"></script> -->")
                    .append("<!--\n" +
                            // mounted 执行
                            "                $('#" + name + "').datepicker({\n" +
                            "                    language: \"zh-CN\",\n" +
                            "                    autoclose: true,\n" +
                            "                    clearBtn: true,\n" +
                            "                    todayBtn: 'linked',\n" +
                            "                    todayHighlight: true,\n" +
                            "                    format: 'yyyy-mm-dd'\n" +
                            "                })           \n" +
                            "            -->");
        } else if (cpnType == CpnTypeEnum.MULTIPLE_SELECT) {
            htmlBuilder
                    .append("<!-- <link rel=\"stylesheet\" th:href=\"@{/plugins/multiple-select/multiple-select.min.css}\"> -->")
                    .append("<!-- <script th:src=\"@{/plugins/multiple-select/multiple-select.min.js}\"></script> -->")
                    .append("<!--// vue 需要在 mounted 执行\n" +
                            "               $('#" + name + "').multipleSelect({\n" +
                            "                    filter: true,\n" +
                            "                    selectAll: true,\n" +
                            "                    single: false,\n" +
                            "                    placeholder: '选择"+label+"'\n" +
                            "                }).multipleSelect(\"setSelects\", [[${new com.rick.formflow.form.cpn.CheckBox().parseValue("+entityName+"."+name+")}]] || [])\n" +
                            "            -->\n" +
                            "            <!--// 多选需要通过 multipleSelect 获取数据\n" +
                            "               formData.materialTypeList = $('#materialTypeList').multipleSelect('getSelects') \n" +
                            "            --> ");
        }  else if (cpnType == CpnTypeEnum.SEARCH_SELECT) {
            htmlBuilder
                    .append("<!-- <link rel=\"stylesheet\" th:href=\"@{/plugins/multiple-select/multiple-select.min.css}\"> -->")
                    .append("<!-- <script th:src=\"@{/plugins/multiple-select/multiple-select.min.js}\"></script> -->")
                    .append("<!--// vue 需要在 mounted 执行\n" +
                            "               $('#" + name + "').multipleSelect({\n" +
                            "                    filter: true,\n" +
                            "                    single: true,\n" +
                            "                    placeholder: '选择"+label+"'\n" +
                            "                })\n" +
                            "            -->");
        }
        return htmlBuilder.toString();
    }

    protected String getEmbeddedClassPathName(String name, Map<String, Object> additionalInfo) {
        DictCategoryEnum dictCategoryEnum = (DictCategoryEnum) additionalInfo.get(ADDITIONAL_DICT_CATEGORY);
        String embeddedClassPathName = name;
        if (!Collection.class.isAssignableFrom(((Field) additionalInfo.get(ADDITIONAL_DICT_FIELD)).getType())) {
            if (dictCategoryEnum == DictCategoryEnum.DICT_VALUE) {
                embeddedClassPathName = com.rick.common.util.StringUtils.camelToDot(name);
            } else if (dictCategoryEnum == DictCategoryEnum.ENUM) {
                embeddedClassPathName = name + ".name";
            }
        }
        return embeddedClassPathName;
    }

    private String horizontalFormWrap(FormLayoutEnum formLayout, CpnTypeEnum cpnType, String controlHtml) {
        String colFormLabel = (CpnTypeEnum.CHECKBOX == cpnType || CpnTypeEnum.RADIO == cpnType || CpnTypeEnum.SELECT == cpnType) ? " col-form-label" : "";
        return formLayout == FormLayoutEnum.HORIZONTAL ? "<div class=\"col-4" + colFormLabel + "\">" + controlHtml + "</div>" : controlHtml;
    }

    private void formGroupWrap(StringBuilder htmlBuilder, FormLayoutEnum formLayout, CpnTypeEnum cpnType, String innerHtml) {
        if (formLayout == FormLayoutEnum.INLINE) {
            if (cpnType == CpnTypeEnum.CHECKBOX || cpnType == CpnTypeEnum.RADIO) {
                htmlBuilder.append("<div class=\"form-group col-4 form-group-check-container\">" + innerHtml + "</div>");
            } else {
                htmlBuilder.append("<div class=\"form-group col-4\">" + innerHtml + "</div>");
            }
        } else if (formLayout == FormLayoutEnum.HORIZONTAL) {
            htmlBuilder.append("<div class=\"form-group row\">" + innerHtml + "</div>");
        }
    }

    protected abstract String generate(FormLayoutEnum formLayout, CpnTypeEnum cpnType, String entityName, String name, String dictType, Map<String, Object> additionalInfo);

    public abstract RenderTypeEnum renderType();
}
