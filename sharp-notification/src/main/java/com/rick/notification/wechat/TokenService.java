package com.rick.notification.wechat;

import com.rick.common.util.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * @author Rick.Xu
 * @date 2024/3/6 09:21
 */
@Slf4j
@RequiredArgsConstructor
public final class TokenService {

    private final WechatKey wechatKey;

    private String token = null;

    private final OkHttpClient client = new OkHttpClient();

    private LocalDateTime tokenGetTime = null;

    public String getToken() {
        if (token != null) {
            // 检查token是否有效
            long seconds = Duration.between(tokenGetTime, LocalDateTime.now()).getSeconds();
            log.info("seconds: {}", seconds);
            if (seconds <= 7000) {
                return token;
            }
        }

        Request request = new Request.Builder()
                .url("https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid="+wechatKey.getAppid()+"&secret="+wechatKey.getSecret()+"")
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                return null;
            }

            String responseBody = response.body().string();
            Map<String, ?> responseMap = JsonUtils.toObject(responseBody, Map.class);
            if (responseMap.get("access_token") != null) {
                token = (String) responseMap.get("access_token");
                tokenGetTime = LocalDateTime.now();
                log.info("成功获取 token：{}", token);
            } else {
                log.error("获取 toke 失败。{}", responseMap.get("errmsg"));
            }
        } catch (IOException e) {
            return null;
        }

        return token;
    }
}
