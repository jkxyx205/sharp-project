package com.rick.notification.fcm;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * @author Rick.Xu
 * @date 2025/5/8 11:01
 */
@Service
public class MessagingSendService {

    public static final String URL = "https://us-central1-fir-cloudmessaging-4e2cd.cloudfunctions.net/send";

    public void send(String token, String title, String message) {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType JSON = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(JSON, "{\n    \"data\": {\n        \"to\": \""+token+"\",\n        \"ttl\": 60,\n        \"priority\": \"high\",\n        \"data\": {\n            \"text\": {\n                \"title\": \""+title+"\",\n                \"message\": \""+message+"\",\n                \"clipboard\": true\n            }\n        }\n    }\n}"
                );
        Request request = new Request.Builder()
                .url(URL)
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .build();
        try {
            client.newCall(request).execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
