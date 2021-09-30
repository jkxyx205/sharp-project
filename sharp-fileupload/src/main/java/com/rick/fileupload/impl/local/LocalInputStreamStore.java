package com.rick.fileupload.impl.local;

import com.rick.common.util.IdGenerator;
import com.rick.fileupload.core.AbstractInputStreamStore;
import com.rick.fileupload.core.model.StoreResponse;
import com.rick.fileupload.impl.local.property.LocalProperties;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Rick
 * @createdAt 2021-09-29 13:54:00
 */
@RequiredArgsConstructor
public class LocalInputStreamStore extends AbstractInputStreamStore {

    private final LocalProperties localProperties;

    @Override
    public StoreResponse store(String groupName, String extension, InputStream is) throws IOException {
        String storeName = IdGenerator.getSequenceId() + "." + extension;

        String storePath = getGroupNamePath(groupName);

        File storePathFolder = new File(storePath);
        if (!storePathFolder.exists()) {
            storePathFolder.mkdirs();
        }

        IOUtils.copy(is, new FileOutputStream(new File(storePath, storeName)));
        return new StoreResponse(groupName, storeName,storePath + File.separator + storeName , getURL(groupName, storeName));
    }

    @Override
    public void delete(String groupName, String path) throws IOException {
        FileUtils.forceDelete(new File(getGroupNamePath(groupName) + File.separator + path));
    }



    private String getGroupNamePath(String groupName) {
        return localProperties.getRootPath() + File.separator + groupName;
    }

    @Override
    protected String getServerUrl() {
        return localProperties.getServerUrl();
    }
}
