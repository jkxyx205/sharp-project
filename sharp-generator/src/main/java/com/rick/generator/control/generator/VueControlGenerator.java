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
public class VueControlGenerator extends AbstractControlGenerator {

    @Override
    public RenderTypeEnum renderType() {
        return RenderTypeEnum.VUE;
    }

    @Override
    public String generate(FormLayoutEnum formLayout, CpnTypeEnum cpnType, String entityName, String name, String dictType, Map<String, Object> additionalInfo) {
        // 由前端构建属性属性
//            DictCategoryEnum dictCategoryEnum = (DictCategoryEnum) additionalInfo.get(ADDITIONAL_DICT_CATEGORY);
//            String value = name;
//            if (!Collection.class.isAssignableFrom(((Field) additionalInfo.get("ADDITIONAL_DICT_FIELD")).getType())
//                    && dictCategoryEnum == DictCategoryEnum.DICT_VALUE) {
//                value = com.rick.common.util.StringUtils.camelToDot(name);
//            }

        if (cpnType == CpnTypeEnum.TEXT) {
            String inputPattern = "";
            if (additionalInfo != null) {
                inputPattern = StringUtils.defaultString((String) additionalInfo.get(ADDITIONAL_INFO_INPUT_PATTERN), "");
            }
            return "<input class=\"form-control\" type=\"text\" id=\""+name+"\" name=\""+name+"\" v-model=\"form."+name+"\" "+inputPattern+" required>";
        } else if(cpnType == CpnTypeEnum.TEXTAREA) {
            return "<textarea class=\"form-control\" id=\""+name+"\" name=\""+name+"\" v-model=\"form."+name+"\" required></textarea>";
        } else if (cpnType == CpnTypeEnum.SELECT) {
            return "<select class=\"form-control\" type=\"text\" id=\""+name+"\" name=\""+name+"\" v-model=\"form."+name+"\" required>\n" +
                    "    <option v-for=\"dict in datasource.dicts."+dictType+"\" :value=\"dict.name\" v-text=\"dict.label\"></option>\n" +
                    "</select>";
        } else if (cpnType == CpnTypeEnum.RADIO) {
            return "<div class=\"form-check-group\" id="+name+"><div class=\"form-check form-check-inline mr-2\" v-for=\"dict in datasource.dicts."+dictType+"\">\n" +
                    "    <input class=\"form-check-input\" type=\"radio\" :id=\"'radio_' + dict.name\" :value=\"dict.name\" name=\""+name+"\" v-model=\"form."+name+"\">\n" +
                    "    <label class=\"form-check-label\" :for=\"'radio_' + dict.name\">{{dict.label}}</label>\n" +
                    "    </div></div>";
        } else if (cpnType == CpnTypeEnum.CHECKBOX) {
            return "<div class=\"form-check-group\" id="+name+"><div class=\"form-check form-check-inline mr-2\" v-for=\"dict in datasource.dicts."+dictType+"\">\n" +
                    "    <input class=\"form-check-input\" type=\"checkbox\" :id=\"'checkbox_' + dict.name\" :value=\"dict.name\" name=\""+name+"\" v-model=\"form."+name+"\">\n" +
                    "    <label class=\"form-check-label\" :for=\"'checkbox_' + dict.name\">{{dict.label}}</label>\n" +
                    "    </div></div>";
        }  else if  (cpnType == CpnTypeEnum.NUMBER_TEXT) {
            return "<input class=\"form-control\" type=\"number\" id=\""+name+"\" name=\""+name+"\" v-model=\"form."+name+"\" pattern=\"-?\\d+(\\.\\d+)?\" required>";
        } else if  (cpnType == CpnTypeEnum.SINGLE_CHECKBOX) {
            return "<div class=\"form-check form-check-inline mr-2\">\n" +
                    "    <input class=\"form-check-input\" type=\"checkbox\" id=\""+name+"\" value=\"1\" name=\""+name+"\" v-model=\"form."+name+"\">\n" +
                    "    </div>";
        } else if (cpnType == CpnTypeEnum.SWITCH) {
            return "<label class=\"switch switch-pill switch-primary\">\n" +
                    "    <input class=\"switch-input\" type=\"checkbox\" id=\""+name+"\" name=\""+name+"\" v-model=\"form."+name+"\">\n" +
                    "    <span class=\"switch-slider\"></span>\n" +
                    "</label>";
        } else if  (cpnType == CpnTypeEnum.DATE) {
            return "<input class=\"form-control\" type=\"text\" id=\""+name+"\" name=\""+name+"\" v-model=\"form."+name+"\" required>";
        }

        return "<!-- "+name+" 没有找到模版-->";
    }
}
