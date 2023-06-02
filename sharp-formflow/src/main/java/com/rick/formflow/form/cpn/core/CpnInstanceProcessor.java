package com.rick.formflow.form.cpn.core;


import com.rick.formflow.form.service.bo.FormBO;
import com.rick.formflow.form.valid.core.Validator;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

/**
 * @author Rick
 * @createdAt 2021-11-02 20:50:00
 */
public class CpnInstanceProcessor {

    private FormBO.Property property;

    private CpnConfigurer configurer;

    private Object paramValue;

    private Object cpnValue;

    private String stringValue;

    private Cpn cpn;

    private BindingResult bindingResult;

    public CpnInstanceProcessor(FormBO.Property property, Object paramValue, BindingResult bindingResult) {
        this.property = property;
        this.configurer = property.getConfigurer();
        this.cpn = CpnManager.getCpnByType(configurer.getCpnType());
        this.paramValue = paramValue;
        this.cpnValue = cpn.httpConverter(paramValue);
        this.stringValue = cpn.getStringValue(cpnValue);
        this.bindingResult = bindingResult;
    }

    public void valid() {
        try {
            // 选项验证
            cpn.valid(cpnValue, configurer.getOptions());
        } catch (IllegalArgumentException e) {
            bindingResult.addError(new FieldError("form", property.getName(), paramValue, false, null, null, configurer.getLabel() + e.getMessage()));
        }

        // 逻辑验证
        for (Validator validator : configurer.getValidatorList()) {
            if (this.cpn.hasValidator(validator)) {
                try {
                    validator.valid(cpnValue);
                } catch (IllegalArgumentException e) {
                    bindingResult.addError(new FieldError("form", property.getName(), paramValue, false, null, null, configurer.getLabel() + e.getMessage()));
                }
            }
        }

    }

    public String getParamValue() {
        return stringValue;
    }

    public Object getCpnValue() {
        return cpnValue;
    }
}
