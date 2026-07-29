package com.rick.common.util.model;

import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
public class Device {

    /**
     * 是否为普通设备（非移动、非平板，通常指桌面浏览器）
     */
    private boolean normal;

    /**
     * 是否为手机设备
     */
    private boolean mobile;

    /**
     * 是否为平板设备
     */
    private boolean tablet;

    /**
     * 操作系统平台
     */
    private Platform platform;

    public enum Platform {
        IOS,
        ANDROID,
        UNKNOWN
    }

    @Override
    public String toString() {
        List<String> flags = new ArrayList<>();
        if (normal) {
            flags.add("normal");
        }
        if (mobile) {
            flags.add("mobile");
        }
        if (tablet) {
            flags.add("tablet");
        }

        return "Device{" +
                "type=" + (flags.isEmpty() ? "unknown" : String.join(",", flags)) +
                ", platform=" + platform +
                '}';
    }
}