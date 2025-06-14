package com.rick.notification.bark;

import com.rick.common.util.JsonUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * @author Rick.Xu
 * @date 2025/4/27 15:05
 */
@FieldDefaults(makeFinal=true, level= AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Slf4j
public class PushNotificationService {

    DeviceKey deviceKey;

    public void push(PushNotification pushNotification) {
        if (ArrayUtils.isNotEmpty(deviceKey.getDeviceKeys())) {
            if (ArrayUtils.isEmpty(pushNotification.getDeviceKeys())) {
                pushNotification.setDeviceKeys(deviceKey.getDeviceKeys());
            } else {
                // merge
                String[] merged = Arrays.copyOf(pushNotification.getDeviceKeys(), pushNotification.getDeviceKeys().length + deviceKey.getDeviceKeys().length);
                System.arraycopy(deviceKey.getDeviceKeys(), 0, merged, pushNotification.getDeviceKeys().length, deviceKey.getDeviceKeys().length);
                pushNotification.setDeviceKeys(merged);
            }
        }

        if (StringUtils.isEmpty(pushNotification.getDeviceKey())) {
            pushNotification.setDeviceKey(deviceKey.getDeviceKey());
        }

        // https://api.day.app 批量推送仅支持Json请求，需 bark-server 更新至 v2.1.9 https://bark.day.app/#/batch
//        pushNotification.setDeviceKey(deviceKey.getDeviceKeys()[0]);

        postJson(pushNotification);

        // cloudflare 不允许 curl 访问
//        try {
//            HttpUtils.postJson(URL, pushNotification);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    private void postJson(PushNotification pushNotification) {
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JsonUtils.toJson(pushNotification), JSON);

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS) // 连接超时5秒
                .readTimeout(10, TimeUnit.SECONDS)   // 读取超时10秒
                .build();

        Request request = new Request.Builder()
                .url(deviceKey.getUrl() + "/push")
                .post(body)
                .header("User-Agent", "Mozilla/5.0")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                log.info("成功：{}", response.body().string());
            } else {
                log.error("请求失败，状态码：{}", response.code());
            }
        } catch (IOException e) {
            log.error("请求异常：{}", e.getMessage());
        }
    }

}
