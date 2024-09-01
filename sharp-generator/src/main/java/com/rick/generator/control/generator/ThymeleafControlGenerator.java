package com.rick.generator.control.generator;

import com.rick.formflow.form.cpn.core.CpnTypeEnum;
import com.rick.generator.control.AbstractControlGenerator;
import com.rick.generator.control.FormLayoutEnum;
import com.rick.generator.control.RenderTypeEnum;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

import static com.rick.generator.Generator.ADDITIONAL_INFO_INPUT_PATTERN;

/**
 * @author Rick.Xu
 * @date 2024/8/25 20:07
 */
public class ThymeleafControlGenerator extends AbstractControlGenerator {

    @Override
    public RenderTypeEnum renderType() {
        return RenderTypeEnum.THYMELEAF;
    }

    @Override
    public String generate(FormLayoutEnum formLayout, CpnTypeEnum cpnType, String entityName, String name, String dictType, Map<String, Object> additionalInfo) {
        String embeddedClassPathName = getEmbeddedClassPathName(name, additionalInfo);
        String embeddedName  = StringUtils.substringBefore(embeddedClassPathName, ".");
        if (cpnType == CpnTypeEnum.TEXT) {
            String inputPattern = "";
            if (additionalInfo != null) {
                inputPattern = StringUtils.defaultString((String) additionalInfo.get(ADDITIONAL_INFO_INPUT_PATTERN), "");
            }
            return "<input class=\"form-control\" type=\"text\" id=\"" + name + "\" name=\"" + name + "\" th:value=\"${" + entityName + "." + name + "}\" " + inputPattern + " required>";
        } else if (cpnType == CpnTypeEnum.TEXTAREA) {
            return "<textarea class=\"form-control\" id=\"" + name + "\" name=\"" + name + "\" th:text=\"${" + entityName + "." + name + "}\" required></textarea>";
        } else if (cpnType == CpnTypeEnum.SELECT) {
            return "<sp:select class=\"form-control\" id=\""+name+"\" name=\""+name+"\" key=\""+dictType+"\" th:value=\"${"+entityName+"."+ StringUtils.substringBefore(embeddedClassPathName, ".") +" ne null ? "+entityName+"."+embeddedClassPathName+" : ''}\"  required/>";
        } else if (cpnType == CpnTypeEnum.RADIO) {
            return "<div class=\"form-check form-check-inline mr-2\" th:each =\"dict : ${" + dictType + "}\">\n" +
                    "<input class=\"form-check-input\" type=\"radio\" th:id=\"${'radio_' + dict.name}\" th:value=\"${dict.name}\" name=\"" + name + "\" th:checked=\"${"+entityName+"."+embeddedName+" ne null && dict.name == " + entityName + "." + embeddedClassPathName + "}\">\n" +
                    "<label class=\"form-check-label\" th:for=\"${'radio_' + dict.name}\" th:text=\"${dict.label}\"></label>\n" +
                    "</div>";
        } else if (cpnType == CpnTypeEnum.CHECKBOX) {
            return "<div class=\"form-check form-check-inline mr-2\" th:each =\"dict : ${" + dictType + "}\" th:each=\"dict : ${" + dictType + "}\" th:with=\"" + name + "=${new com.rick.formflow.form.cpn.CheckBox().parseValue("+entityName+"."+embeddedName+" ne null ? " + entityName + "." + embeddedClassPathName + " : null)}\">\n" +
                    "<input class=\"form-check-input\" type=\"checkbox\" th:id=\"${'checkbox_' + dict.name}\" th:value=\"${dict.name}\" name=\"" + name + "\" th:checked=\"${" + name + " ne null && #sets.contains(" + name + ", dict.name)}\">\n" +
                    "<label class=\"form-check-label\" th:for=\"${'checkbox_' + dict.name}\" th:text=\"${dict.label}\"></label>\n" +
                    "</div>";
        } else if  (cpnType == CpnTypeEnum.NUMBER_TEXT) {
            return "<input class=\"form-control\" type=\"number\" id=\"" + name + "\" name=\"" + name + "\" th:value=\"${" + entityName + "." + name + "}\"  pattern=\"-?\\d+(\\.\\d+)?\" required>";
        } else if  (cpnType == CpnTypeEnum.SWITCH) {
            return "<div class=\"form-check form-check-inline mr-2\">\n" +
                    "<input class=\"form-check-input\" type=\"checkbox\" th:id=\""+name+"\" th:value=\"1\" name=\"" + name + "\" th:checked=\"${"+entityName+"." + name + " ne null && "+entityName+"."+name+" == true }\">\n" +
                    "<label class=\"form-check-label\" th:for=\""+name+"\"></label>\n" +
                    "</div>";
        } else if  (cpnType == CpnTypeEnum.DATE) {
            return "<input class=\"form-control\" type=\"text\" id=\"" + name + "\" name=\"" + name + "\" th:value=\"${" + entityName + "." + name + "}\" required>";
        }

        return "<!-- " + name + " 没有找到模版-->";
    }
}
