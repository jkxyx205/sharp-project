package com.rick.common.util;

/**
 * @author Rick
 * @createdAt 2021-10-27 09:48:00
 */
public abstract class Assert {

    public static void notNull(Object object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);

        }
    }

    public static void hasText(String text, String message) {
        if (!(text != null && !text.isEmpty() && containsText(text))) {
            throw new IllegalArgumentException(message);
        }
    }

    private static boolean containsText(CharSequence str) {
        int strLen = str.length();

        for(int i = 0; i < strLen; ++i) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return true;
            }
        }

        return false;
    }

}
