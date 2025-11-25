package com.rick.common.function;

import java.io.Serializable;

@FunctionalInterface
public interface SConsumer<T> extends SInfo, Serializable {

    void accept(T t);

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