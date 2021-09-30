package com.rick.fileupload.core.support;

import com.google.common.collect.Lists;
import com.rick.fileupload.core.model.FileMeta;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.File;
import java.io.IOException;
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
public final class FileMetaUtils {

    public static List<FileMeta> parse(MultipartHttpServletRequest multipartRequest, String formFileName) throws IOException {
        List<MultipartFile> fileList = multipartRequest.getFiles(formFileName);
        return parse(fileList);
    }

    public static List<FileMeta> parse(List<MultipartFile> fileList) throws IOException {
        if (CollectionUtils.isEmpty(fileList)) {
            return Collections.emptyList();
        }

        List<FileMeta> fileMetaList = Lists.newArrayListWithExpectedSize(fileList.size());

        for (MultipartFile multipartFile : fileList) {
            fileMetaList.add(parse(multipartFile));
        }
        return fileMetaList;
    }


    public static FileMeta parse(MultipartFile file) throws IOException {
        //设置文件基本信息
        FileMeta fileMeta = new FileMeta();
        fileMeta.setFullName(file.getOriginalFilename());
        fileMeta.setContentType(file.getContentType());
        fileMeta.setSize(file.getSize());
        fileMeta.setData(IOUtils.toByteArray(file.getInputStream()));
        return fileMeta;
    }


    public static FileMeta parse(File file) throws IOException {
        //设置文件基本信息
        FileMeta fileMeta = new FileMeta();
        fileMeta.setFullName(file.getName());
        fileMeta.setData(FileUtils.readFileToByteArray(file));
        fileMeta.setSize((long) fileMeta.getData().length);
        fileMeta.setContentType(file.toURI().toURL().openConnection().getContentType());
        return fileMeta;
    }
}
