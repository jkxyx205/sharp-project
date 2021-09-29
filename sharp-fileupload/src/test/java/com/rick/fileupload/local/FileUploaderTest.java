package com.rick.fileupload.local;


import com.rick.fileupload.core.FileUploader;
import com.rick.fileupload.persist.Document;
import com.rick.fileupload.persist.support.DocumentUtils;
import org.apache.commons.io.FileUtils;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * @author Rick
 * @createdAt 2021-09-29 14:27:00
 */
@SpringBootTest
public class FileUploaderTest {

    @Autowired
    private FileUploader fileUploader;

    @Test
    public void testUpload() throws IOException {
        File file = new File("/Users/rick/jkxyx205/tmp/fileupload/a/b/474645016472883200.jpeg");
        Document document = DocumentUtils.parseToDocument(file);
        fileUploader.upload2(Lists.newArrayList(document), null, "/a/q");
    }

    @Test
    public void testDelete() throws IOException {
        fileUploader.delete(474707684453945344L);
    }

    @Test
    public void testRename() {
        fileUploader.rename(474709254159634432L, "Test");
    }

    @Test
    public void testGetUrl() {
        String url = fileUploader.getURL(474709254159634432L);
        System.out.println(url);
    }

}
