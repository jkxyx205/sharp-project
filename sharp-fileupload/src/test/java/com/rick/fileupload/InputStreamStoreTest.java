package com.rick.fileupload;


import com.rick.fileupload.core.InputStreamStore;
import com.rick.fileupload.core.model.StoreResponse;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Rick
 * @createdAt 2021-09-29 14:27:00
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class InputStreamStoreTest {

    @Autowired
    private InputStreamStore inputStreamStore;

    private static String path;

    @Test
    @Order(1)
    public void testPropertyStore() throws IOException {
        StoreResponse response = inputStreamStore.store("group", "jpeg",
                new FileInputStream("/Users/rick/Space/tmp/fileupload/demo/1.jpg"));
        System.out.println(response.getGroupName());
        System.out.println(response.getPath());
        System.out.println(response.getFullPath());
        System.out.println(response.getUrl());
        path = response.getPath();
    }

    @Test
    @Order(2)
    public void getURL() {
        String url = inputStreamStore.getURL("group", path);
        System.out.println(url);
    }

    @Test
    @Order(3)
    public void getInputStream() throws IOException {
        InputStream is = inputStreamStore.getInputStream("group", path);
        FileUtils.copyInputStreamToFile(is, new File("/Users/rick/Space/tmp/fileupload/download/1.png"));
        is.close();
    }

    @Test
    @Order(Order.DEFAULT)
    public void testPropertyDelete() throws IOException {
        inputStreamStore.delete("group", path);
    }
}
