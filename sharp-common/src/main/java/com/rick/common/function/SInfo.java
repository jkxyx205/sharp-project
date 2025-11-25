package com.rick.common.function;

/**
 * @author Rick.Xu
 * @date 2025/11/17 10:27
 */
public interface SInfo {

    // 是否是方法引用
    boolean isMethodReference();

    // 方法引用时获取方法名
    String getMethodName();

    // 方法引用时获取属性名
    String getPropertyName();

}
