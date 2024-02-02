package com.rick.admin.auth.common;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * @author Rick.Xu
 * @date 2023/5/29 13:54
 */
public class AuthConstants {

    public static final List<String> IGNORE_URL = Lists.newArrayList("/login", "/forbidden", "/kaptcha", "/error");

    public static final int MAX_TRY_COUNT = 5;

    public static final int MAX_TRY_IMAGE_CODE_COUNT = 2;

    public static final int LOCK_MINUTES = 10;

    public static final String IMAGE_CODE_SESSION_KEY = "showCode";

    public static final String DEFAULT_PASSWORD = "111111";
}
