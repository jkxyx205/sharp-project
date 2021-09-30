package com.rick.fileupload.core;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Rick
 * @createdAt 2021-09-29 21:37:00
 */
public abstract class AbstractInputStreamStore implements InputStreamStore {

    @Override
    public InputStream getInputStream(String groupName, String path) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request req = new Request.Builder()
                .url(getURL(groupName, path))
                .get()  //默认为GET请求，可以不写
                .build();

        final Call call = client.newCall(req);
        return call.execute().body().byteStream();
    }

    @Override
    public String getURL(String groupName, String path) {
        return getServerUrl() + groupName + "/" + path;
    }

    protected abstract String getServerUrl();

}
