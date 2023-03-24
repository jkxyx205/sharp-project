package com.rick.demo.fileupload;

import com.rick.fileupload.core.model.FileMeta;
import com.rick.fileupload.core.support.FileConvertUtils;
import com.rick.fileupload.core.support.FileMetaUtils;
import com.rick.fileupload.plugin.image.ImageParam;
import com.rick.fileupload.plugin.image.ImageService;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

@SpringBootTest
public class FileUploadTest {

    @Autowired
    private ImageService imageService;

    @Test
    public void testCrop() throws IOException {
        File file = new File("/Users/rick/Space/tmp/fileupload/demo/1.jpg");
        FileMeta fileMeta = FileMetaUtils.parse(file);

        // 裁剪9：5
        ImageParam imageParam = new  ImageParam();
        imageParam.setRw(9);
        imageParam.setRh(5);

        imageService.write(fileMeta, imageParam,
                new FileOutputStream(new File("/Users/rick/Space/tmp/fileupload/pdf/2.png")));
    }

    @Test
    public void testPdf2Image1() throws IOException {
        File file = new File("/Users/rick/Space/tmp/fileupload/pdf/1.pdf");
        FileMeta fileMeta = FileMetaUtils.parse(file);

        List<byte[]> list = FileConvertUtils.pdf2Image(fileMeta.getData(), 150);
        File folder = new File("/Users/rick/Space/tmp/fileupload/pdf");
        for (int i = 0; i < list.size(); i++) {
            FileUtils.writeByteArrayToFile(new File(folder, i + ".png"), list.get(i));
        }
    }

    @Test
    public void testPdf2Image2() throws IOException {
        File file = new File("/Users/rick/Space/tmp/fileupload/pdf/1.pdf");
        FileMeta fileMeta = FileMetaUtils.parse(file);

        FileConvertUtils.pdf2Image(fileMeta.getData(),
                new FileOutputStream("/Users/rick/Space/tmp/fileupload/pdf/full.png"),
                150);
    }

}