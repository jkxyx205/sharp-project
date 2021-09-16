package com.rick.sms.core;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 短信验证码
 * @author Rick
 * @createdAt 2021-03-03 15:30:00
 */
@RequiredArgsConstructor
public class ValidateCodeSender {

    private final Sender sender;

    public void send(String phone, String signName, String templateCode, String code) {
        Map<String, String> params = new HashMap<>(1);
        params.put("code", code);
        sender.send(phone, signName, templateCode, params);
    }
}
