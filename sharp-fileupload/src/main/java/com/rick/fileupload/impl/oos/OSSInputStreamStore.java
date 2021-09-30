package com.rick.fileupload.impl.oos;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.OSSObject;
import com.rick.common.util.IdGenerator;
import com.rick.fileupload.core.InputStreamStore;
import com.rick.fileupload.core.model.StoreResponse;
import com.rick.fileupload.impl.oos.property.OSSProperties;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Rick
 * @createdAt 2021-09-30 22:23:00
 */
@RequiredArgsConstructor
public class OSSInputStreamStore implements InputStreamStore {

    private final OSS ossClient;

    private final OSSProperties ossProperties;

    @Override
    public StoreResponse store(String groupName, String extension, InputStream is) throws IOException {
        String storeName = IdGenerator.getSequenceId() + "." + extension;
        ossClient.putObject(ossProperties.getBucketName(),
                groupName + "/" +  storeName,
                is);
        return new StoreResponse(groupName, storeName,ossProperties.getBucketName() + File.separator + groupName + "／" + storeName ,
                getURL(groupName, storeName));
    }

    @Override
    public void delete(String groupName, String path) throws IOException {
        ossClient.deleteObject(ossProperties.getBucketName(), getOssPath(groupName, path));
    }

    @Override
    public String getURL(String groupName, String path) {
        return "https://" + ossProperties.getBucketName() + "." +ossProperties.getEndpoint()+ "/" + getOssPath(groupName, path);
    }

    @Override
    public InputStream getInputStream(String groupName, String path) {
        OSSObject ossObject = ossClient.getObject(ossProperties.getBucketName(), getOssPath(groupName, path));
        return ossObject.getObjectContent();
    }

    private String getOssPath(String groupName, String path) {
        return groupName + "/" + path;
    }
}
