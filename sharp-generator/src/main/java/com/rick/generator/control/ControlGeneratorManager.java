package com.rick.generator.control;

import com.rick.formflow.form.cpn.core.CpnTypeEnum;
import com.rick.generator.control.generator.ReactControlGenerator;
import com.rick.generator.control.generator.ThymeleafControlGenerator;
import com.rick.generator.control.generator.VueControlGenerator;
import lombok.experimental.UtilityClass;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Rick.Xu
 * @date 2024/8/25 20:26
 */
@UtilityClass
public class ControlGeneratorManager {

    static Map<RenderTypeEnum, AbstractControlGenerator> registerMap = new HashMap<>();

    static {
        registerMap.put(RenderTypeEnum.THYMELEAF, new ThymeleafControlGenerator());
        registerMap.put(RenderTypeEnum.VUE, new VueControlGenerator());
        registerMap.put(RenderTypeEnum.REACT, new ReactControlGenerator());
    }

    public String generate(CpnTypeEnum cpnType, String entityName, String name, RenderTypeEnum renderTypeEnum) {
        return registerMap.get(renderTypeEnum).generate(cpnType, entityName, name);
    }
}
