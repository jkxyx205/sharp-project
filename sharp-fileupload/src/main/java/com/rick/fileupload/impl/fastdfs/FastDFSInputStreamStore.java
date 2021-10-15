package com.rick.fileupload.impl.fastdfs;

import com.rick.fileupload.core.AbstractInputStreamStore;
import com.rick.fileupload.core.model.StoreResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author Rick
 * @createdAt 2021-09-29 15:58:00
 */
@Slf4j
public class FastDFSInputStreamStore extends AbstractInputStreamStore {

    /**
     * "fdfs_client.properties"
     *
     * @param propertyFilePath
     * @throws IOException
     * @throws MyException
     */
    public FastDFSInputStreamStore(String propertyFilePath) throws IOException, MyException {
        Properties properties = new Properties();
        properties.load(new ClassPathResource(propertyFilePath).getInputStream());
        ClientGlobal.initByProperties(properties);
    }

    @Override
    public StoreResponse store(String groupName, String storeName, String extension, InputStream is) throws IOException {
        log.warn("FastDFS 无法指定存储文件名：{}", storeName);
        StorageClient storageClient = getTrackerClient();
        try {
            String[] uploadResults = storageClient.upload_file(groupName, IOUtils.toByteArray(is), extension, null);
            return new StoreResponse(uploadResults[0], uploadResults[1], getFullPath(groupName, uploadResults[1]), getURL(uploadResults[0], uploadResults[1]));
        } catch (MyException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void delete(String groupName, String path) throws IOException {
        //删除文件
        StorageClient storageClient = getTrackerClient();
        try {
            storageClient.delete_file(groupName, path);
        } catch (MyException e) {
            throw new IOException(e.getMessage());
        }
    }

    private static StorageClient getTrackerClient() throws IOException {
        TrackerServer trackerServer = getTrackerServer();
        StorageClient storageClient = new StorageClient(trackerServer, null);
        return storageClient;
    }

    private static TrackerServer getTrackerServer() throws IOException {
        TrackerClient trackerClient = new TrackerClient();
        TrackerServer trackerServer = trackerClient.getConnection();
        return trackerServer;
    }

    @Override
    public String getServerUrl() {
        try {
            return "http://" + getTrackerServer().getInetSocketAddress().getHostString() + ":" + ClientGlobal.getG_tracker_http_port() + "/";
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
