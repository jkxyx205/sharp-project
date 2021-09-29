package com.rick.fileupload.persist.support;

import com.google.common.collect.Lists;
import com.rick.fileupload.persist.Document;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;

/**
 * All rights Reserved, Designed By www.xhope.top
 *
 * @version V1.0
 * @Description: (用一句话描述该文件做什么)
 * @author: Rick.Xu
 * @date: 1/8/20 9:08 AM
 * @Copyright: 2020 www.yodean.com. All rights reserved.
 */
public final class DocumentUtils {

    /**
     * 产生文件随机名
     * @param ext
     */
    public static String getRandomFileName(String ext) {
        String fileName = System.currentTimeMillis() + RandomStringUtils.randomAlphanumeric(10);

        if (StringUtils.isBlank(ext))
            return fileName;

        return fileName + "." + ext;
    }


    public static Document parseToDocument(MultipartFile file) {
        //设置文件基本信息
        Document document = new Document();
        document.setFullName(file.getOriginalFilename());
        document.setContentType(file.getContentType());
        document.setSize(file.getSize());
        return document;
    }

    public static List<Document> parseToDocuments(MultipartHttpServletRequest multipartRequest, String formFileName) {
        List<MultipartFile> multipartFileList = multipartRequest.getFiles(formFileName);

        if (CollectionUtils.isEmpty(multipartFileList)) {
            return Collections.emptyList();
        }

        List<Document> documentList = Lists.newArrayListWithExpectedSize(multipartFileList.size());

        multipartFileList.forEach(file -> documentList.add(parseToDocument(file)));

        return documentList;

    }

    public static Document parseToDocument(File file) throws IOException {
        //设置文件基本信息
        Document document = new Document();
        document.setFullName(file.getName());
        document.setData(FileUtils.readFileToByteArray(file));
        document.setSize((long) document.getData().length);
        document.setContentType(file.toURI().toURL().openConnection().getContentType());

        return document;
    }
}
