package com.rick.admin.demo;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Rick.Xu
 * @date 2024/8/24 14:21
 */
public class SimpleTest {

    @Test
    public void testExec() throws IOException {
        Process process = Runtime.getRuntime().exec("mvn test -Dtest=StudentTest#testReport");

        InputStream in = process.getInputStream();
        byte[] bcache = new byte[1024];
        int readSize = 0;   //每次读取的字节长度
        ByteArrayOutputStream infoStream = new ByteArrayOutputStream();
        while ((readSize = in.read(bcache)) > 0) {
            infoStream.write(bcache, 0, readSize);
        }

        System.out.println(infoStream.toString());
    }
}
