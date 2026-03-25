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
     * @param extension 扩展名
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

    /**
     * 获取访问地址
     * @param groupName
     * @param path
     * @return
     */
    String getURL(String groupName, String path);

    /**
     * 获取文件流
     * @param groupName
     * @param path
     * @return
     * @throws IOException
     */
    InputStream getInputStream(String groupName, String path) throws IOException;

    /**
     * 获取字节数据
     * @param groupName
     * @param path
     * @return
     * @throws IOException
     */
    byte[] getByteArray(String groupName, String path) throws IOException;

    /**
     * 下载 OSS 某文件夹下的所有文件
     *
     * @param path OSS 文件夹前缀，如 "images/" 或 "data/logs/2024/"
     * @param localDir  本地保存根目录
     */
    default void downloadFolder(String path, String localDir) throws InterruptedException {}

    /**
     * 下载 OSS 单个文件到本地
     *
     * @param path    OSS 文件路径，如 "images/photo.jpg"
     * @param localPath 本地保存路径，如 "/tmp/download/photo.jpg"
     */
    default void downloadFile(String path, String localPath) {}
}
