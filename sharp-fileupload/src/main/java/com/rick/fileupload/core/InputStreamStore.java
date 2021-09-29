package com.rick.fileupload.core;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Rick
 * @createdAt 2021-09-29 13:46:00
 */
public interface InputStreamStore {

    /**
     * @param is
     * @param extension 文件扩展名
     * @return 返回 文件相对路径 groupName & path
     */
    String[] store(InputStream is, String extension) throws IOException;

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
