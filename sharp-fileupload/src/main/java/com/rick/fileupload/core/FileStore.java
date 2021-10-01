package com.rick.fileupload.core;

import com.google.common.collect.Lists;
import com.rick.fileupload.core.model.FileMeta;
import com.rick.fileupload.core.model.StoreResponse;
import com.rick.fileupload.core.support.FileMetaUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

/**
 * @author Rick
 * @createdAt 2021-09-30 13:45:00
 */
@RequiredArgsConstructor
public class FileStore implements InputStreamStore {

    private final InputStreamStore inputStreamStore;

    /**
     * 文件存储
     * @param multipartFileList
     * @return
     */
    public List<? extends FileMeta> upload(List<MultipartFile> multipartFileList, String groupName) throws IOException {
        if (CollectionUtils.isEmpty(multipartFileList)) {
            return Collections.emptyList();
        }

        List<FileMeta> fileMetaList = Lists.newArrayListWithExpectedSize(multipartFileList.size());
        for (MultipartFile multipartFile : multipartFileList) {
            FileMeta fileMeta = FileMetaUtils.parse(multipartFile);
            StoreResponse store = inputStreamStore.store(groupName, fileMeta.getExtension(), multipartFile.getInputStream());
            fileMeta.setGroupName(groupName);
            fileMeta.setPath(store.getPath());
            fileMeta.setUrl(store.getUrl());
            fileMetaList.add(fileMeta);
        }

        return fileMetaList;
    }

    /**
     * 文件存储
     * @param fileList
     * @param groupName
     * @return
     */
    public List<? extends FileMeta> storeFiles(List<File> fileList, String groupName) throws IOException {
        if (CollectionUtils.isEmpty(fileList)) {
            return Collections.emptyList();
        }

        List<FileMeta> fileMetaList = Lists.newArrayListWithExpectedSize(fileList.size());

        for (File file : fileList) {
            fileMetaList.add(FileMetaUtils.parse(file));
        }
        return storeFileMeta(fileMetaList, groupName);
    }

    /**
     * 文件存储
     * @param fileMetaList FileMeta需要设置data
     * @param groupName
     * @return
     */
    public List<? extends FileMeta> storeFileMeta(List<? extends FileMeta> fileMetaList, String groupName) throws IOException {
        if (CollectionUtils.isEmpty(fileMetaList)) {
            return Collections.emptyList();
        }

        for (FileMeta fileMeta : fileMetaList) {
            StoreResponse store = inputStreamStore.store(groupName, fileMeta.getExtension(), new ByteArrayInputStream(fileMeta.getData()));
            fileMeta.setGroupName(groupName);
            fileMeta.setPath(store.getPath());
            fileMeta.setUrl(store.getUrl());
        }

        return fileMetaList;
    }

    @Override
    public StoreResponse store(String groupName, String extension, InputStream is) throws IOException {
        return inputStreamStore.store(groupName, extension, is);
    }

    @Override
    public StoreResponse store(String groupName, String storeName, String extension, InputStream is) throws IOException {
        return inputStreamStore.store(groupName, storeName, extension, is);
    }

    @Override
    public void delete(String groupName, String path) throws IOException {
        inputStreamStore.delete(groupName, path);
    }

    @Override
    public String getURL(String groupName, String path) {
        return inputStreamStore.getURL(groupName, path);
    }

    @Override
    public InputStream getInputStream(String groupName, String path) throws IOException {
        return inputStreamStore.getInputStream(groupName, path);
    }

    @Override
    public byte[] getByteArray(String groupName, String path) throws IOException {
        return inputStreamStore.getByteArray(groupName, path);
    }

}
