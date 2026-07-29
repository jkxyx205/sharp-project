package com.rick.common.util;

import com.rick.common.util.model.Device;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.regex.Pattern;

public class DeviceUtils {

    private DeviceUtils() {
    }

    // 平板特征（优先匹配，因为 iPad/Android Tablet 的 UA 里也会包含 "Mobile" 等干扰词）
    private static final Pattern TABLET_PATTERN = Pattern.compile(
            "iPad|Android(?!.*Mobile)|Tablet|Kindle|Silk|PlayBook|Nexus (7|9|10)",
            Pattern.CASE_INSENSITIVE
    );

    // 手机特征
    private static final Pattern MOBILE_PATTERN = Pattern.compile(
            "iPhone|iPod|Android.*Mobile|Windows Phone|BlackBerry|BB10|Mobile Safari|Opera Mini|IEMobile",
            Pattern.CASE_INSENSITIVE
    );

    // iOS 平台特征
    private static final Pattern IOS_PATTERN = Pattern.compile(
            "iPhone|iPad|iPod",
            Pattern.CASE_INSENSITIVE
    );

    // Android 平台特征
    private static final Pattern ANDROID_PATTERN = Pattern.compile(
            "Android",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * 根据请求解析当前设备信息
     */
    public static Device getCurrentDevice(HttpServletRequest request) {
        String userAgent = resolveUserAgent(request);

        boolean isTablet = TABLET_PATTERN.matcher(userAgent).find();
        // 平板优先判定：命中平板特征后不再算作 mobile
        boolean isMobile = !isTablet && MOBILE_PATTERN.matcher(userAgent).find();
        boolean isNormal = !isTablet && !isMobile;

        Device.Platform platform = resolvePlatform(userAgent);

        return Device.builder()
                .normal(isNormal)
                .mobile(isMobile)
                .tablet(isTablet)
                .platform(platform)
                .build();
    }

    private static String resolveUserAgent(HttpServletRequest request) {
        if (request == null) {
            return "";
        }
        String userAgent = request.getHeader("User-Agent");
        return StringUtils.hasText(userAgent) ? userAgent : "";
    }

    private static Device.Platform resolvePlatform(String userAgent) {
        if (IOS_PATTERN.matcher(userAgent).find()) {
            return Device.Platform.IOS;
        }
        if (ANDROID_PATTERN.matcher(userAgent).find()) {
            return Device.Platform.ANDROID;
        }
        return Device.Platform.UNKNOWN;
    }
}
