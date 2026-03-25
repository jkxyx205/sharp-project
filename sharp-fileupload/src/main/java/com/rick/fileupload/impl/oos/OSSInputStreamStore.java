package com.rick.fileupload.impl.oos;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.*;
import com.rick.common.util.FileUtils;
import com.rick.fileupload.core.AbstractInputStreamStore;
import com.rick.fileupload.core.model.StoreResponse;
import com.rick.fileupload.impl.oos.property.OSSProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Rick
 * @createdAt 2021-09-30 22:23:00
 */
@RequiredArgsConstructor
@Slf4j
public class OSSInputStreamStore extends AbstractInputStreamStore {

    private final OSS ossClient;

    private final OSSProperties ossProperties;

    @Override
    public StoreResponse store(String groupName, String storeName, String extension, InputStream is) throws IOException {
        String path = FileUtils.fullName(storeName, extension);
        String fullPath = getFullPath(groupName, path);
        ossClient.putObject(ossProperties.getBucketName(),
                fullPath, is);
        return new StoreResponse(groupName, path, fullPath, getURL(groupName, path));
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

    @Override
    public void downloadFolder(String path, String localDir) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(10);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        List<String> failedKeys = Collections.synchronizedList(new ArrayList<>());

        log.debug("开始下载 OSS 文件夹: {}", path);

        String nextMarker = null;
        boolean hasMore = true;
        int totalFiles = 0;

        // 分页遍历，每页 1000 个，直到全部列举完
        while (hasMore) {
            ListObjectsRequest listReq = new ListObjectsRequest(ossProperties.getBucketName())
                    .withPrefix(path)
                    .withMarker(nextMarker)
                    .withMaxKeys(1000);

            ObjectListing listing = ossClient.listObjects(listReq);
            List<OSSObjectSummary> objects = listing.getObjectSummaries();

            for (OSSObjectSummary obj : objects) {
                String ossKey = obj.getKey();

                // 跳过文件夹对象本身（以 / 结尾）
                if (ossKey.endsWith("/")) continue;

                totalFiles++;
                final int fileIndex = totalFiles;

                executor.submit(() -> {
                    try {
                        // 计算本地路径：去掉 ossFolder 前缀，保留子目录结构
                        String relativePath = ossKey.substring(path.length());
                        Path localPath = Paths.get(localDir, relativePath);

                        // 创建本地目录
                        Files.createDirectories(localPath.getParent());

                        // 执行下载
                        ossClient.getObject(new GetObjectRequest(ossProperties.getBucketName(), ossKey), localPath.toFile());

                        log.debug("[{}] ✔ {}", fileIndex, ossKey);
                        successCount.incrementAndGet();

                    } catch (Exception e) {
                        log.error("[{}] ✘ {} → {}", fileIndex, ossKey, e.getMessage());
                        failCount.incrementAndGet();
                        failedKeys.add(ossKey);
                    }
                });
            }

            hasMore = listing.isTruncated();
            nextMarker = listing.getNextMarker();
        }

        log.debug("共发现 {} 个文件，等待下载完成...", totalFiles);

        // 等待所有任务完成
        executor.shutdown();
        executor.awaitTermination(60, TimeUnit.MINUTES);

        // 打印结果
        log.debug("✅ 成功: {} 个", successCount.get());
        log.debug("❌ 失败: {} 个", failCount.get());
        if (!failedKeys.isEmpty()) {
            log.debug("失败文件列表:");
            failedKeys.forEach(k -> log.debug("  - {}", k));
        }
    }

    @Override
    public void downloadFile(String path, String localPath) {
        Path localFilepath = Paths.get(localPath);
        try {
            Files.createDirectories(localFilepath.getParent());
            log.debug("开始下载 OSS 文件夹: {}", path);
            ossClient.getObject(new GetObjectRequest(ossProperties.getBucketName(), path), localFilepath.toFile());
            log.debug("✅ 成功: 1 个");
        } catch (IOException e) {
            log.debug("❌ 失败: 1 个");
            throw new RuntimeException(e);
        }
    }
}
