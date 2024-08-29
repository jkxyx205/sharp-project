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
        htmlBuilder.append("<label class=\"col-form-label\" for=\""+name+"\">"+label+"</label>\n");
        return htmlBuilder.append(generate(cpnType, entityName, name, dictType)).toString();
    }

    public abstract String generate(CpnTypeEnum cpnType, String entityName, String name, String dictType);

    public abstract RenderTypeEnum renderType();
}
