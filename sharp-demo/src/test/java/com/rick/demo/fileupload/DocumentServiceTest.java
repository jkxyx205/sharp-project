package com.rick.demo.fileupload;


import com.rick.fileupload.client.support.Document;
import com.rick.fileupload.client.support.DocumentService;
import com.rick.fileupload.core.model.FileMeta;
import com.rick.fileupload.core.support.FileMetaUtils;
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
public class DocumentServiceTest {

    @Autowired
    private DocumentService documentService;

    private static long id;

    @Test
    @Order(1)
    public void testStore() throws IOException {
        File file = new File("/Users/rick/Space/tmp/fileupload/demo/1.jpg");
        FileMeta fileMeta = FileMetaUtils.parse(file);
        Document store = documentService.store(fileMeta, "document");
        id = store.getId();
    }

    @Test
    @Order(2)
    public void testRename() {
        documentService.rename(id, "Test");
    }

    @Test
    @Order(3)
    public void testGetUrl() throws IOException {
        String url = documentService.getURL(id);
        System.out.println(url);
    }

    @Test
    @Order(Order.DEFAULT)
    public void testDelete() throws IOException {
        documentService.delete(id);
    }

}
