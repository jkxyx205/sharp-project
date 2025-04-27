package com.rick.notification.wechat;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

/**
 * @author Rick.Xu
 * @date 2025/4/27 20:47
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TemplateMessageSender {

    private final TokenService tokenService;

    private static final OkHttpClient client = new OkHttpClient();

    public void send(String openId, String templateId, Map<String, String> data) {
        log.info("send message {} to {} with templateId {}", data, openId, templateId);
        String url = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=" + tokenService.getToken();

        // 构造 JSON 字符串
        String jsonBody = "{\n" +
                "   \"touser\":\"" + openId + "\",\n" +
                "   \"template_id\":\"" + templateId + "\",\n" +
                "   \"data\":{\n " +
                postFormatterData(data) +
                "   }\n" +
                "}";

        // 创建请求体
        RequestBody body = RequestBody.create(
                jsonBody,
                MediaType.parse("application/json; charset=utf-8")
        );

        // 创建请求
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        // 发送请求
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                log.error("发送失败，HTTP状态码：{}", response.code());
            } else {
                log.info("发送成功：{}", response.body());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String postFormatterData(Map<String, String> data) {
        StringBuilder dataBuilder = new StringBuilder();
        for (Map.Entry<String, String> entry : data.entrySet()) {
            dataBuilder.append("\"").append(entry.getKey()).append("\":{\"value\":\"").append(entry.getValue()).append("\"},");
        }
        dataBuilder.deleteCharAt(dataBuilder.length() - 1);
        return dataBuilder.toString();
    }
}
