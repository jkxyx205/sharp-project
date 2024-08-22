package com.rick.fileupload.core.support;

import lombok.experimental.UtilityClass;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Rick.Xu
 * @date 2024/8/22 19:19
 */
@UtilityClass
public class FileCheckUtils {
    /**
     * 是否正确的文件类型 单个图片
     * @param request
     * @return
     */
    public Boolean isImageType(HttpServletRequest request, String name) {
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        String ext = StringUtils.getFilenameExtension(multipartRequest.getFile(name).getOriginalFilename());

        return isImageType(ext);
    }

    public Boolean isImageType(String fileName) {
        String ext = StringUtils.getFilenameExtension(fileName);

        if (!StringUtils.isEmpty(ext) && ext.matches("(?i)(bmp|png|jpeg|jpg|gif|ico)")) {
            return true;
        }

        return false;
    }

    /**
     * 是否超出最到文件上传的值单位MB
     * @param request
     * @param name
     * @return
     */
    public Boolean maximumSize(HttpServletRequest request, String name, int size) {
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        long fileSize = multipartRequest.getFile(name).getSize();
        if (size * 1024 * 1024 < fileSize) {
            return true;
        }

        return false;
    }
}
