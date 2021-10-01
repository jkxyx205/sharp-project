package com.rick.fileupload.core;

import com.rick.common.util.IdGenerator;
import com.rick.fileupload.core.model.StoreResponse;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.apache.commons.io.IOUtils;

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
    public byte[] getByteArray(String groupName, String path) throws IOException {
        return IOUtils.toByteArray(getInputStream(groupName, path));
    }

    @Override
    public StoreResponse store(String groupName, String extension, InputStream is) throws IOException {
        return store(groupName, String.valueOf(IdGenerator.getSequenceId()), extension, is);
    }

    @Override
    public String getURL(String groupName, String path) {
        return getServerUrl() + groupName + "/" + path;
    }

    protected String getFullPath(String groupName, String path) {
        return groupName + "/" + path;
    }

    /**
     * 根访问地址
     * @return
     */
    protected abstract String getServerUrl();

}
