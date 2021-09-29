package com.rick.fileupload.core;

import com.rick.fileupload.persist.Document;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * All rights Reserved, Designed By www.xhope.top
 *
 * @version V1.0
 * @Description: 文件上传器
 * @author: Rick.Xu
 * @date: 1/7/20 6:33 PM
 * @Copyright: 2020 www.yodean.com. All rights reserved.
 */
public interface FileUploader {

    /**
     * 文件上传
     * @param multipartFileList
     * @return
     */
    List<Document> upload(List<MultipartFile> multipartFileList, String groupName, String path);

    List<Document> upload2(List<Document> documents, String groupName, String path);

    /**
     * 文件下载
     * @param request
     * @param response
     * @param ids
     * @throws IOException
     */
    void download(HttpServletRequest request, HttpServletResponse response, long... ids) throws IOException ;

    /**
     * 文件删除
     * @param ids
     */
    void delete(Long ...ids);

    /**
     * 文件重命名
     * @param id
     * @param name
     */
    void rename(long id, String name);

    /**
     * 根据id查找文件
     * @param id
     * @return
     */
    Document findById(long id);

    /**
     * 预览文件
     * @param request
     * @param response
     * @param id
     * @throws IOException
     */
    void view(HttpServletRequest request, HttpServletResponse response, long id) throws IOException;

    /**
     * 获取文件网络地址
     * @param id
     * @return
     */
    String getURL(long id);
}
