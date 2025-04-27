package com.rick.common.http.util;

import com.rick.common.util.JsonUtils;

import java.io.*;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * @author Rick.Xu
 * @date 2025/4/27 15:20
 */
public class HttpUtils {

    /**
     * 发送 HTTP POST JSON 请求（Content-Type: application/json）
     *
     * @param url     请求地址
     * @param dataObj 要发送的对象（会转成 JSON）
     * @return 服务器响应内容
     */
    public static String postJson(String url, Object dataObj) throws Exception {
        // 1. 把对象转成 JSON 字符串
        String json = JsonUtils.toJson(dataObj);

        // 2. 创建连接
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
        conn.setRequestProperty("Accept-Charset", "UTF-8");

        // 3. 发送数据
        try (OutputStream os = conn.getOutputStream()) {
            os.write(json.getBytes(StandardCharsets.UTF_8));
        }

        // 4. 读取响应
        int responseCode = conn.getResponseCode();
        if (responseCode == 200) {
            return new String(readAllBytes(conn.getInputStream()), StandardCharsets.UTF_8);
        } else {
            return "Error: HTTP " + responseCode;
        }
    }

    /**
     * 发送 GET 请求 (带参数)
     */
    public static String get(String urlString, Object params) throws Exception {
        String formData = toFormData(params);
        String fullUrl = urlString + "?" + formData;

        URL url = new URL(fullUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        return getResponse(conn);
    }

    /**
     * 将对象转换为 x-www-form-urlencoded 格式
     */
    private static String toFormData(Object obj) throws IllegalAccessException, UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder();
        Class<?> clazz = obj.getClass();
        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            field.setAccessible(true);
            Object value = field.get(obj);
            if (value != null) {
                if (sb.length() > 0) {
                    sb.append("&");
                }
                sb.append(URLEncoder.encode(field.getName(), StandardCharsets.UTF_8.toString()));
                sb.append("=");
                sb.append(URLEncoder.encode(value.toString(), StandardCharsets.UTF_8.toString()));
            }
        }
        return sb.toString();
    }

    /**
     * 读取响应
     */
    private static String getResponse(HttpURLConnection conn) throws Exception {
        int responseCode = conn.getResponseCode();
        BufferedReader reader;
        if (responseCode >= 200 && responseCode < 300) {
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
        } else {
            reader = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8));
        }

        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        conn.disconnect();

        return response.toString();
    }

    public static byte[] readAllBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[4096];

        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }

        return buffer.toByteArray();
    }
}
