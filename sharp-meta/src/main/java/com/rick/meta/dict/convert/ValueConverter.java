package com.rick.meta.dict.convert;

import java.io.Serializable;

/**
 * @author Rick
 * @createdAt 2023-03-17 13:43:00
 */
public interface ValueConverter<T> extends Serializable {

    String convert(Object context, T value);
}
