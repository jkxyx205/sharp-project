package com.rick.fileupload.upload;


import com.rick.common.http.model.Result;
import com.rick.common.http.model.ResultUtils;
import com.rick.fileupload.core.FileUploader;
import com.rick.fileupload.persist.Document;
import org.springframework.beans.factory.annotation.Autowired;
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
public class DocumentController {

    private static final String UPLOAD_NAME = "file";

    @Autowired
    private FileUploader fileUploader;


    /**
     * 批量上传文件
     * @param multipartRequest
     * @return
     */
    @PostMapping("/upload")
    public Result<List<Document>> fileUpload(MultipartHttpServletRequest multipartRequest) {
        List<MultipartFile> files = multipartRequest.getFiles(UPLOAD_NAME);
        return ResultUtils.success(fileUploader.upload(files, null, "a/s"));
    }


    /**
     * 批量文件上传
     * @param files
     * @return
     */
    @PostMapping("/upload2")
    public Result<List<Document>> fileUpload2(@RequestBody List<Document> files) {
        return ResultUtils.success(fileUploader.upload2(files, null, null));
    }

    /**
     * 下载文件
     * @param id
     * @return
     */
    @GetMapping(value = "/download/{id}")
    public void download(HttpServletRequest request, HttpServletResponse response, @PathVariable long id) throws IOException {
        fileUploader.download(request, response , id);
    }

    /**
     * 批量下载文件
     * @param ids
     * @return
     */
    @GetMapping(value = "/download")
    public void download(HttpServletRequest request, HttpServletResponse response, @RequestParam(name = "id") long[] ids) throws IOException {
        fileUploader.download(request, response, ids);

    }

    /**
     * 预览文件: 通过nginx，这种访问速度会更快些
     * @return
     * @throws IOException
     */
    @GetMapping(value = "/preview/{id}")
    public void view(HttpServletResponse response, @PathVariable Long id) throws IOException {
        response.sendRedirect(fileUploader.getURL(id));
    }
//
    /**
     * 预览文件,将文件写到流中
     * @return
     * @throws IOException
     */
    @GetMapping(value = "/preview2/{id}")
    public void view2(HttpServletRequest request, HttpServletResponse response, @PathVariable Long id) throws IOException {
        fileUploader.view(request, response, id);
    }

//    /**
//     * 预览文件 速度很慢
//     * @return
//     * @throws IOException
//     */
//    @GetMapping(value = "/preview3/{id}")
//    public void view3(HttpServletRequest request, HttpServletResponse response, @PathVariable Long id) throws IOException {
//        fileUploader.view(request, response, id);
//    }

    /**
     * 文件详情
     * @return
     * @throws IOException
     */
    @GetMapping(value = "/{id}")
    public Result<Document> documentInfo(@PathVariable Long id) {
        return ResultUtils.success(fileUploader.findById(id));
    }

    /**
     * 删除文件
     * @return
     * @throws IOException
     */
    @DeleteMapping(value = "/{id}")
    public Result deleteDocument(@PathVariable Long id) {
        fileUploader.delete(id);
        return ResultUtils.success();
    }

    /**
     * 重命名
     * @param id
     * @param fileFullName
     * @return
     */
    @PutMapping("/{id}/rename")
    public Result rename(@PathVariable Long id,  String fileFullName) {
        fileUploader.rename(id, fileFullName);
        return ResultUtils.success();
    }
}
