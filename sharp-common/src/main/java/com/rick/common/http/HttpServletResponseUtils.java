package com.rick.common.http;


import org.apache.commons.codec.binary.Base64;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

/**
 * All rights Reserved, Designed By www.xhope.top
 *
 * @version V1.0
 * @Description: (用一句话描述该文件做什么)
 * @author: Rick.Xu
 * @date: 6/16/20 1:43 PM
 * @Copyright: 2020 www.yodean.com. All rights reserved.
 */
public final class HttpServletResponseUtils {

    private static final String CHARSET_UTF_8 = StandardCharsets.UTF_8.name();

    private HttpServletResponseUtils() {
    }

    public static OutputStream getOutputStream(HttpServletRequest request, HttpServletResponse response, String fileName) throws IOException {
        OutputStream os;
        String _fileName = fileName.replaceAll("[\\/:*?\"<>[|]]", "");

        String browserType = request.getHeader("User-Agent").toLowerCase();

        if (browserType.indexOf("firefox") > -1) { //FF
            _fileName = "=?" + CHARSET_UTF_8 + "?B?" + (new String(Base64.encodeBase64(_fileName.getBytes(CHARSET_UTF_8)))) + "?=";
        } else {
            if (fileName.matches(".*[^\\x00-\\xff]+.*")) {
                if (request.getHeader("User-Agent").toLowerCase().indexOf("msie") > -1) { // IE
                    _fileName = java.net.URLEncoder.encode(_fileName, CHARSET_UTF_8);
                } else { //其他
                    _fileName = new String(_fileName.getBytes(CHARSET_UTF_8), "ISO-8859-1");
                }
            }
        }

        response.reset();// 清空输出流   
        response.setCharacterEncoding(CHARSET_UTF_8);
        response.setHeader("Content-disposition", "attachment; filename=" + _fileName + "");// 设定输出文件头
        response.setContentType("application/vnd.ms-excel;charset=" + CHARSET_UTF_8 + "");// 定义输出类型
        os = response.getOutputStream(); // 取得输出流   
        return os;
    }

    public static void write(HttpServletResponse response, String contentType, String value) {
        response.setContentType(contentType);
        try {
            PrintWriter pw = response.getWriter();
            pw.write(value);
            pw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
