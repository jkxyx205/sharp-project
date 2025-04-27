package com.rick.notification.bark;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

/**
 * 参考： https://bark.day.app/#/tutorial
 * @author Rick.Xu
 * @date 2025/4/27 15:03
 */
@Data
@Builder
public class PushNotification {

    private String title;        // 推送标题
    private String subtitle;     // 推送副标题
    private String body;
    @JsonProperty("device_key") // 这里指定名字// 推送内容
    private String deviceKey;    // 设备key
    @JsonProperty("device_keys")
    private String[] deviceKeys; // 批量推送的key数组
    private String level;        // 推送中断级别 (critical, active, timeSensitive, passive)
    private Integer volume;      // 重要警告通知音量 (0-10)
    private Integer badge;       // 推送角标
    private String call;         // 传 "1" 时铃声重复播放
    private String autoCopy;     // 自动复制内容 ("1"表示开启)
    private String copy;         // 指定复制的内容
    private String sound;        // 自定义铃声
    private String icon;         // 自定义图标URL
    private String group;        // 分组
    private String ciphertext;   // 加密推送密文
    private Integer isArchive;   // 是否保存推送 (1保存)
    private String url;          // 点击推送跳转的URL
    private String action;       // 点击推送动作 ("none" 不弹窗)
}
