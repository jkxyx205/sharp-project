package com.rick.demo.module.project.controller;


import com.rick.common.http.model.Result;
import com.rick.common.http.model.ResultUtils;
import com.rick.fileupload.client.support.DocumentService;
import com.rick.fileupload.core.support.FileMetaUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.IOException;
import java.util.List;

/**
 * FastDFS文件上传功能
 */
@RestController
@RequestMapping("/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    /**
     * 批量上传文件
     * @param multipartRequest
     * @return
     */
    @PostMapping("/upload")
    public Result<List<?>> fileUpload(MultipartHttpServletRequest multipartRequest) throws IOException {
        return ResultUtils.success(documentService.store(FileMetaUtils.parse(multipartRequest, multipartRequest.getParameter("name")), "upload"));
    }

}
