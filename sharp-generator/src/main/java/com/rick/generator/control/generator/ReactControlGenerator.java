package com.rick.generator.control.generator;

import com.rick.formflow.form.cpn.core.CpnTypeEnum;
import com.rick.generator.control.AbstractControlGenerator;
import com.rick.generator.control.FormLayoutEnum;
import com.rick.generator.control.RenderTypeEnum;

import java.util.Map;

/**
 * @author Rick.Xu
 * @date 2024/8/25 20:07
 */
public class ReactControlGenerator extends AbstractControlGenerator {

    @Override
    public RenderTypeEnum renderType() {
        return RenderTypeEnum.REACT;
    }

    @Override
    protected String generate(FormLayoutEnum formLayout, CpnTypeEnum cpnType, String entityName, String name, String dictType, Map<String, Object> additionalInfo) {
        // TODO

        return "<!-- "+name+" 没有找到模版-->";
    }
}
