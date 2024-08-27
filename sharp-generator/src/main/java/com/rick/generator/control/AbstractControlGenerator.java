package com.rick.generator.control;

import com.rick.formflow.form.cpn.core.CpnTypeEnum;

/**
 * @author Rick.Xu
 * @date 2024/8/25 19:55
 */
public abstract class AbstractControlGenerator {

    public abstract RenderTypeEnum renderType();

    public abstract String generate(CpnTypeEnum cpnType, String entityName, String name);
}
