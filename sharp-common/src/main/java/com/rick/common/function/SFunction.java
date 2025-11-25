package com.rick.common.function;

import java.io.Serializable;

@FunctionalInterface
public interface SFunction<T, R> extends SInfo, Serializable {

    R apply(T t);

    default String getMethodName() {
        return SInfoHelper.getMethodName(this);
    }

    default String getPropertyName() {
        return SInfoHelper.getPropertyName(this);
    }

    default boolean isMethodReference() {
        return SInfoHelper.isMethodReference(this);
    }

}