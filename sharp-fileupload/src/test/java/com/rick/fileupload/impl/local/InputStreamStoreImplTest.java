package com.rick.fileupload.impl.local;


import com.rick.fileupload.core.InputStreamStore;
import com.rick.fileupload.core.model.StoreResponse;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * @author Rick
 * @createdAt 2021-09-29 14:27:00
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class InputStreamStoreImplTest {

    @Autowired
    private InputStreamStore inputStreamStore;

    private static String path;

    @Test
    @Order(1)
    public void testPropertyStore() throws IOException {
        StoreResponse response = inputStreamStore.store("g1", "jpeg",
                new FileInputStream("/Users/rick/jkxyx205/tmp/fileupload/demo/1.jpg"));
        System.out.println(response.getGroupName());
        System.out.println(response.getPath());
        System.out.println(response.getFullPath());
        System.out.println(response.getUrl());
        path = response.getPath();;
    }

    @Test
    @Order(2)
    public void getURL(){
        String url = inputStreamStore.getURL("g1",path);
        System.out.println(url);
    }

    @Test
    @Order(3)
    public void testPropertyDelete() throws IOException {
        inputStreamStore.delete("g1", path);
    }
}
