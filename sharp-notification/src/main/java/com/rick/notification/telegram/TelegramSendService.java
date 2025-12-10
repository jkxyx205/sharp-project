package com.rick.notification.telegram;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * @author Rick.Xu
 * @date 2025/12/10 13:10
 */
@Service
public class TelegramSendService {

    public void send(String url) {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();

        Request request = new Request.Builder()
                .url(url)
                .build();
        try {
            client.newCall(request).execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
