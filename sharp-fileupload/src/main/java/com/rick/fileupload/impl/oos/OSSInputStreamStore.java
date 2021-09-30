package com.rick.fileupload.impl.oos;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.OSSObject;
import com.rick.fileupload.core.AbstractInputStreamStore;
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
public class OSSInputStreamStore extends AbstractInputStreamStore {

    private final OSS ossClient;

    private final OSSProperties ossProperties;

    @Override
    public StoreResponse store(String groupName, String storeName, String extension, InputStream is) throws IOException {
        ossClient.putObject(ossProperties.getBucketName(),
                groupName + "/" +  storeName,
                is);
        return new StoreResponse(groupName, storeName,ossProperties.getBucketName() + File.separator + groupName + "／" + storeName ,
                getURL(groupName, storeName));
    }

    @Override
    public void delete(String groupName, String path) throws IOException {
        ossClient.deleteObject(ossProperties.getBucketName(), getFullPath(groupName, path));
    }


    @Override
    protected String getServerUrl() {
        return "https://" + ossProperties.getBucketName() + "." + ossProperties.getEndpoint() + "/";
    }

    @Override
    public InputStream getInputStream(String groupName, String path) {
        OSSObject ossObject = ossClient.getObject(ossProperties.getBucketName(), getFullPath(groupName, path));
        return ossObject.getObjectContent();
    }


}
