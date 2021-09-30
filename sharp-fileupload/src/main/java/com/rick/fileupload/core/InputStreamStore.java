package com.rick.fileupload.core;

import com.rick.fileupload.core.model.StoreResponse;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Rick
 * @createdAt 2021-09-29 13:46:00
 */
public interface InputStreamStore {

    StoreResponse store(String groupName, String extension, InputStream is) throws IOException;

    /**
     *
     * @param groupName
     * @param storeName 磁盘存储的文件名
     * @param extension
     * @param is
     * @return
     * @throws IOException
     */
    StoreResponse store(String groupName, String storeName, String extension, InputStream is) throws IOException;

    /**
     * 删除文件
     * @param groupName
     * @param path
     * @return
     */
    void delete(String groupName, String path) throws IOException;

    String getURL(String groupName, String path);

    InputStream getInputStream(String groupName, String path) throws IOException;
}
