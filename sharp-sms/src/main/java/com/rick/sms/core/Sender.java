package com.rick.sms.core;

import java.util.Map;

/**
 * @author Rick
 * @createdAt 2021-03-03 15:28:00
 */
public interface Sender {

    /**
     * @param phone 手机号
     * @param sign 签名
     * @param templateCode 模版code
     * @param params 短信参数
     */
    void send(String phone, String sign, String templateCode, Map<String, String> params);
}
