package com.rick.common.util;

import lombok.experimental.UtilityClass;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

/**
 * @author Rick.Xu
 * @date 2024/8/23 08:27
 */
@UtilityClass
public class FileUtils {


    /**
     * 根据“文件名的后缀”获取文件内容类型（而非根据File.getContentType()读取的文件类型）
     * @param fileName 带验证的文件名
     * @return 返回文件类型
     */
    public static String getContentType(String fileName) {
        String contentType = "application/octet-stream";
        if (fileName.lastIndexOf(".") < 0)
            return contentType;

        fileName = fileName.toLowerCase();
        fileName = fileName.substring(fileName.lastIndexOf(".") + 1);
        if (fileName.equals("html") || fileName.equals("htm") || fileName.equals("shtml")) {
            contentType = "text/html";
        } else if (fileName.equals("apk")) {
            contentType = "application/vnd.android.package-archive";
        } else if (fileName.equals("sis")) {
            contentType = "application/vnd.symbian.install";
        } else if (fileName.equals("sisx")) {
            contentType = "application/vnd.symbian.install";
        } else if (fileName.equals("exe")) {
            contentType = "application/x-msdownload";
        } else if (fileName.equals("msi")) {
            contentType = "application/x-msdownload";
        } else if (fileName.equals("css")) {
            contentType = "text/css";
        } else if (fileName.equals("xml")) {
            contentType = "text/xml";
        } else if (fileName.equals("gif")) {
            contentType = "image/gif";
        } else if (fileName.equals("jpeg") || fileName.equals("jpg")) {
            contentType = "image/jpeg";
        } else if (fileName.equals("js")) {
            contentType = "application/x-javascript";
        } else if (fileName.equals("atom")) {
            contentType = "application/atom+xml";
        } else if (fileName.equals("rss")) {
            contentType = "application/rss+xml";
        } else if (fileName.equals("mml")) {
            contentType = "text/mathml";
        } else if (fileName.equals("txt")) {
            contentType = "text/plain";
        } else if (fileName.equals("jad")) {
            contentType = "text/vnd.sun.j2me.app-descriptor";
        } else if (fileName.equals("wml")) {
            contentType = "text/vnd.wap.wml";
        } else if (fileName.equals("htc")) {
            contentType = "text/x-component";
        } else if (fileName.equals("png")) {
            contentType = "image/png";
        } else if (fileName.equals("tif") || fileName.equals("tiff")) {
            contentType = "image/tiff";
        } else if (fileName.equals("wbmp")) {
            contentType = "image/vnd.wap.wbmp";
        } else if (fileName.equals("ico")) {
            contentType = "image/x-icon";
        } else if (fileName.equals("jng")) {
            contentType = "image/x-jng";
        } else if (fileName.equals("bmp")) {
            contentType = "image/x-ms-bmp";
        } else if (fileName.equals("svg")) {
            contentType = "image/svg+xml";
        } else if (fileName.equals("jar") || fileName.equals("var")
                || fileName.equals("ear")) {
            contentType = "application/java-archive";
        } else if (fileName.equals("doc")) {
            contentType = "application/msword";
        } else if (fileName.equals("pdf")) {
            contentType = "application/pdf";
        } else if (fileName.equals("rtf")) {
            contentType = "application/rtf";
        } else if (fileName.equals("xls")) {
            contentType = "application/vnd.ms-excel";
        } else if (fileName.equals("ppt")) {
            contentType = "application/vnd.ms-powerpoint";
        } else if (fileName.equals("7z")) {
            contentType = "application/x-7z-compressed";
        } else if (fileName.equals("rar")) {
            contentType = "application/x-rar-compressed";
        } else if (fileName.equals("swf")) {
            contentType = "application/x-shockwave-flash";
        } else if (fileName.equals("rpm")) {
            contentType = "application/x-redhat-package-manager";
        } else if (fileName.equals("der") || fileName.equals("pem")
                || fileName.equals("crt")) {
            contentType = "application/x-x509-ca-cert";
        } else if (fileName.equals("xhtml")) {
            contentType = "application/xhtml+xml";
        } else if (fileName.equals("zip")) {
            contentType = "application/zip";
        } else if (fileName.equals("mid") || fileName.equals("midi")
                || fileName.equals("kar")) {
            contentType = "audio/midi";
        } else if (fileName.equals("mp3")) {
            contentType = "audio/mpeg";
        } else if (fileName.equals("ogg")) {
            contentType = "audio/ogg";
        } else if (fileName.equals("m4a")) {
            contentType = "audio/x-m4a";
        } else if (fileName.equals("ra")) {
            contentType = "audio/x-realaudio";
        } else if (fileName.equals("3gpp")
                || fileName.equals("3gp")) {
            contentType = "video/3gpp";
        } else if (fileName.equals("mp4")) {
            contentType = "video/mp4";
        } else if (fileName.equals("mpeg")
                || fileName.equals("mpg")) {
            contentType = "video/mpeg";
        } else if (fileName.equals("mov")) {
            contentType = "video/quicktime";
        } else if (fileName.equals("flv")) {
            contentType = "video/x-flv";
        } else if (fileName.equals("m4v")) {
            contentType = "video/x-m4v";
        } else if (fileName.equals("mng")) {
            contentType = "video/x-mng";
        } else if (fileName.equals("asx") || fileName.equals("asf")) {
            contentType = "video/x-ms-asf";
        } else if (fileName.equals("wmv")) {
            contentType = "video/x-ms-wmv";
        } else if (fileName.equals("avi")) {
            contentType = "video/x-msvideo";
        }
        return contentType;
    }

    public static String getFilenameExtension(@Nullable String path) {
        if (path == null) {
            return null;
        } else {
            int extIndex = path.lastIndexOf(46);
            if (extIndex == -1) {
                return null;
            } else {
                int folderIndex = path.lastIndexOf("/");
                return folderIndex > extIndex ? null : path.substring(extIndex + 1);
            }
        }
    }

    public static String stripFilenameExtension(String path) {
        int extIndex = path.lastIndexOf(46);
        if (extIndex == -1) {
            return path;
        } else {
            int folderIndex = path.lastIndexOf("/");
            return folderIndex > extIndex ? path : path.substring(0, extIndex);
        }
    }

    public static String getFilename(String path) {
        if (path == null) {
            return null;
        } else {
            int separatorIndex = path.lastIndexOf("/");
            return separatorIndex != -1 ? path.substring(separatorIndex + 1) : path;
        }
    }

    public static String fullName(String name, String extension) {
        if (org.apache.commons.lang3.StringUtils.isBlank(extension)) {
            return name;
        }

        return name +  "." + extension;
    }


    /**
     * 是否正确的文件类型 单个图片
     * @param request
     * @return
     */
    public Boolean isImageType(HttpServletRequest request, String name) {
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        String extension = org.springframework.util.StringUtils.getFilenameExtension(multipartRequest.getFile(name).getOriginalFilename());

        return isImageType(extension);
    }

    public static boolean isImageType(String path, String contentType) {
        if (isImageType(path)) {
            return true;
        }

        if (Objects.nonNull(contentType) && contentType.startsWith("image")) {
            return true;
        }

        return false;
    }

    /**
     * 判断是否是合法的文件类型
     * @param path file.txt .txt text
     * @return
     */
    public static boolean isImageType(String path) {
        if (!StringUtils.isEmpty(path) && path.matches("(?i)(.*)[.]?(bmp|png|jpeg|jpg|gif|ico)")) {
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
