package com.rick.fileupload.client.controller;


import com.rick.common.http.HttpServletResponseUtils;
import com.rick.common.http.model.Result;
import com.rick.common.http.model.ResultUtils;
import com.rick.fileupload.client.support.Document;
import com.rick.fileupload.client.support.DocumentService;
import com.rick.fileupload.core.model.FileMeta;
import com.rick.fileupload.core.support.FileMetaUtils;
import com.rick.fileupload.plugin.image.ImageParam;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
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

    private static final String UPLOAD_GROUP_NAME = "upload";

    private final DocumentService documentService;

    /**
     * 文件详情
     * @return
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
    public Result<List<?>> fileUpload(MultipartHttpServletRequest multipartRequest, String groupName) throws IOException {
        return ResultUtils.success(documentService.store(FileMetaUtils.parse(multipartRequest,
                StringUtils.defaultString(multipartRequest.getParameter("name"), UPLOAD_NAME)),
                StringUtils.defaultString(groupName, UPLOAD_GROUP_NAME)));
    }

    /**
     * 批量上传文件
     * @param fileList
     * @return
     */
    @PostMapping("/upload2")
    public Result<List<?>> fileUpload2(@RequestParam(UPLOAD_NAME) List<MultipartFile> fileList) throws IOException {
        return ResultUtils.success(documentService.store(FileMetaUtils.parse(fileList), UPLOAD_GROUP_NAME));
    }

    /**
     * 批量文件上传
     * @param fileMetaList
     * @return
     */
    @PostMapping("/upload3")
    public Result<List<?>> fileUpload3(@RequestBody List<FileMeta> fileMetaList) throws IOException {
        return ResultUtils.success(documentService.store(fileMetaList, UPLOAD_GROUP_NAME));
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
    @GetMapping(value = "/preview/{id:\\d+}")
    public void view(HttpServletResponse response, @PathVariable Long id) throws IOException {
        response.sendRedirect(documentService.getURL(id));
    }

    /**
     * 预览普通文件,将文件写到流中
     * http://localhost:8080/documents/preview2/475029213054144512
     * 预览word，必须有后缀
     * https://view.officeapps.live.com/op/view.aspx?src=http%3A%2F%2Fa923-112-87-216-2.ngrok.io%2Fdocuments%2Fpreview2%2F477896371325009920/hello.docx
     * @return
     * @throws IOException
     */
    @GetMapping(value = {"/preview2/{id:\\d+}", "/preview2/{id:\\d+}/{fileName}.{extension:(?i)docx|xlsx|pptx}"})
    public void view2(HttpServletRequest request, HttpServletResponse response, @PathVariable Long id, ImageParam imageParam) throws IOException {
        Document document = documentService.findById(id);
        documentService.preview(id, imageParam, HttpServletResponseUtils.getOutputStreamAsView(request, response, document.getFullName()));
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
     */
    @DeleteMapping(value = "/{id}")
    public Result deleteDocument(@PathVariable Long id) {
        documentService.delete(id);
        return ResultUtils.success();
    }
}
