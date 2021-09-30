package com.rick.fileupload.client;

import com.rick.common.http.HttpServletResponseUtils;
import com.rick.common.http.model.Result;
import com.rick.common.http.model.ResultUtils;
import com.rick.fileupload.core.model.FileMeta;
import com.rick.fileupload.core.model.ImageParam;
import com.rick.fileupload.core.support.FileMetaUtils;
import com.rick.fileupload.plugin.image.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * All rights Reserved, Designed By www.xhope.top
 *
 * @version V1.0
 * @Description: 图片处理类
 * @author: Rick.Xu
 * @date: 10/19/18 14:28
 * @Copyright: 2018 www.yodean.com. All rights reserved.
 */
@RestController
@RequestMapping("/images")
public class ImageController {

    @Autowired
    private ImageService imageService;

    @Autowired
    private DocumentService documentService;

    /**
     * http://localhost:8080/images/475036437923139584?rw=1&rh=1&p=0&r=30&w=500
     * 原图按1:1裁剪，旋转30度，宽度500像素
     * 图片查看
     * @param request
     * @param response
     * @param imageParam
     */
    @GetMapping("/{id}")
    public void preview(HttpServletRequest request, HttpServletResponse response, @PathVariable("id") Long id, ImageParam imageParam) throws IOException {
        Document document = documentService.findById(id);
        imageService.preview(document, imageParam, HttpServletResponseUtils.getOutputStreamAsView(request, response, document.getFullName()));
    }

    /**
     * 手动裁剪
     * @param file
     * @param x
     * @param y
     * @param w
     * @param h
     * @param aspectRatioW
     * @param aspectRatioH
     * @throws IOException
     */
    @PostMapping("/cropPic")
    public Result<FileMeta> cropPic(@RequestParam("file") MultipartFile file, int x, int y, int w, int h, int aspectRatioW, int aspectRatioH) throws IOException {
        return ResultUtils.success(imageService.cropPic(FileMetaUtils.parse(file), x, y, w, h, aspectRatioW, aspectRatioH));
    }

    /**
     * 按比例自动裁剪
     * @param file
     * @param aspectRatioW
     * @param aspectRatioH
     * @throws IOException
     */
    @PostMapping("/cropPic2")
    public Result<FileMeta>  cropPic(@RequestParam("file") MultipartFile file , int aspectRatioW, int aspectRatioH) throws IOException {
        return ResultUtils.success(imageService.cropPic(FileMetaUtils.parse(file), aspectRatioW, aspectRatioH));
    }

    /**
     * 创建用户名头像
     * @param text
     * @return
     */
    @PostMapping("create")
    public Result<String> createImage(String text) throws IOException {
        String url = imageService.createImage(text, "header");
        return ResultUtils.success(url);
    }
}
