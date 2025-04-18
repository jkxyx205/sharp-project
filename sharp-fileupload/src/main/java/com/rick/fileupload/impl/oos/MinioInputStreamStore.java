package com.rick.fileupload.impl.oos;

import com.rick.common.util.FileUtils;
import com.rick.fileupload.core.AbstractInputStreamStore;
import com.rick.fileupload.core.model.StoreResponse;
import com.rick.fileupload.impl.oos.property.OSSProperties;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Rick
 * @createdAt 2025-04-18 15:23:00
 */
@RequiredArgsConstructor
public class MinioInputStreamStore extends AbstractInputStreamStore {

    private final MinioClient ossClient;

    private final OSSProperties ossProperties;

    @Override
    public StoreResponse store(String groupName, String storeName, String extension, InputStream is) throws IOException {
        String fileName = FileUtils.fullName(storeName, extension);
        String fullPath = getFullPath(groupName, fileName);
        try {
            ossClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(ossProperties.getBucketName())
                            .object(fullPath)
                            .contentType(FileUtils.getContentType(fileName))
                            .stream(is, -1 , 10485760)
                            .build());
        } catch (Exception e) {
            throw new IOException();
        }

        return new StoreResponse(groupName, fileName, fullPath, getURL(groupName, fileName));
    }

    @Override
    public void delete(String groupName, String path) throws IOException {
        try {
            ossClient.removeObject(
                    RemoveObjectArgs.builder().bucket(ossProperties.getBucketName()).object(getFullPath(groupName, path)).build());
        } catch (Exception e) {
            throw new IOException();
        }
    }

    @Override
    protected String getServerUrl() {
        return ossProperties.getEndpoint() + "/" + ossProperties.getBucketName() + "/";
    }

    @Override
    public InputStream getInputStream(String groupName, String path) throws IOException {
        try {
            InputStream stream =
                    ossClient.getObject(
                            GetObjectArgs.builder().bucket(ossProperties.getBucketName()).object(getFullPath(groupName, path)).build());
            return stream;
        } catch (Exception e) {
           throw new IOException();
        }
    }

}
