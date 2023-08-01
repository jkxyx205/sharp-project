package com.rick.formflow.form.service;

import com.rick.formflow.form.cpn.core.Form;
import com.rick.formflow.form.service.bo.FormBO;

import java.util.List;
import java.util.Map;

/**
 * @author Rick
 * @createdAt 2021-11-07 09:00:00
 */
public interface FormAdvice {

    /**
     * 表单实例数据保存之后
     */
    default void beforeInstanceHandle(FormBO form, Long instanceId, Map<String, Object> values){}

    /**
     * 表单实例数据保存之后
     */
    default void afterInstanceHandle(FormBO form, Long instanceId, Map<String, Object> values) {}

    /**
     * 获取实例之前
     * @param instanceId
     * @param values
     */
    default void beforeGetInstance(Long instanceId, Map<String, Object> values) {}

    /**
     * 获取实例之后
     * @param instanceId
     */
    default void afterGetInstance(Form form, Long instanceId, List<FormBO.Property> propertyList, Map<String, Object> valueMap) {}

    /**
     * 删除实例之前
     * @param instanceId
     */
    default void beforeDeleteInstance(Long instanceId) {}
}
