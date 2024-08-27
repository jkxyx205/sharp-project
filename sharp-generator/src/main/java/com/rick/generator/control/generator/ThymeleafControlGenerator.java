package com.rick.generator.control.generator;

import com.rick.formflow.form.cpn.core.CpnTypeEnum;
import com.rick.generator.control.AbstractControlGenerator;
import com.rick.generator.control.RenderTypeEnum;

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
    public String generate(CpnTypeEnum cpnType, String entityName, String name) {
        if (cpnType == CpnTypeEnum.TEXT) {
            return "<input class=\"form-control\" type=\"text\" id=\""+name+"\" value=\"\" name=\""+name+"\" required th:value=\"${"+entityName+"."+name+"}\">";
        } else if(cpnType == CpnTypeEnum.TEXTAREA) {
            return "<textarea class=\"form-control\" id=\""+name+"\" name=\""+name+"\" th:text=\"${"+entityName+"."+name+"}\"></textarea>";
        }

        return "<!-- "+name+" 没有找到模版-->";
    }
}
