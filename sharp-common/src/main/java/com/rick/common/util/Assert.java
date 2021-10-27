package com.rick.common.util;

import org.springframework.lang.Nullable;

/**
 * @author Rick
 * @createdAt 2021-10-27 09:48:00
 */
public abstract class Assert {

    public static void notNull(@Nullable Object object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }
}
