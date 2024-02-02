package com.rick.admin.common;

import com.rick.common.http.model.Result;
import com.rick.common.http.model.ResultUtils;
import com.rick.fileupload.client.support.DocumentService;
import com.rick.fileupload.core.support.FileMetaUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * @author Rick.Xu
 * @date 2023/6/2 16:11
 */
@RestController
@RequestMapping("/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    /**
     * 批量上传文件
     * @param multipartRequest 文件上传
     */
    @PostMapping("/upload")
    public Result<List<?>> fileUpload(MultipartHttpServletRequest multipartRequest, String groupName) throws IOException {
        return ResultUtils.success(documentService.store(FileMetaUtils.parse(multipartRequest, multipartRequest.getParameter("name")), StringUtils.defaultString(groupName, "upload")));
    }

    /**
     * http://localhost:8080/documents/download?id=475029213070921728
     * 下载文件
     * @param id 文件id
     */
    @GetMapping(value = "/download/{id}")
    public void download(HttpServletRequest request, HttpServletResponse response, @PathVariable long id) throws IOException {
        documentService.download(request, response , id);
    }

}