package com.rick.generator.control.generator;

import com.rick.formflow.form.cpn.core.CpnTypeEnum;
import com.rick.generator.control.AbstractControlGenerator;
import com.rick.generator.control.RenderTypeEnum;

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
    public String generate(CpnTypeEnum cpnType, String entityName, String name, String dictType) {
        if (cpnType == CpnTypeEnum.TEXT) {
            return "<input class=\"form-control\" type=\"text\" id=\""+name+"\" value=\"\" name=\""+name+"\" required v-model=\"form."+name+"\">";
        } else if(cpnType == CpnTypeEnum.TEXTAREA) {
            return "<textarea class=\"form-control\" id=\""+name+"\" name=\""+name+"\" v-model=\"form."+name+"\" required></textarea>";
        } else if (cpnType == CpnTypeEnum.SELECT) {
            return "<select class=\"form-control\" type=\"text\" id=\""+name+"\" value=\"\" name=\""+name+"\" required v-model=\"form."+name+"\">\n" +
                    "   <option v-for=\"dict in datasource.dicts."+dictType+"\" :value=\"dict.name\" v-text=\"dict.label\"></option>\n" +
                    "</select>";
        }

        return "<!-- "+name+" 没有找到模版-->";
    }
}
