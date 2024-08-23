package com.rick.admin.module.ckeditor.controller;

import com.rick.common.util.FileUtils;
import com.rick.fileupload.client.support.DocumentService;
import com.rick.fileupload.core.model.FileMeta;
import com.rick.fileupload.core.support.FileMetaUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * All rights Reserved, Designed By www.xhope.top
 *
 * @version V1.0
 * @Description: (用一句话描述该文件做什么)
 * @author: Rick.Xu
 * @date: 9/29/19 5:26 PM
 * @Copyright: 2019 www.yodean.com. All rights reserved.
 */
@RestController
@RequestMapping("ckeditor")
public class CkeditorController {

    private static final int MAXIMUM_SIZE = 5;

    private static final String MAXIMUM_TIP_ERROR_MESSAGE = "图片最大不能超过"+MAXIMUM_SIZE+"M.";
    private static final String MAXIMUM_TYPE_ERROR_MESSAGE = "图片为以下格式：bmp, png, jpeg, jpg, gif, ico";

    @Autowired
    private DocumentService documentService;


    @PostMapping("uploadImage")
    public void uploadImage(MultipartHttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=utf-8");
        PrintWriter out = response.getWriter();

        if (!FileUtils.isImageType(request, "upload")) {
            out.write("{\"error\": {\"message\":\"" + MAXIMUM_TYPE_ERROR_MESSAGE + "\"}}");
        } else if (FileUtils.maximumSize(request, "upload", MAXIMUM_SIZE)) {
            out.write("{\n" +
                    "    \"uploaded\": 0,\n" +
                    "    \"error\": {\n" +
                    "        \"message\": \""+MAXIMUM_TIP_ERROR_MESSAGE+"\"\n" +
                    "    }\n" +
                    "}");

        } else {
            List<MultipartFile> files = request.getFiles("upload");
            List<? extends FileMeta> list = documentService.store(FileMetaUtils.parse(files), "ckeditor");
            FileMeta uf = list.get(0);
            out.write("{\n" +
                    "    \"uploaded\": 1,\n" +
                    "    \"fileName\": \""+uf.getFullName()+"\",\n" +
                    "    \"url\": \""+ uf.getUrl()+"\"\n" +
                    "}");
        }

        out.close();
    }

    @PostMapping("uploadFile")
    public List<? extends FileMeta> uploadFile(MultipartHttpServletRequest request) throws IOException {
        List<MultipartFile> files = request.getFiles("upload");
        return documentService.store(FileMetaUtils.parse(files), "ckeditor");
    }
}
