package com.rick.fileupload.local;

import com.rick.common.util.IdGenerator;
import com.rick.common.util.StringUtils;
import com.rick.fileupload.core.AbstractInputStreamStore;
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
public abstract class LocalInputStreamStore extends AbstractInputStreamStore {

    @Override
    public String[] store(InputStream is, String extension) throws IOException {
        String storeName = IdGenerator.getSequenceId() + "." + extension;
        String fullPath = getRoot() + File.separator + getGroupName() + File.separator + getPath();

        File fullPathFolder = new File(fullPath);
        if (!fullPathFolder.exists()) {
            fullPathFolder.mkdirs();
        }

        IOUtils.copy(is, new FileOutputStream(new File(fullPath, storeName)));
        return new String[]  { getGroupName(), StringUtils.formatURLSeparator(getPath() + File.separator + storeName)};
    }

    @Override
    public void delete(String groupName, String path) throws IOException {
        FileUtils.forceDelete(new File( getRoot() + File.separator +groupName + File.separator + path));
    }

    public void delete(String path) throws IOException {
        FileUtils.forceDelete(new File( getRoot() + File.separator +getGroupName() + File.separator + getPath()));
    }

    public String getURL() {
        return getServerUrl() + getGroupName() + "/" + getPath();
    }

    public abstract String getGroupName();

    public abstract String getPath();

    public abstract String getRoot();

}
