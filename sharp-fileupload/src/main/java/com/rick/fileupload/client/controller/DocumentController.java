package com.rick.fileupload.client.controller;


import com.rick.common.http.model.Result;
import com.rick.common.http.model.ResultUtils;
import com.rick.fileupload.client.support.Document;
import com.rick.fileupload.client.support.DocumentService;
import com.rick.fileupload.core.model.FileMeta;
import com.rick.fileupload.core.support.FileMetaUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * FastDFS文件上传功能
 */
@RestController
@RequestMapping("/documents")
@RequiredArgsConstructor
public class DocumentController {

    private static final String UPLOAD_NAME = "file";

    private final DocumentService documentService;

    /**
     * 文件详情
     * @return
     * @throws IOException
     */
    @GetMapping(value = "/{id}")
    public Result<Document> documentInfo(@PathVariable Long id) {
        return ResultUtils.success(documentService.findById(id));
    }

    /**
     * 批量上传文件
     * @param multipartRequest
     * @return
     */
    @PostMapping("/upload")
    public Result<List<?>> fileUpload(MultipartHttpServletRequest multipartRequest) throws IOException {
        return ResultUtils.success(documentService.store(FileMetaUtils.parse(multipartRequest, UPLOAD_NAME), "upload"));
    }

    /**
     * 批量上传文件
     * @param fileList
     * @return
     */
    @PostMapping("/upload2")
    public Result<List<?>> fileUpload2(List<MultipartFile> fileList) throws IOException {
        return ResultUtils.success(documentService.store(FileMetaUtils.parse(fileList), "upload"));
    }

    /**
     * 批量文件上传
     * @param fileMetaList
     * @return
     */
    @PostMapping("/upload3")
    public Result<List<?>> fileUpload3(@RequestBody List<FileMeta> fileMetaList) throws IOException {
        return ResultUtils.success(documentService.store(fileMetaList, "upload"));
    }

    /**
     * http://localhost:8080/documents/download?id=475029213070921728
     * 下载文件
     * @param id
     * @return
     */
    @GetMapping(value = "/download/{id}")
    public void download(HttpServletRequest request, HttpServletResponse response, @PathVariable long id) throws IOException {
        documentService.download(request, response , id);
    }

    /**
     * http://localhost:8080/documents/download?id=475029213054144512&id=475029213070921728
     * 批量下载文件
     * @param ids
     * @return
     */
    @GetMapping(value = "/download")
    public void download(HttpServletRequest request, HttpServletResponse response, @RequestParam(name = "id") long[] ids) throws IOException {
        documentService.download(request, response, ids);
    }

    /**
     * 预览文件: 通过nginx，这种访问速度会更快些
     * http://localhost:8080/documents/preview/475029213054144512
     * @return
     * @throws IOException
     */
    @GetMapping(value = "/preview/{id}")
    public void view(HttpServletResponse response, @PathVariable Long id) throws IOException {
        response.sendRedirect(documentService.getURL(id));
    }

    /**
     * 预览文件,将文件写到流中
     * http://localhost:8080/documents/preview2/475029213054144512
     * @return
     * @throws IOException
     */
    @GetMapping(value = "/preview2/{id}")
    public void view2(HttpServletRequest request, HttpServletResponse response, @PathVariable Long id) throws IOException {
        documentService.preview(request, response, id);
    }

    /**
     * 重命名
     * @param id
     * @param name
     * @return
     */
    @PutMapping("/{id}/rename")
    public Result rename(@PathVariable Long id,  String name) {
        documentService.rename(id, name);
        return ResultUtils.success();
    }

    /**
     * 删除文件
     * @return
     * @throws IOException
     */
    @DeleteMapping(value = "/{id}")
    public Result deleteDocument(@PathVariable Long id) {
        documentService.delete(id);
        return ResultUtils.success();
    }
}
