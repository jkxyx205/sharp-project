package com.rick.fileupload.impl.local;

import com.rick.common.util.StringUtils;
import com.rick.fileupload.core.AbstractInputStreamStore;
import com.rick.fileupload.core.model.StoreResponse;
import com.rick.fileupload.impl.local.property.LocalProperties;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;

/**
 * @author Rick
 * @createdAt 2021-09-29 13:54:00
 */
@RequiredArgsConstructor
public class LocalInputStreamStore extends AbstractInputStreamStore {

    private final LocalProperties localProperties;

    @Override
    public StoreResponse store(String groupName, String storeName, String extension, InputStream is) throws IOException {
        String storeFullName = StringUtils.fullName(storeName, extension);

        String storePath = getGroupNamePath(groupName);

        File storePathFolder = new File(storePath);
        if (!storePathFolder.exists()) {
            storePathFolder.mkdirs();
        }

        IOUtils.copy(is, new FileOutputStream(new File(storePath, storeFullName)));
        return new StoreResponse(groupName, storeFullName, storePath + File.separator + storeFullName, getURL(groupName, storeFullName));
    }

    @Override
    public InputStream getInputStream(String groupName, String path) throws IOException {
        return new FileInputStream(getGroupNamePath(groupName) + File.separator + path);
    }

    @Override
    public void delete(String groupName, String path) throws IOException {
        FileUtils.forceDelete(new File(getGroupNamePath(groupName) + File.separator + path));
    }

    @Override
    protected String getServerUrl() {
        return localProperties.getServerUrl();
    }

    private String getGroupNamePath(String groupName) {
        return localProperties.getRootPath() + File.separator + groupName;
    }

}
