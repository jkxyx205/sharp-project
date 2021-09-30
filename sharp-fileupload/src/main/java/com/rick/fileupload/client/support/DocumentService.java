package com.rick.fileupload.client.support;

import com.rick.fileupload.core.model.FileMeta;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * @author Rick
 * @createdAt 2021-09-29 19:18:00
 */

public interface DocumentService {

    /**
     * @param fileMeta data数据
     * @param groupName
     * @return
     * @throws IOException
     */
    Document store(FileMeta fileMeta, String groupName) throws IOException;

    List<Document> store(List<FileMeta> fileMetaList, String groupNme) throws IOException;

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
    void preview(HttpServletRequest request, HttpServletResponse response, long id) throws IOException;

    /**
     * 获取文件网络地址
     * @param id
     * @return
     */
    String getURL(long id);

}
