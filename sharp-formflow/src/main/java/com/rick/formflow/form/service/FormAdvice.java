package com.rick.formflow.form.service;

import com.rick.formflow.form.service.bo.FormBO;

import java.util.Map;

/**
 * @author Rick
 * @createdAt 2021-11-07 09:00:00
 */
public interface FormAdvice {

    /**
     * 表单实例数据保存之后
     */
    void beforeInstanceHandle(FormBO form, Long instanceId, Map<String, Object> values);

    /**
     * 表单实例数据保存之后
     */
    void afterInstanceHandle(FormBO form, Long instanceId, Map<String, Object> values);

//    Map<String, Object> getValue(Long formId, Long instanceId);
}
