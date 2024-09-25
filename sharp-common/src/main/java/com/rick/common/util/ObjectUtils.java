package com.rick.common.util;

import lombok.experimental.UtilityClass;

import java.time.temporal.Temporal;
import java.util.Collection;
import java.util.Map;

/**
 * @author Rick.Xu
 * @date 2024/8/23 06:37
 */
@UtilityClass
public class ObjectUtils {

    public boolean mayPureObject(Object obj) {
        if (obj == null) {
            return false;
        }

        return mayPureObject(obj.getClass());
    }

    public boolean mayPureObject(Class clazz) {
        if (clazz == null) {
            return false;
        }

        if (Number.class.isAssignableFrom(clazz)) {
            return false;
        } else if (CharSequence.class.isAssignableFrom(clazz)) {
            return false;
        } else if (clazz == Character.class) {
            return false;
        } else if (clazz == Boolean.class) {
            return false;
        } else if (clazz.isEnum()) {
            return false;
        } else if (clazz.isArray()) {
            return false;
        } else if (Temporal.class.isAssignableFrom(clazz)) {
            return false;
        } else if (clazz.isPrimitive()) {
            return false;
        } else if (Collection.class.isAssignableFrom(clazz) || Map.class.isAssignableFrom(clazz)) {
            return false;
        }

        return true;
    }
}
