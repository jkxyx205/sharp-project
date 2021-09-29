package com.rick.fileupload.local;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * @author Rick
 * @createdAt 2021-09-29 14:27:00
 */
@SpringBootTest
public class LocalInputStreamStoreTest {

    @Autowired
    private PropertyConfigurerLocalInputStreamStore propertyConfigurerLocalInputStreamStore;

    @Test
    public void testPropertyStore() throws IOException {
        String[] path = propertyConfigurerLocalInputStreamStore.store(new FileInputStream("/Users/rick/jkxyx205/avatar.jpeg")
                , "jpeg");
        System.out.println(path[0] + "," + path[1]);
    }

    @Test
    public void testPropertyDelete() throws IOException {
        propertyConfigurerLocalInputStreamStore.delete("fileupload", "rick/474736657036644352.jpeg");
    }

    @Test
    public void testPropertyDelete2() throws IOException {
        propertyConfigurerLocalInputStreamStore.delete("fileupload", "rick/474736657036644352.jpeg");
    }

    @Test
    public void getURL(){
        String url = propertyConfigurerLocalInputStreamStore.getURL(null,"a/c/474633321763475456.jpeg");
        String url2 = propertyConfigurerLocalInputStreamStore.getURL(null, "a/c/474633321763475456.jpeg");
        System.out.println(url);
        System.out.println(url2);
    }
}
