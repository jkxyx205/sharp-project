package com.rick.formflow.form.service;

import java.util.Map;

/**
 * @author Rick
 * @createdAt 2021-11-07 09:00:00
 */
public interface FormAdvice {

    /**
     * 表单实例数据保存之后
     */
    void beforeInstanceHandle(Long formId, Long instanceId, Map<String, Object> values);

    /**
     * 表单实例数据保存之后
     */
    void afterInstanceHandle(Long formId, Long instanceId, Map<String, Object> values);

//    Map<String, Object> getValue(Long formId, Long instanceId);
}
