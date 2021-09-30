package com.rick.fileupload;


import com.rick.fileupload.core.FileStore;
import com.rick.fileupload.core.InputStreamStore;
import com.rick.fileupload.core.model.FileMeta;
import com.rick.fileupload.core.model.StoreResponse;
import com.rick.fileupload.core.support.FileMetaUtils;
import com.rick.fileupload.plugin.image.ImageService;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @author Rick
 * @createdAt 2021-09-29 14:27:00
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ImageServiceTest {

    @Autowired
    private ImageService imageService;

    @Autowired
    private FileStore fileStore;

    @Test
    @Order(1)
    public void testCreateImage() throws IOException {
        String image = imageService.createImage("张三", "header");
        System.out.println(image);
    }

    @Test
    @Order(1)
    public void testCropPic() throws IOException {
        File file = new File("/Users/rick/jkxyx205/tmp/fileupload/demo/1.jpg");
        FileMeta fileMeta = FileMetaUtils.parse(file);
        // 裁剪9：5
        FileMeta cropPicFileMeta = imageService.cropPic(fileMeta, 9, 5);
        // 将裁剪的存储到磁盘
        List<? extends FileMeta> crop = fileStore.storeFileMeta(Arrays.asList(cropPicFileMeta), "crop");
        System.out.println(crop.get(0).getUrl());
    }
}
