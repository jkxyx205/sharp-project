package com.rick.common.function;

import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;

/**
 * @author Rick.Xu
 * @date 2025/11/17 10:29
 */
class SInfoHelper {

    // 获取方法名
    static String getMethodName(Object lambdaObject) {
        try {
            return getSerializedLambda(lambdaObject).getImplMethodName();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // 获取属性名
    static String getPropertyName(Object lambdaObject) {
        String methodName = getMethodName(lambdaObject);
        if (methodName.startsWith("get") && methodName.length() > 3) {
            String name = methodName.substring(3);
            return Character.toLowerCase(name.charAt(0)) + name.substring(1);
        }
        return methodName;
    }

    static boolean isMethodReference(Object lambdaObject) {
        try {
            SerializedLambda lambda = getSerializedLambda(lambdaObject);
            String methodName = lambda.getImplMethodName();
            int kind = lambda.getImplMethodKind();
            // 方法引用的特征：
            // 1. 方法名不以 lambda$ 开头
            // 2. implMethodKind 为 5(虚方法) 或 6(静态) 或 7(特殊)
            return !methodName.startsWith("lambda$") &&
                    (kind == 5 || kind == 6 || kind == 7);
        } catch (Exception e) {
            return false;
        }
    }

    static private SerializedLambda getSerializedLambda(Object lambdaObject) {
        try {
            Method method = lambdaObject.getClass().getDeclaredMethod("writeReplace");
            method.setAccessible(true);
            SerializedLambda lambda = (SerializedLambda) method.invoke(lambdaObject);
            return lambda;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
