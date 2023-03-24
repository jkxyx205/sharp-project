package com.rick.demo.fileupload;


import com.rick.fileupload.core.FileStore;
import com.rick.fileupload.core.model.FileMeta;
import com.rick.fileupload.core.support.FileMetaUtils;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;

/**
 * @author Rick
 * @createdAt 2021-09-29 14:27:00
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FileStoreTest {

    @Autowired
    private FileStore fileStore;

    @Test
    @Order(1)
    public void testStore() throws IOException {
        File file = new File("/Users/rick/Space/tmp/fileupload/demo/1.jpg");
        FileMeta fileMeta = FileMetaUtils.parse(file);
        fileStore.storeFileMeta(Lists.newArrayList(fileMeta), "upload").get(0);
    }
}
