package com.rick.fileupload.image;

import com.rick.common.http.model.Result;
import com.rick.common.http.model.ResultUtils;
import com.rick.fileupload.core.model.ImageParam;
import com.rick.fileupload.persist.Document;
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

    /**
     * 图片查看
     * @param request
     * @param response
     * @param imageParam
     */
    @GetMapping("/{id}")
    public void view(HttpServletRequest request, HttpServletResponse response, @PathVariable("id") Long id, ImageParam imageParam) {
        imageService.view(request, response, id, imageParam);
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
    public Result<Document> cropPic(@RequestParam("file") MultipartFile file, int x, int y, int w, int h, int aspectRatioW, int aspectRatioH) throws IOException {
        return ResultUtils.success(imageService.cropPic(file, x, y, w, h, aspectRatioW, aspectRatioH));
    }

    /**
     * 按比例自动裁剪
     * @param file
     * @param aspectRatioW
     * @param aspectRatioH
     * @throws IOException
     */
    @PostMapping("/cropPic2")
    public Result<Document>  cropPic(@RequestParam("file") MultipartFile file , int aspectRatioW, int aspectRatioH) throws IOException {
        return ResultUtils.success(imageService.cropPic(file, aspectRatioW, aspectRatioH));
    }

    /**
     * 创建用户名头像
     * @param text
     * @return
     */
    @PostMapping("create")
    public Result<String> createImage(String text) throws IOException {
        String url = imageService.createImage(text);
        return ResultUtils.success(url);
    }

}
